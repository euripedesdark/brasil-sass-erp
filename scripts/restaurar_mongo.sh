#!/usr/bin/env bash
# =============================================================================
# Restaurar o MongoDB a partir do zip que esta no repositorio
# =============================================================================
# POR QUE ESTE SCRIPT EXISTE
#
# O dump do Mongo esta versionado (`mongo_<db>.zip`, 19 MB) e o exportador o
# gera, mas nao havia NADA que o restaurasse. O installbase.sh instalava o banco
# do Postgres e nao tocava no Mongo, entao numa maquina nova o ERP subia e
# ficava logged:
#
#     org.mongodb.driver.cluster - Waiting for server to become available for
#     operation with server selection: ...
#
# indefinidamente. O Tomcat subia mesmo assim, o que faz o problema parecer
# menor do que e' — o log manda "waiting" e nao "failed", e o sintoma no uso e'
# dado que sumiu, nao o ERP fora.
#
# O FORMATO
#
# O exportador faz:
#
#     mongodump --uri="$MURI" --db="${MONGODB_DB}" --out="$TMP/mongo" --gzip
#     ( cd "$TMP/mongo" && zip -qr "$RAIZ/mongo_${MONGODB_DB}.zip" "${MONGODB_DB}" )
#
# Entao dentro do zip ha um diretorio com o nome do banco, contendo
# `<colecao>.metadata.json` e `<colecao>.bson.gz`. O `--gzip` e' do mongodump:
# os .bson estao comprimidos, e o mongorestore precisa do `--gzip` para ler.
# Sem ele, o erro e' "invalid BSON size" — que fala de arquivo corrompido e e'
# falta da opcao.
#
# DIFERENTE DO restore DO POSTGRES
#
# O Postgres se restaura com `sa` por certificado, e o dump traz o nome do banco
# dentro dele. O Mongo e' diferente: o `--db` do mongodump NAO cria o banco, e o
# mongorestore aceita `--db`/`--nsFrom`/`--nsTo` para escolher o destino. Como
# aqui o dump e' do banco `brasil-saas` e o servidor nao exige autenticacao, o
# destino e' o mesmo nome, e a conferencia e' por collection.
# =============================================================================
set -euo pipefail

PROJETO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TRABALHO="${BRASIL_SAAS_MONGO_DIR:-/tmp/brasil_saas_mongo}"

# O nome do banco esta no nome do arquivo. Deducao de nome e' melhor que
# parametro obrigatorio: quem restaura nao deveria ter que saber o nome, e o
# nome errado nao e' detectado pelo script — ele apenas nao acha o dump.
BANCO="${1:-}"
if [ -z "$BANCO" ]; then
    ZIP_MONGODB="$(ls -1 "$PROJETO"/mongo_*.zip 2>/dev/null | head -1 || true)"
    if [ -z "$ZIP_MONGODB" ]; then
        echo "ERRO: nenhum mongo_*.zip em $PROJETO."
        echo "      O dump nao esta no repositorio. Gere com:  ./exportar_dump.sh"
        exit 1
    fi
    BANCO="$(basename "$ZIP_MONGODB" .zip)"
    BANCO="${BANCO#mongo_}"
else
    ZIP_MONGODB="$PROJETO/mongo_${BANCO}.zip"
fi

echo
echo "==============================================================="
echo " MongoDB: $BANCO"
echo " dump:    $(basename "$ZIP_MONGODB")"
echo " projeto: $PROJETO"
echo " trabalho: $TRABALHO"
echo "==============================================================="
echo

if [ ! -f "$ZIP_MONGODB" ]; then
    echo "ERRO: dump nao encontrado: $ZIP_MONGODB"
    exit 1
fi

# ------------------------------------------------------------------ #
# 1. O SERVIDOR ESTA DE PE?
# ------------------------------------------------------------------ #
# Confere ANTES de desembrulhar 19 MB. Sem mongod nao ha o que restaurar, e o
# erro do mongorestore nesse caso ("connect ECONNREFUSED 127.0.0.1:27017") e'
# direto, mas chega depois de todo o trabalho de descompactar.
echo "[1/4] o servidor esta de pe?"
if ! pgrep -x mongod >/dev/null 2>&1; then
    echo "    NAO: nao achei o processo mongod."
    echo "    Sem servidor nao ha o que restaurar. No erp:"
    echo "      sudo systemctl enable --now mongod"
    exit 1
fi
command -v mongorestore >/dev/null 2>&1 || {
    echo "    NAO: o binario mongorestore nao esta instalado."
    echo "    Ele vem no pacote mongodb-database-tools:"
    echo "      sudo apt-get install -y mongodb-database-tools"
    exit 1
}
echo "    ok (mongod rodando, mongorestore em $(command -v mongorestore))"
echo

# ------------------------------------------------------------------ #
# 2. O QUE JA EXISTE
# ------------------------------------------------------------------ #
# Um --drop no restore apaga o que tem. Isso e' o esperado ao restaurar um dump,
# mas a saida "0 collections" seguido de erro e' o caso que custa tempo: e' bom
# dizer o que vai sumir.
echo "[2/4] o que ja existe em '$BANCO'?"
JA_EXISTE=0
if command -v mongosh >/dev/null 2>&1; then
    JA_EXISTE="$(mongosh --quiet --eval "db.getSiblingDB('$BANCO').getCollectionNames().length" 2>/dev/null || echo 0)"
fi
echo "    $JA_EXISTE collection(s). O --drop vai substitui-las."
echo

# ------------------------------------------------------------------ #
# 3. DESEMBRULHAR
# ------------------------------------------------------------------ #
echo "[3/4] descompactando o dump"
mkdir -p "$TRABALHO"
# O --delete evita a CLASSICA suja: rodar duas vezes e a segunda juntar os
# arquivos da primeira com os da segunda, e o mongorestore restaurar cada
# colecao duas vezes.
rm -rf "${TRABALHO:?}/"* 2>/dev/null || true
if ! unzip -q -o "$ZIP_MONGODB" -d "$TRABALHO"; then
    echo "ERRO: falha ao extrair $ZIP_MONGODB"
    exit 1
fi
DIR_DUMP="$TRABALHO/$BANCO"
if [ ! -d "$DIR_DUMP" ]; then
    # O zip pode ter o diretorio com o nome que o mongodump usou, que nem
    # sempre bate com o nome do arquivo. Achar em vez de supor.
    DIR_DUMP="$(find "$TRABALHO" -maxdepth 2 -type d -name '*.bson*' -printf '%h\n' 2>/dev/null | head -1)"
    [ -n "$DIR_DUMP" ] || DIR_DUMP="$(find "$TRABALHO" -maxdepth 1 -mindepth 1 -type d | head -1)"
fi
if [ -z "$DIR_DUMP" ] || [ ! -d "$DIR_DUMP" ]; then
    echo "ERRO: o zip foi extraido, mas nao achei a pasta do dump dentro dele."
    echo "      conteudo: $(ls -1 "$TRABALHO" 2>/dev/null | tr '\n' ' ')"
    exit 1
fi
N_COLS="$(find "$DIR_DUMP" -name '*.bson*' | wc -l)"
# O metadata vem COMPRESSADO tambem (`<coll>.metadata.json.gz`), entao contar
# `*.metadata.json` da zero numa pasta que tem os dois. E' o que fez a primeira
# versao deste script anunciar "0 metadata" com 4 collections esperando.
N_META="$(find "$DIR_DUMP" -name '*.metadata.json*' | wc -l)"
echo "    $N_COLS arquivo(s) de dados, $N_META metadata"
if [ "$N_COLS" -eq 0 ]; then
    echo "ERRO: o dump nao tem nenhum .bson. O zip veio truncado ou corrompido?"
    exit 1
fi
echo

# ------------------------------------------------------------------ #
# 4. RESTAURAR
# ------------------------------------------------------------------ #
echo "[4/4] restaurando"
# O DIRETORIO QUE SE PASSA E' O RAIZ, E OS ARQUIVOS VAO DESCOMPRIMIDOS.
#
# Duas coisas, e as duas erradas da primeira vez:
#
# 1. `mongorestore --gzip <pasta>` NESTA VERSAO nao reconhece `.bson.gz` em modo
#    diretorio, e responde:
#
#        don't know what to do with file `.../documentos.bson.gz`, skipping...
#        0 document(s) restored successfully. 0 document(s) failed to restore.
#
#    Repare no "0 restored" com "0 failed" ao lado: nao e' que deu erro, e' que
#    ele nao olhou arquivo nenhum. E' o tipo de saida que parece sucesso.
#
# 2. O diretorio a passar e' o RAIZ, que contem a pasta com o nome do banco — o
#    mongorestore le o NOME do subdiretorio para montar o namespace. Passando a
#    pasta interna, ele nao acha onde esta o banco e nao restaura nada.
#
# Entao: descomprime no lugar, e passa o raiz.
#
#     /tmp/brasil_saas_mongo/
#       brasil-saas/
#         documentos.bson        <- aqui
#         documentos.metadata.json
#
# O --drop torna a operacao repetivel, que e' o que se precisa ao rodar o
# instalador de novo.
for gz in "$DIR_DUMP"/*.gz; do
    [ -f "$gz" ] || continue
    gunzip -c "$gz" > "$DIR_DUMP/$(basename "$gz" .gz)" || {
        echo "ERRO: nao consegui descomprimir $gz"
        exit 1
    }
    rm -f "$gz"
done

# O prelude.json e' artefato do mongos (cluster sharded); numa instancia
# standalone ele nao serve e o mongorestore o ignora com um aviso. Sumi-lo com o
# resto nao faz mal nenhum.
rm -f "$DIR_DUMP/prelude.json" 2>/dev/null || true

RAIZ_DUMP="$(dirname "$DIR_DUMP")"
echo "    restaurando de $RAIZ_DUMP (o mongorestore le o nome de '$BANCO')"
if mongorestore --drop "$RAIZ_DUMP" 2>&1 | tail -5 | sed 's/^/    /'; then
    :
else
    echo "ERRO: o mongorestore falhou. O dump continua em $TRABALHO e pode ser"
    echo "      tentado de novo."
    exit 1
fi
echo

# ------------------------------------------------------------------ #
# CONFERENCIA
# ------------------------------------------------------------------ #
# A saida do mongorestore conta o que ele processou, e ele conta antes de
# gravar. O que decide e a collection existir DEPOIS.
echo
echo "conferindo"
if command -v mongosh >/dev/null 2>&1; then
    DEPOIS="$(mongosh --quiet --eval "db.getSiblingDB('$BANCO').getCollectionNames().length" 2>/dev/null || echo 0)"
    echo "    collections em '$BANCO': $DEPOIS"
    if [ "${DEPOIS:-0}" -lt 1 ]; then
        echo "ERRO: apos o restore o banco '$BANCO' ficou sem collection nenhuma."
        echo "      O dump continua em $TRABALHO/$BANCO"
        exit 1
    fi
    # Documentos de uma collection qualquer, para mostrar que o conteudo veio.
    PRIMEIRA="$(mongosh --quiet --eval "db.getSiblingDB('$BANCO').getCollectionNames()[0]" 2>/dev/null || echo '')"
    if [ -n "$PRIMEIRA" ]; then
        DOCS="$(mongosh --quiet --eval "db.getSiblingDB('$BANCO').getCollection('$PRIMEIRA').countDocuments({})" 2>/dev/null || echo '?')"
        echo "    '$PRIMEIRA': $DOCS documento(s)"
    fi
else
    echo "    (sem mongosh, nao da para conferir; instale mongodb-mongosh)"
fi
echo
echo "MongoDB '$BANCO' restaurado."
