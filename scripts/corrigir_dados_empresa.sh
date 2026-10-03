#!/bin/bash
# Grava os dados reais da empresa que tem o certificado A1.
#
# O problema que resolve: a empresa dona do certificado foi criada com os dados
# de preenchimento de tela ("EMPRESA INICIAL", sem UF, sem endereco) e o CNPJ
# certo. Num banco novo, a empresa nasce com o CNPJ 00000000000000 e a UF RS,
# que e' pior: errado desde o comeco, e o erro so aparece quando alguem emite
# documento fiscal.
#
# Os dados vem de duas fontes que concordam entre si:
#   - o certificado ICP-Brasil, no campo CN: "...TECNOLOGIA DA I:00000000000191"
#   - os dados cadastrais publicos da Receita Federal
#
# Nao mexe em nenhuma outra empresa. So a que tem o CNPJ 00000000000191.
#
# Rodar como root, no servidor:
#   sudo bash scripts/corrigir_dados_empresa.sh
#
# Faz backup da tabela antes, em /tmp. So grava se o backup passar.

set -euo pipefail

CNPJ="${BRASIL_SAAS_CNPJ:-00000000000191}"
BANCO="${BRASIL_SAAS_BANCO:-brasil-saas}"
SCHEMA="brasil_saas"

# A conexao segue o mesmo caminho do restaurar_banco.sh: o usuario 'sa', com o
# certificado da PKI. Sem isto, o psql chamado por root cai no fallback scram e
# pergunta a senha do postgres, que ninguem tem a mao na maquina.
PGHOST="${PGHOST:-localhost}"
PGUSER="${PGUSER:-sa}"
CERT_DIR="${BRASIL_SAAS_CERTS_DIR:-/etc/brasil-saas/pki}"
if [ -d "$CERT_DIR" ]; then
    export PGSSLROOTCERT="$CERT_DIR/ca.crt"
    export PGSSLCERT="$CERT_DIR/issued/sa.crt"
    if [ -r "$CERT_DIR/private/sa.key" ]; then
        export PGSSLKEY="$CERT_DIR/private/sa.key"
    elif [ -r "$CERT_DIR/private/sa.pk8" ]; then
        export PGSSLKEY="$CERT_DIR/private/sa.pk8"
    fi
fi

# O 'sa' nao e superuser do Postgres, entao um SELECT/UPDATE normal nao
# precisa de nada alem disso. Se ainda assim o psql recusar, o plano B e' o
# usuario do sistema 'postgres', que tem acesso local sem senha.
#
# O stderr NAO e descartado: um script de manutencao que engole o erro faz
# "deu falha" parecer "deu certo" quando o que houve foi o banco inexistente.
# O que vai para o arquivo de log e' so a mensagem do psql.
LOG="/tmp/corrigir_dados_empresa.log"

# O 'sa' entra por TCP com certificado — as regras do pg_hba.conf do ERP sao
# hostssl em 127.0.0.1/32, e so existem para TCP.
q() {
    psql -h "$PGHOST" -U "$PGUSER" -v ON_ERROR_STOP=1 "$@" 2>>"$LOG"
}

# O postgres do sistema entra pelo socket, SEM -h. Isso e' o que faz funcionar:
# no socket a autenticacao e' peer (basta ser o usuario postgres do SO), e no
# TCP o papel postgres exige senha. Passar -h localhost aqui daria
# "fe_sendauth: no password supplied" — que e' a falha que este script teve na
# primeira versao.
q_admin() {
    su postgres -c "$(printf '%q ' psql -v ON_ERROR_STOP=1 "$@")" 2>>"$LOG"
}

# Tenta o 'sa'; se recusar, tenta como postgres do sistema. Uma manutencao que
# so funciona com o certificado nao serve quando o certificado e' justamente o
# que esta quebrado.
q_ou() { q "$@" || q_admin "$@"; }

#traduz o erro do psql para algo que diz o que fazer, e nao so que falhou
traduzir_erro() {
    local erro="$1"
    case "$erro" in
        *"does not exist"*)
            echo "ERRO: o banco '$BANCO' nao existe nesta maquina." >&2
            echo "      Se o nome do banco for outro, rode com BRASIL_SAAS_BANCO=<nome>." >&2 ;;
        *"no pg_hba.conf entry"*|*"password"*)
            echo "ERRO: o Postgres recusou a conexao do usuario '$PGUSER'." >&2
            echo "      A PKI pode estar em outro lugar. Aponte com BRASIL_SAAS_CERTS_DIR=<pasta>," >&2
            echo "      ou rode como: sudo -u postgres bash $0" >&2 ;;
        *"could not connect"*|*"Connection refused"*)
            echo "ERRO: o Postgres nao esta rodando, ou nao escuta em $PGHOST." >&2
            echo "      Confira com: systemctl status postgresql" >&2 ;;
        *"permission denied"*)
            echo "ERRO: o usuario '$PGUSER' nao tem permissao de escrita nessa tabela." >&2 ;;
        *) echo "ERRO: o psql falhou. A mensagem completa esta em $LOG:" >&2
           tail -3 "$LOG" | sed 's/^/      /' >&2 ;;
    esac
}

: > "$LOG"

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

echo "==> estado atual da empresa $CNPJ"
if ! q_ou -d "$BANCO" -c "
    SELECT id, razao_social, nome_fantasia, cnpj, uf, cep, status
      FROM $SCHEMA.bc_core_empresa WHERE cnpj = '$CNPJ';
"; then
    traduzir_erro "$(tail -5 "$LOG" 2>/dev/null)"
    exit 1
fi

N=$(q_ou -d "$BANCO" -Atc \
    "SELECT count(*) FROM $SCHEMA.bc_core_empresa WHERE cnpj = '$CNPJ'" \
    | grep -E '^[0-9]+$')
if [ "$N" = "0" ]; then
    echo "ERRO: nenhuma empresa com o CNPJ $CNPJ em $BANCO." >&2
    echo "      Se o CNPJ no banco estiver com mascara ou errado, ajuste e rode de novo." >&2
    exit 1
fi

BACKUP="/tmp/empresa_antes_$(date +%Y%m%d-%H%M%S).sql"
q_ou -d "$BANCO" -Atc "SELECT 1" >/dev/null 2>&1
# O nome do backup vai entre aspas no printf %q porque tem data e hora; os
# outros argumentos sao palavras simples. Sem o espaco em %q , so o primeiro
# argumento seria escapado e o resto iria cru — que produz um backup vazio sem
# nenhuma mensagem de erro.
su postgres -c "$(printf '%q ' pg_dump --data-only --table="$SCHEMA.bc_core_empresa" -f "$BACKUP" "$BANCO")" 2>/dev/null || true
if [ ! -s "$BACKUP" ]; then
    echo "ERRO: o backup ficou vazio, nao vou gravar sem ele." >&2
    exit 1
fi
echo "==> backup em $BACKUP ($(wc -c < "$BACKUP") bytes)"

# O SQL vai num arquivo, e nao por heredoc no stdin: o caminho de administracao
# roda o psql atraves de `su postgres -c`, e o su nao repassa o heredoc. Com o
# heredoc, o psql do su recebia stdin vazio, rodava sem erro, e nao gravava
# nada — o script ainda imprimia "gravando" e "como ficou", com a empresa
# igual. Falha silenciosa, que e' a pior.
SQL_ARQ=$(mktemp /tmp/corrigir_empresa.XXXXXX.sql)
# O arquivo precisa ser legivel pelo usuario postgres: o caminho de
# administracao roda o psql atraves de `su postgres -c`, e o mktemp cria o
# arquivo com dono root e modo 600. Sem o chmod, o postgres recebia
# "permission denied" ao abrir o SQL.
chmod 644 "$SQL_ARQ"
trap 'rm -f "$SQL_ARQ"' EXIT
cat > "$SQL_ARQ" <<SQL
UPDATE $SCHEMA.bc_core_empresa SET
    razao_social      = 'EURIPEDES BATISTA DE PAIVA JUNIOR TECNOLOGIA DA INFORMACAO LTDA',
    nome_fantasia     = 'SRVCLOUD CONSULTORIA',
    regime_tributario = 'SIMPLES',
    codigo_ibge       = '3550308',
    endereco          = 'RUA PAIS LEME',
    numero            = '215',
    complemento       = 'CONJ 1713',
    bairro            = 'PINHEIROS',
    cep               = '05424150',
    uf                = 'SP',
    telefone          = '4197880145',
    status            = 'ATIVO',
    updated_at        = now()
 WHERE cnpj = '$CNPJ';
SQL

echo "==> gravando"
if ! q_ou -d "$BANCO" -q -f "$SQL_ARQ"; then
    traduzir_erro "$(tail -5 "$LOG" 2>/dev/null)"
    exit 1
fi

# Confere que gravou. O codigo de saida do psql nao serve aqui: um UPDATE que
# casa zero linha devolve 0 e nao reclama. Sem esta checagem, "gravou" e
# "correu" continuam sendo a mesma coisa na pratica.
CHANGED=$(q_ou -d "$BANCO" -Atc \
    "SELECT count(*) FROM $SCHEMA.bc_core_empresa
      WHERE cnpj = '$CNPJ'
        AND razao_social = 'EURIPEDES BATISTA DE PAIVA JUNIOR TECNOLOGIA DA INFORMACAO LTDA'
        AND uf = 'SP'" 2>/dev/null | grep -E '^[0-9]+$' || echo 0)
if [ "$CHANGED" != "1" ]; then
    echo "ERRO: o UPDATE rodou mas a empresa nao ficou com os dados novos." >&2
    echo "      Verifique se o CNPJ no banco tem 14 digitos e sem mascara:" >&2
    q_ou -d "$BANCO" -c "SELECT id, cnpj, razao_social FROM $SCHEMA.bc_core_empresa WHERE cnpj LIKE '%$CNPJ%'" >&2 || true
    echo "      Nada foilosto: o backup esta em $BACKUP" >&2
    exit 1
fi

echo "==> gravado e conferido"
echo "==> como ficou"
q_ou -d "$BANCO" -c "
    SELECT id, razao_social, nome_fantasia, cnpj, uf, regime_tributario, status
      FROM $SCHEMA.bc_core_empresa WHERE cnpj = '$CNPJ';
"

echo
echo "Para desfazer:"
echo "  psql -d $BANCO -c '\\i $BACKUP'"
