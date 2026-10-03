#!/usr/bin/env bash
#
# Restaura o banco brasil_saas a partir das partes do dump.
#
# O QUE ESTE SCRIPT FAZ, E POR QUE EXISTE
#
# O dump e um arquivo so, partido em 4 zip. A numeracao e a coisa que faz
# alguem restaurar errado: DENTRO do zip 01 o pedaco se chama parte00, dentro
# do 02 e parte01, e assim por diante. Um laco que procure "parte01" dentro de
# "parte_dump_01.zip" nao encontra, e a pessoa conclui que o zip esta ruim.
# Aqui o nome interno e calculado a partir do nome do zip, e o script confere
# o tamanho de cada pedaco antes de juntar.
#
# O docs/infra/RESTAURAR.md tem o roteiro em markdown. Este arquivo e o mesmo roteiro
# executavel, com as conferencias que o markdown deixa para o_make shift: um
# pedaco faltando e um dump truncado, e o pg_restore falha no fim, depois de
# restaurar 130 MB, sem dizer o que faltou.
#
# USO
#
#   scripts/restaurar_banco.sh              # confirmar, e restaurar
#   scripts/restaurar_banco.sh --simular    # so conferir as partes
#
# O NOME DO BANCO NAO E PARAMETRO. Ele esta gravado dentro do dump, que traz
# "CREATE DATABASE brasil_saas". Passar outro nome na linha de comando nao
# renomeia nada: o --create faz o dump criar o brasil_saas e restaurar nele.
# Restaurar com outro nome exigiria reescrever o dump.
#
# O QUE ELE NAO FAZ
#
# Nao mexe no MongoDB: isso e mongorestore, e o dump de cada colecao esta em
# mongo_brasil-saas.zip. Ver docs/infra/RESTAURAR.md, secao 3.
#
# Nao aplica as migrations. A versao do Flyway no dump vem do
# dump_manifest.env (DUMP_FLYWAY). Depois de restaurar, o ERP sobe e roda da
# proxima versao ate a ultima sozinho — e o que ele eSuposto fazer. Rodar
# migration na mao aqui seria o ERP e o script discordando sobre quem esta em
# dia.

set -euo pipefail

PROJETO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TRABALHO="${BRASIL_SAAS_RESTORE_DIR:-/tmp/brasil_saas_restaura}"
# O BANCO e o SCHEMA NAO TEM O MESMO NOME, e confundir os dois faz o script
# RELER COM SUCESSO e depois dizer que falhou.
#
#   banco  = brasil-saas   (com HIFEN)  — e' o que o pg_restore --create cria
#   schema = brasil_saas   (com UNDERSCORE) — 167 tabelas dentro do banco
#
# O script usava `BANCO="brasil_saas"` e passava esse valor no `-d` do psql. O
# psql respondia
#
#     FATAL: database "brasil_saas" does not exist
#
# que a versao anterior engolia com `2>/dev/null || echo`, e a verificacao final
# recebia vazio, via no `-lt 100` e imprimia
#
#     ERRO: apos o restore o schema brasil_saas tem 0 tabelas.
#
# Com o restore tendo dado CERTO: pg_restore saiu 0 e o banco tinha as 167
# tabelas do manifesto. E' a pior forma de falha — a que faz alguem restaurar
# de novo, ou restaurar para outro lugar, achando que deu errado.
BANCO="brasil-saas"      # o banco; gravado no dump; nao e parametro
SCHEMA="brasil_saas"     # o schema DENTRO do banco
PGHOST="${PGHOST:-localhost}"
PGUSER="${PGUSER:-sa}"

# Os certificados. O caminho do repositorio tem espaco ("GIT Repos") e o libpq
# recusa espaco em sslrootcert quando o caminho vem na URI; as variaveis de
# ambiente resolvem sem parsing de URI. E' o mesmo remedy que o exportar_dump.sh
# usa, e pela mesma razao.
CERT_DIR="${BRASIL_SAAS_CERTS_DIR:-$PROJETO/certs/pki}"
if [[ ! -r "$CERT_DIR/ca.crt" ]]; then
    CERT_DIR="$HOME/.local/share/brasil-saas-certs/pki"
    echo "aviso: PKI nao encontrada no projeto; usando $CERT_DIR"
fi
export PGSSLROOTCERT="$CERT_DIR/ca.crt"
export PGSSLCERT="$CERT_DIR/issued/sa.crt"
# A chave privada e' sa.key. O sa.pk8 que o installbase.sh cita e' o mesmo par em
# outro formato: o libpq le o .key, e apontar para o .pk8 da "unable to load
# private key" sem dizer que o problema e' o formato.
export PGSSLKEY="$CERT_DIR/private/sa.key"
[[ -r "$PGSSLKEY" ]] || export PGSSLKEY="$CERT_DIR/private/sa.pk8"
# O padrao, para quando o manifesto nao estiver presente — dump antigo, ou
# arquivo movido sem o dump_manifest.env ao lado. O exportador hoje faz 8.
PARTES=8

# O numero de partes, e o tamanho de cada pedaco, vemem do dump_manifest.env, que o exportar_dump.sh
# escreve na hora da exportacao.
#
# Este numero estava FIXO no codigo — 34205801, o do dump de 2026-09-25. Isso
# ata o restaurador a UM dump: qualquer exportacao nova tem tamanho diferente, e
# o script recusava as 4 partes com "o zip esta corrompido", sendo que o zip
# estava inteiro e era apenas outro dump. Pior: quem se deparasse com a recusa
# na conferencia achava que o transporte tinha estragado o arquivo e reexportava —
# o que produz um dump novo, de outro tamanho, e a mesma recusa.
#
# Se o manifesto nao existir, o valor antigo vira o padrao e o script AVISA que
# esta conferindo contra um dump de 2026-09-25 — recusar sem explicar seria
# pior do que conferir com um numero velho.
TAMANHO_ESPERADO=34205801
ORIGEM_TAMANHO="valor fixo no codigo (dump de 2026-09-25)"
if [[ -f "$PROJETO/dump_manifest.env" ]]; then
    # shellcheck disable=SC1091
    source "$PROJETO/dump_manifest.env"
    TAMANHO_ESPERADO="$DUMP_TAMANHO_PEDACO"
    PARTES="$DUMP_PARTES"
    ORIGEM_TAMANHO="dump_manifest.env, gerado em $DUMP_EXPORTADO_EM"
    # A lista dos tamanhos reais, quando o exportador a escreveu. E' o criterio
    # exato; o TAMANHO_ESPERADO e' o arredondado, que so serve de reserva.
    TAMANHOS_REAIS="${DUMP_TAMANHOS:-}"
    echo "=============================================================="
    echo " Dump: $DUMP_BANCO (schema $DUMP_SCHEMA)"
    echo " exportado em $DUMP_EXPORTADO_EM  |  Postgres $DUMP_POSTGRES"
    echo " $DUMP_TABELAS tabelas, $DUMP_LINHAS linhas, Flyway ate V$DUMP_FLYWAY"
    echo "=============================================================="
else
    echo "AVISO: dump_manifest.env ausente. Conferindo o tamanho do pedaco contra"
    echo "       $TAMANHO_ESPERADO, que e' do dump de 2026-09-25. Se voce"
    echo "       exportou outro dump, este numero esta errado e a restauracao vai"
    echo "       recusar as partes. Gere o manifesto com ./exportar_dump.sh"
fi

SIMULAR=0
[[ "${1:-}" == "--simular" ]] && SIMULAR=1

# A CONEXAO E POR CERTIFICADO, e o search_path.
#
# Este script usava PGUSER=postgres e pedia a senha no passo 5. Nao funciona
# neste ambiente, e a falha era de dois jeitos ao mesmo tempo:
#
#   - o 'postgres' entra por peer, via wheelmap, que so o root alcanca; e
#   - as conexoes por TCP sao scram, entao qualquer usuario cae na regra que
#     exige senha — inclusive o 'sa', que tem certificado e nao precisa dela.
#
# O sintoma era "fe_sendauth: no password supplied", que e' a MENSAGEM DE SENHA
# FALTANDO. E nao era: faltava a regra do certificado. Quem depurava configurava
# senha, e a senha nao resolvia, porque a regra que casava era a de scram.
#
# O 'sa' e' superusuario e, pela regra que o installbase.sh escreve, entra por
# certificado nos bancos de manutencao — 'postgres' e 'template1'. E' o que o
# pg_restore --create exige: a conexao comeca no 'postgres', que existe sempre,
# e o proprio dump nomeia o banco de destino.
#
# O search_path deste ambiente e "$user", public, e o schema brasil_saas nao
# esta nele. Sem isto, psql e pg_dump dizem "no matching tables were found"
# para uma tabela que existe. Nao e a tabela que some, e o caminho de busca.
#
# O PGPASSFILE fica de fora de proposito: um .pgpass no home faria o psql CAIR na
# regra de scram e pedir senha, que e' exatamente o que o certificado evita.
export PGOPTIONS="${PGOPTIONS:--c search_path=brasil_saas,public}"
export PGOPTIONS="${PGOPTIONS:--c search_path=brasil_saas,public}"

echo "=============================================================="
echo " Restaurar o banco '$BANCO'"
echo " projeto: $PROJETO"
echo " trabalho: $TRABALHO"
echo "=============================================================="
echo

# ---------------------------------------------------------------- #
# 1. As partes existem?
# ---------------------------------------------------------------- #
echo "[1/5] conferindo as partes do dump"
faltando=0
for i in $(seq 1 $PARTES); do
    N=$(printf "%02d" "$i")
    Z="$PROJETO/parte_dump_${N}.zip"
    if [[ ! -f "$Z" ]]; then
        echo "    FALTA: $Z"
        faltando=$((faltando + 1))
        continue
    fi
    # O nome interno do pedaco e N-1, comecando em zero.
    interno="parte$(printf "%02d" $((i - 1)))"
    tem=$(unzip -l "$Z" 2>/dev/null | awk -v p="$interno" '$4==p {print 1}')
    if [[ -z "$tem" ]]; then
        echo "    $Z nao contem '$interno' (esperado)"
        faltando=$((faltando + 1))
        continue
    fi
    printf "    %-28s ok  (%s bytes)\n" "parte_dump_${N}.zip" "$(stat -c%s "$Z")"
done

if [[ $faltando -gt 0 ]]; then
    echo
    echo "ERRO: $faltando parte(s) faltando. Nada foi restaurado."
    exit 1
fi
echo "    as $PARTES partes estao no projeto"
echo

# ---------------------------------------------------------------- #
# 2. Extrair e conferir o tamanho de cada pedaco
# ---------------------------------------------------------------- #
echo "[2/5] juntando os pedacos em $TRABALHO/dados.dump"
mkdir -p "$TRABALHO"
rm -f "$TRABALHO"/pedaco_* "$TRABALHO"/dados.dump

for i in $(seq 1 $PARTES); do
    N=$(printf "%02d" "$i")
    interno="parte$(printf "%02d" $((i - 1)))"
    destino="$TRABALHO/pedaco_$(printf "%02d" "$i")"
    unzip -o -j "$PROJETO/parte_dump_${N}.zip" "$interno" -d "$TRABALHO" >/dev/null
    mv "$TRABALHO/$interno" "$destino"

    tamanho=$(stat -c%s "$destino")

    # O tamanho esperado DESTE pedaco.
    #
    # A versao anterior comparava os quatro contra o mesmo numero, o
    # TAMANHO_ESPERADO. Esse numero e' o TAM_CHUNK do `split -b`, que e'
    # arredondado PARA CIMA — porque pedaco de zero byte nao serve. Entao, se o
    # dump nao divide exato pelo numero de partes, o ULTIMO pedaco fica menor:
    #
    #   dump de 137.400.275 bytes em 8 partes
    #     TAM_CHUNK = 17.175.035
    #     pedaco_01..07  17.175.035
    #     pedaco_08      17.175.030   <-- resto da divisao
    #
    # e o script recusava o ultimo com "o zip esta corrompido". O zip estava
    # inteiro: a mensagem estava errada, e quem restaurasse numa maquina nova
    # concluiria que o transporte estragou o arquivo.
    #
    # O dump antigo, de 136.823.204 bytes, dividia exato por 4, entao os quatro
    # tinham o mesmo tamanho e a regra passava por sorte. Qualquer dump de
    # tamanho nao divisivel por 4 a teria derrubado.
    #
    # Tres criterios, do mais exato para o mais fraco:
    #   1) a lista DUMP_TAMANHOS do manifesto, se existir
    #   2) a conta total - (partes - 1) * TAM_CHUNK, que e' o ultimo pedaco
    #   3) o TAMANHO_ESPERADO, so para o dump antigo sem manifesto
    if [[ -n "$TAMANHOS_REAIS" ]]; then
        # campo i da lista, 1-based
        esperado_i=$(echo "$TAMANHOS_REAIS" | tr ' ' '\n' | sed -n "${i}p")
        criterio="manifesto"
    elif [[ "$i" -eq "$PARTES" && -n "${DUMP_TOTAL:-}" ]]; then
        esperado_i=$(( DUMP_TOTAL - (PARTES - 1) * TAMANHO_ESPERADO ))
        criterio="conta do ultimo pedaco"
    else
        esperado_i=$TAMANHO_ESPERADO
        criterio="TAMANHO_ESPERADO (reserva)"
    fi

    if [[ -n "$esperado_i" && "$tamanho" -ne "$esperado_i" ]]; then
        echo
        echo "ERRO: $destino tem $tamanho bytes e o esperado e $esperado_i"
        echo "      (criterio: $criterio; origem: $ORIGEM_TAMANHO)"
        echo "      O zip esta corrompido ou foi extraido pela metade."
        exit 1
    fi
    printf "    pedaco_%s  %s bytes  ok  (%s)\n" "$N" "$tamanho" "$criterio"
done

# O glob ordena por nome: pedaco_01..pedaco_04, que e a ordem do dump.
cat "$TRABALHO"/pedaco_* > "$TRABALHO/dados.dump"
TOTAL=$(stat -c%s "$TRABALHO/dados.dump")
echo "    dados.dump: $TOTAL bytes"
echo

# Um dump custom do Postgres comeca com a assinatura PGDMP. Se nao comecar,
# a juncao saiu fora de ordem e o pg_restore vai recusar com um erro que nao
# diz nada sobre a causa.
if ! head -c 5 "$TRABALHO/dados.dump" | grep -q "PGDMP"; then
    echo "ERRO: dados.dump nao comeca com a assinatura PGDMP."
    echo "      As partes estao fora de ordem, ou alguma nao e parte do dump."
    exit 1
fi
echo "    assinatura PGDMP confere"
echo

if [[ $SIMULAR -eq 1 ]]; then
    echo "--simular: conferi as partes e o dump esta integro. Nada foi restaurado."
    exit 0
fi

# ---------------------------------------------------------------- #
# 3. O que sera destruido
# ---------------------------------------------------------------- #
echo "[3/5] sobre '$BANCO'"
existe_bd=$(psql -h "$PGHOST" -U "$PGUSER" -d postgres -tAc \
  "select count(*) from pg_database where datname='$BANCO'" 2>/dev/null || echo 0)
if [[ "$existe_bd" != "0" ]]; then
    # O banco existe. O schema pode nao existir ainda — e o caso de um banco
    # recem-criado, em que a consulta de linhas volta vazia e o script
    # anunciava "EXISTE, com cerca de ? linhas". O "?" e o aviso: nao havia
    # schema para contar. Nao e erro, mas a mensagem parecia falha.
    linhas=$(psql -h "$PGHOST" -U "$PGUSER" -d "$BANCO" -tAc \
      "select coalesce(sum(n_live_tup),0) from pg_stat_user_tables where schemaname='$SCHEMA'" 2>/dev/null || true)
    tabelas=$(psql -h "$PGHOST" -U "$PGUSER" -d "$BANCO" -tAc \
      "select count(*) from information_schema.tables where table_schema='$SCHEMA'" 2>/dev/null || echo 0)
    if [[ "${tabelas:-0}" -gt 0 ]]; then
        echo "    EXISTE com dados: $tabelas tabelas, cerca de ${linhas:-0} linhas."
    else
        echo "    EXISTE, mas vazio (sem o schema $SCHEMA)."
    fi
    echo "    O dump traz 'DROP DATABASE $BANCO' e ela sera SUBSTITUIDA."
    echo
    read -r -p "    Digite o nome do banco para confirmar: " confirma
    if [[ "$confirma" != "$BANCO" ]]; then
        echo "    cancelado. Nada foi feito."
        exit 0
    fi
else
    echo "    nao existe. o proprio dump cria."
fi
echo

# ---------------------------------------------------------------- #
# 4. A role 'sa'
# ---------------------------------------------------------------- #
echo "[4/5] conferindo a role 'sa'"
# O dump foi gerado com a role 'sa' como dona das tabelas. Sem ela, o
# pg_restore cria as tabelas como postgres e as aplicacoes que usam a role sa
# perdem permissao em silencio — o banco funciona e o usuario nao consegue
# escrever.
#
# A CONFERENCIA E A CRACAO VAO COMO `postgres`, PORQUE A `sa` CONECTA POR
# CERTIFICADO E PORQUE AINDA PODE NAO EXISTIR.
#
# A versao anterior abria a conexao como `sa` para ver se ela existia, e depois
# tentava criar a `sa` pela MESMA conexao. Quando a role nao existia — que e'
# exatamente o caso de uma maquina nova, e o unico caso em que este passo
# importa — a conexao falhava, a contagem virava 0, e a criacao falhava do
# mesmo jeito. A impasse era invisivel: o script avisava "nao deu para criar a
# role" e seguia, e o passo seguinte morria com
#
#     fe_sendauth: no password supplied
#
# que fala de senha e nao e senha. Era a role faltando.
#
# `postgres` entra por peer, que e' o unico caminho para criar a primeira role.
# E SEM SENHA: a autenticacao daqui e' o certificado de cliente, e uma senha
# seria uma segunda credencial para o mesmo acesso — a que o dono nao quis.
tem_sa=$(sudo -u postgres psql -Atqc "SELECT count(*) FROM pg_roles WHERE rolname='sa'" postgres 2>/dev/null || echo 0)
if [[ "$tem_sa" == "0" ]]; then
    echo "    nao existe. criando"
    if sudo -u postgres psql -Atqc "CREATE ROLE sa LOGIN CREATEDB" postgres >/dev/null 2>&1; then
        echo "    criada (LOGIN, sem senha — entra por certificado)"
    else
        echo "    ERRO: nao deu para criar a role 'sa'."
        echo "    Crie na mao e rode de novo:"
        echo "      sudo -u postgres psql -c \"CREATE ROLE sa LOGIN CREATEDB\""
    fi
else
    echo "    existe"
fi
echo

# ---------------------------------------------------------------- #
# 5. Restaurar
# ---------------------------------------------------------------- #
echo "[5/5] restaurando"
echo "    pg_restore -d $BANCO --no-owner --no-acl --exit-on-error"
echo

# NAO ha senha, e nao ha prompt.
#
# A autenticacao deste ambiente e' o certificado de cliente, e o passo [3/5] ja
# pediu o nome do banco para confirmar — que e' a confirmacao que importa antes
# de destruir algo. Pedir a senha aqui seria pedir uma credencial que o postgres
# nao consulta: a pessoa digitava, e o passo falhava com "fe_sendauth: no
# password supplied" DEPOIS de ter dito a senha.

# O dump traz "CREATE DATABASE brasil_saas" e "DROP DATABASE brasil_saas".
# Isso significa que o proprio dump cria o banco, e o pg_restore precisa de
# --create conectado em OUTRO banco — o postgres, que existe sempre.
#
# A primeira versao deste script derrubava o banco e depois chamava
# pg_restore -d brasil_saas, o que falha com
#     FATAL: database "brasil_saas" does not exist
# porque o -d tenta conectar no banco que acabou de ser removido. O jeito e
# deixar o dump criar: --create faz o restore para o banco que o proprio dump
# nomeia, e a conexao vai para postgres.
if [[ "$existe_bd" != "0" ]]; then
    echo "    o dump traz CREATE DATABASE: o proprio restore recria o banco"
    echo "    (a conexao vai para 'postgres', e sem --clean o DROP do dump e tolerado)"
else
    echo "    o banco nao existe: o dump cria do zero"
fi

set +e
# O NOME DO BANCO ESTA GRAVADO NO DUMP. Nao ha parametro para isso.
#
# O dump tem "CREATE DATABASE brasil_saas" dentro, entao o pg_restore cria o
# banco com o nome que o proprio dump traz. Passar outro nome na linha de
# comando nao renomeia nada: o --create faz o dump criar o "brasil_saas" e
# restaurar nele, e o -d aponta para onde a conexao COMECA, que tem de ser um
# banco que existe — o postgres. Foi por isso que a opcao --banco foi removida:
# ela prometia uma coisa que nao e possivel fazer.
#
# Se o brasil_saas ja existir, o CREATE DATABASE do dump falha. Sem
# --exit-on-error isso e um aviso e o restore SEGUE — mas segue restaurando no
# banco da conexao, que e o postgres. Isso e o pior dos desfechos: o dump
# legado vai para o postgres e o brasil_saas fica vazio. E foi exatamente o
# que aconteceu na primeira rodada, com 208 constraints do schema publico
# brigando no postgres.
#
# Por isso o banco e derrubado antes. Sem banco, o CREATE do dump sempre
# funciona e o --create sempre restaura no lugar certo.
echo "    o nome do banco esta no dump: brasil_saas. Nao ha como trocar."
if [[ "$existe_bd" != "0" ]]; then
    echo "    derrubando o '$BANCO' existente para o CREATE do dump funcionar"
    psql -h "$PGHOST" -U "$PGUSER" -d postgres -q \
      -c "DROP DATABASE IF EXISTS $BANCO;" 2>&1 | head -3 | sed 's/^/    /'
fi
echo "    pg_restore --create -d postgres dados.dump"
echo
pg_restore -h "$PGHOST" -U "$PGUSER" --no-owner --no-acl --create \
  -d postgres "$TRABALHO/dados.dump" 2>&1 | tail -12 | sed 's/^/    /'
status=${PIPESTATUS[0]}
set -e

# pg_restore devolve 0 mesmo havendo avisos. O que decide se restaurou e se o
# schema existe no lugar certo — e isso que este script confere, e nao o
# codigo de saida.

echo
# pg_restore devolve 0 mesmo com avisos, e as mensagens acima sao de
# "already exists" — inocentes. O que decide se restaurou e o schema existir
# depois, entao o codigo de saida nao e o criterio.
tem_schema=$(psql -h "$PGHOST" -U "$PGUSER" -d "$BANCO" -tAc \
  "select count(*) from information_schema.tables where table_schema='$SCHEMA'" 2>/dev/null || echo 0)
if [[ "${tem_schema:-0}" -lt 100 ]]; then
    echo "ERRO: apos o restore o schema $SCHEMA tem ${tem_schema:-0} tabelas."
    echo "      O dump continua em $TRABALHO/dados.dump e pode ser tentado de novo."
    echo "      Saida do pg_restore: $status"
    exit 1
fi

# ---------------------------------------------------------------- #
# 6. Conferir
# ---------------------------------------------------------------- #
echo "conferindo"
psql -h "$PGHOST" -U "$PGUSER" -d "$BANCO" -tA -F' | ' -c "
  select 'tabelas', count(*)::text from information_schema.tables
   where table_schema='$SCHEMA'
  union all select 'linhas', coalesce(sum(n_live_tup),0)::text
    from pg_stat_user_tables where schemaname='$SCHEMA'
  -- version::numeric: version e' VARCHAR e max() em texto e' lexical — '99' ganhari
  -- de '104'. A conferencia anunciaria um dump mais velho do que ele e'.
  union all select 'flyway ate', coalesce(max(version::numeric)::text,'-')
    from brasil_saas.flyway_schema_history
  union all select 'usuario', count(*)::text from brasil_saas.bc_core_usuario" 2>/dev/null | sed 's/^/    /'

echo
echo "=============================================================="
echo " Restaurado."
echo
echo " O dump parou na versao ${DUMP_FLYWAY:-?} do Flyway. O ERP, ao subir, roda"
echo " da proxima ate a ultima sozinho. NAO rode migration na mao: se o ERP"
echo " subir antes, ele ve o banco em dia e nao faz nada; se rodar depois, o"
echo " checksum diverge e a subida quebra."
echo
echo " Proximo passo:  ./subir-dev.sh"
echo "=============================================================="
