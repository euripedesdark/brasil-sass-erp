#!/bin/bash
# Acha o felipe-ti e o srvcloud no banco que estiver, e traz para o brasil-saas.
#
# O problema que este script resolve: o Postgres desta maquina tem DOIS bancos
# com o mesmo conteudo em epocas diferentes. O "brasil-saas" e' o que o ERP le
# (a URL em application.yml aponta para ele). O "brasilcloud" e' o nome que o
# banco tinha antes e que sobreviveu de um restore antigo.
#
# Quem cria usuario pelo psql no banco errado tem o usuario gravado, com tudo
# certo, e mesmo assim:
#   - nao aparece na tela de Usuarios, que le o brasil-saas
#   - nao entra, porque o login confere a senha no brasil-saas
# Nao e' problema de perfil nem de permissao: e' o usuario estar em outro banco.
#
# Rodar como root, no servidor:
#   sudo bash scripts/resgatar_usuarios.sh
#
# So este script mexe em dado. Ele primeiro so mostra; so copia se voce
# responder SIM.

set -euo pipefail

BANCO_ALVO="${BRASIL_SAAS_BANCO:-brasil-saas}"
ALVOS="${BRASIL_SAAS_USUARIOS:-felipe-ti srvcloud}"
SENHA_Temporaria="${BRASIL_SAAS_SENHA_TEMP:-TrocaEssa#2026}"

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

echo "==> bancos nesta maquina"
psql -d postgres -Atc "
    SELECT '  ' || datname
      || CASE WHEN datname = '$BANCO_ALVO' THEN '   <- o que o ERP le' ELSE '' END
      || format('   %s linhas em bc_core_usuario', (
            SELECT count(*) FROM pg_database d2
             LIMIT 1))
      FROM pg_database
     WHERE datistemplate = false
     ORDER BY (datname = '$BANCO_ALVO') DESC, datname;
" 2>/dev/null | grep -v '^ *$' || true

echo
echo "==> onde estao os usuarios procurados: $ALVOS"
for ALVO in $ALVOS; do
    ACHOU=0
    for BANCO in $(psql -d postgres -Atc "SELECT datname FROM pg_database WHERE datistemplate = false" 2>/dev/null); do
        # nem todo banco tem a tabela; o erro de relacao ausente e esperado
        N=$(psql -d "$BANCO" -Atc \
            "SELECT count(*) FROM information_schema.tables
              WHERE table_schema='brasil_saas' AND table_name='bc_core_usuario'" 2>/dev/null \
            | grep -E '^[0-9]+$' || echo 0)
        [ "$N" = "0" ] && continue

        ACHOU=1
        echo
        echo "  banco: $BANCO"
        psql -d "$BANCO" -Atc "
            SELECT '    id=' || u.id
                 || ' | ' || u.username
                 || ' | ' || coalesce(u.nome, '-')
                 || ' | empresa=' || coalesce(u.empresa_id::text, 'NULA')
                 || ' | ativo=' || coalesce(u.ativo::text, '-')
                 || ' | tentativas=' || coalesce(u.tentativas_login::text, '0')
                 || ' | perfis=' || coalesce((
                        SELECT string_agg(p.nome, ',')
                          FROM brasil_saas.bc_core_usuario_perfil up
                          JOIN brasil_saas.bc_core_perfil p ON p.id = up.perfil_id
                         WHERE up.usuario_id = u.id), 'SEM PERFIL')
              FROM brasil_saas.bc_core_usuario u
             WHERE u.username = '$ALVO'
        " 2>/dev/null || echo "    (erro ao ler)"
    done
    [ "$ACHOU" = "0" ] && echo "  $ALVO: nao achei em nenhum banco desta maquina"
done

# ------------------------------------------------------------- copia
echo
echo "==================================================================="
echo "  Copiar os usuarios para o banco '$BANCO_ALVO'?"
echo "  A senha sera a que o usuario ja tem. Se ele foi criado por aqui e"
echo "  ninguem sabe a senha, use a de emergencia depois."
echo "==================================================================="
read -r -p "  Copiar? (digite NAO para sair): " RESPOSTA
case "$RESPOSTA" in
    NAO|nao|Nao|"") echo "Saindo sem mexer."; exit 0 ;;
esac

# A senha tem que ser a do bcrypt que o ERP entende. O hash que veio do outro
# banco serve, se os dois rodaram o mesmo BCrypt. Se nao servirem, o login
# recusa e o sintoma volta a ser "criei e nao entra" — entao a copia avisa.
for ALVO in $ALVOS; do
    for BANCO in $(psql -d postgres -Atc "SELECT datname FROM pg_database WHERE datistemplate = false" 2>/dev/null); do
        [ "$BANCO" = "$BANCO_ALVO" ] && continue
        EXISTE=$(psql -d "$BANCO" -Atc \
            "SELECT count(*) FROM brasil_saas.bc_core_usuario WHERE username='$ALVO'" 2>/dev/null \
            | grep -E '^[0-9]+$' || echo 0)
        [ "$EXISTE" = "0" ] && continue

        JA_EXISTE=$(psql -d "$BANCO_ALVO" -Atc \
            "SELECT count(*) FROM brasil_saas.bc_core_usuario WHERE username='$ALVO'" 2>/dev/null \
            | grep -E '^[0-9]+$' || echo 0)
        if [ "$JA_EXISTE" != "0" ]; then
            echo "  $ALVO ja esta em $BANCO_ALVO. Nada a fazer."
            continue
        fi

        echo
        echo "  copiando $ALVO de '$BANCO' para '$BANCO_ALVO'"

        # O INSERT e' gerado no banco de origem, como texto, e executado no de
        # destino. Sem dblink: dblink exige a extensao instalada nos dois
        # lados e, quando falta, o erro aparece no meio da transacao com o
        # banco ja aberto.
        #
        # %L em TUDO, inclusive no booleano e no id.
        #
        # No texto %L e' obrigatorio: o hash bcrypt tem '$' e barras, e o nome
        # pode ter apostrofo. No booleano, %L sai como 't' e 'f' entre aspas —
        # e isso funciona, o Postgres converte para boolean de verdade (testado:
        # pg_typeof devolve boolean, valor true).
        #
        # %s no booleano parece mais limpo e NAO funciona: sai `t` sem aspas, e
        # o Postgres le `t` como nome de coluna. Da em
        # "ERROR: column \"t\" does not exist" no meio da copia.
        SQL_GERADO=$(psql -d "$BANCO" -Atc "
            SELECT format(
                'INSERT INTO brasil_saas.bc_core_usuario (nome, username, email, senha_hash, ativo, mfa_habilitado, tentativas_login, empresa_id, created_at) VALUES (%L, %L, %L, %L, %L, %L, 0, %L, now())',
                u.nome, u.username, u.email, u.senha_hash,
                coalesce(u.ativo, true), coalesce(u.mfa_habilitado, false),
                coalesce(u.empresa_id, 1))
              FROM brasil_saas.bc_core_usuario u
             WHERE u.username = '$ALVO'
        " 2>/dev/null || true)

        if [ -z "$SQL_GERADO" ]; then
            echo "    FALHOU: nao consegui gerar o INSERT. Nada foi mudado."
            continue
        fi

        # A empresa precisa existir no destino. Se nao existir, o INSERT cai na
        # chave estrangeira e o usuario nao entra, que e' exatamente o sintoma
        # que estamos aqui para resolver. A empresa vem numa consulta a parte,
        # e nao por posicao no texto: depender da posicao quebra na primeira
        # vez que o INSERT ganhar uma coluna nova.
        EMPRESA=$(psql -d "$BANCO" -Atc \
            "SELECT coalesce(empresa_id, 1) FROM brasil_saas.bc_core_usuario WHERE username = '$ALVO'" \
            2>/dev/null | grep -E '^[0-9]+$' || echo 1)
        TEM_EMPRESA=$(psql -d "$BANCO_ALVO" -Atc \
            "SELECT count(*) FROM brasil_saas.bc_core_empresa WHERE id = $EMPRESA" 2>/dev/null \
            | grep -E '^[0-9]+$' || echo 0)
        if [ "$TEM_EMPRESA" = "0" ]; then
            echo "    a empresa $EMPRESA nao existe em $BANCO_ALVO; usando a 1"
            SQL_GERADO="${SQL_GERADO%*, now())}"
            SQL_GERADO="${SQL_GERADO}, 1, now())"
        fi

        if printf '%s' "$SQL_GERADO" | psql -d "$BANCO_ALVO" -v ON_ERROR_STOP=1 -q 2>/tmp/ins.err; then
            echo "    ok"
        else
            echo "    FALHOU:"
            sed 's/^/      /' /tmp/ins.err >&2
        fi
    done
done

echo
echo "Se o login recusar a senha depois disso, o hash veio de um BCrypt"
echo "diferente. Resolva trocando a senha pelo proprio ERP (a rota nova"
echo "PUT /api/superadmin/usuarios/{id}/senha) ou por este comando:"
echo
echo "  psql -d $BANCO_ALVO -c \"UPDATE brasil_saas.bc_core_usuario"
echo "    SET senha_hash = '<hash bcrypt novo>' WHERE username = '$ALVO'\""
