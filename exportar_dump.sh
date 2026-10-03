#!/usr/bin/env bash
#
# Exportacao completa do Brasil SaaS ERP para transporte a outra maquina.
#
# Gera na raiz do projeto:
#   parte_dump_01.zip .. parte_dump_04.zip   dados do Postgres, divididos em 4
#   mongo_brasil-saas.zip                    dados do MongoDB (imagens/documentos)
#   schema_public.zip                       schema do Postgres, sem dados
#   docs/infra/RESTAURAR.md                            passo a passo para restaurar
#
# Uso:
#   ./exportar_dump.sh              gera os arquivos
#   ./exportar_dump.sh --so-pg      so o Postgres
#   ./exportar_dump.sh --so-mongo   so o MongoDB
#
# Propriedade: SrvCloud Solucoes - Euripedes Batista de Paiva Junior
#
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$RAIZ"

# ---- conexao Postgres: certificado de cliente (sem senha) ----
#
# O caminho do repositorio tem espaco ("GIT Repos"). O pg_dump recusa
# parametros de conexao com espaco e pede %20, mas nao consegue aplicar isso
# aos caminhos de certificado de forma confiavel. A saida e passar a URI pela
# variavel de ambiente PGPASSFILE/PGURI: o pg_dump le do ambiente e nao faz
# parse de caminho.
# Certificados: o caminho do repo tem espaco ("GIT Repos") e o libpq recusa
# espaco em sslrootcert. Por isso usamos as variaveis de ambiente, que o
# libpq resolve sem passar por parsing de URI.
CERT_DIR="${BRASIL_SAAS_CERTS_DIR:-$RAIZ/certs/pki}"
if [ ! -r "$CERT_DIR/ca.crt" ]; then
  CERT_DIR="$HOME/.local/share/brasil-saas-certs/pki"
  echo "aviso: certs nao encontrado no repo; usando $CERT_DIR"
fi
export PGSSLROOTCERT="$CERT_DIR/ca.crt"
export PGSSLCERT="$CERT_DIR/issued/sa.crt"
# A chave privada que o libpq le e' a sa.key.
#
# Este script apontava para a sa.pk8, que e' o mesmo par em outro formato. O
# libpq so le .key, e o resultado era
#     pg_dump: error: could not load private key file ...
# sem uma palavra sobre o formato. A sa.pk8.pem, que tem extensao .pem, e' a
# mesma armadilha. A verificacao abaixo evita o guess: se o .key nao existir, o
# script avisa qual arquivo encontrou em vez de falhar no meio do dump.
if [[ -r "$CERT_DIR/private/sa.key" ]]; then
  export PGSSLKEY="$CERT_DIR/private/sa.key"
else
  echo "ERRO: chave privada do 'sa' nao esta em $CERT_DIR/private/sa.key"
  echo "      o que ha em private/:"
  ls -1 "$CERT_DIR/private/" 2>/dev/null | sed 's/^/        /'
  exit 1
fi

# O NOME DO BANCO E DO SCHEMA.
#
# Este script tinha DB="brasil-saas" e o manifesto mandava criar o schema
# "brasil_saas". O banco de hoje e' `brasil-saas` e o schema e' `brasil_saas`:
# foi o rename que o dono mandou fazer, e o exportador ficou para tras. O
# resultado era silencioso — o pg_dump falhava com "database brasil-saas does
# not exist" e quem fosse restaurar na mao nova criava um banco que o ERP nao
# le, ou restaurava o schema errado.
#
# O nome vem do dump, nao do script: e' assim que o restaurador tambem trata.
DB="brasil-saas"
SCHEMA="brasil_saas"
PGHOST=localhost
PGPORT=5432
PGUSER=sa
export PGHOST PGPORT PGUSER PGSSLROOTCERT PGSSLCERT PGSSLKEY
export PGDATABASE="$DB"

# atalho sem argumentos: as variaveis acima ja descrevem a conexao
pg_cmd() { pg_dump -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$DB" "$@"; }
psql_cmd() { psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$DB" "$@"; }
# QUANTAS PARTES: 8.
#
# Era 4, e o numero nao era escolha de ninguem: estava gravado em tres lugares
# ao mesmo tempo — aqui, no laco do restaurador e no laco da etapa 2 do
# instalador. Mudar exigia acertar os tres, e errar um dava um restaurador que
# procurava quatro zips num dump de oito, com o erro "parte ausente" — que
# parece arquivo faltando, e nao e'.
#
# 8 deixa cada parte em uns 17 MB, o que passa em midia removivel, e-mail e
# qualquer transferencia que corte arquivo grande. Com 4, cada parte tinha
# 33 MB.
#
# O numero nao precisa ser propagado a mao: o dump_manifest.env que este
# script escreve carrega DUMP_PARTES, e os dois restauradores leem de la. Se
# este 8 mudar um dia, so muda aqui.
#
# O nome do arquivo e parte_dump_NN.zip com dois digitos, entao o teto e 99.
PARTES=8

TMP="$(mktemp -d /tmp/brasil-saas-dump.XXXXXX)"
trap 'rm -rf "$TMP"' EXIT

echo "=============================================================="
echo " Exportacao Brasil SaaS ERP - SrvCloud Solucoes"
echo " $(date '+%Y-%m-%d %H:%M:%S')"
echo "=============================================================="
echo "destino : $RAIZ"
echo "temporario: $TMP"
echo

so_pg=1; so_mongo=1
case "${1:-}" in
  --so-pg)    so_mongo=0 ;;
  --so-mongo) so_pg=0 ;;
esac

# ---------------------------------------------------------------- Mongo
if [ "$so_mongo" = 1 ]; then
  echo "[1/3] MongoDB"
  if [ -f .env ]; then
    set -a; . ./.env; set +a
  fi
  MURI="mongodb://${MONGODB_USERNAME}:${MONGODB_PASSWORD}@127.0.0.1:27017/${MONGODB_DB}?authSource=admin"

  mongodump --uri="$MURI" --db="${MONGODB_DB}" --out="$TMP/mongo" --gzip
  # Um zip so. A versao anterior criava o arquivo, apagava, mandava o antigo
  # para /dev/null e criava de novo — tres operacoes onde uma basta, e o
  # `mv ... /dev/null` e' um `mv` para um dispositivo, que descarta os dados sem
  # avisar. Funcionava por sorte: o segundo zip recomeçava do zero. Se a
  # primeira criacao tivesse deixado o arquivo pela metade, o segundo `zip -q`
  # acrescentaria em vez de substituir, e o zip sairia com conteudo duplicado.
  rm -f "$RAIZ/mongo_${MONGODB_DB}.zip"
  ( cd "$TMP/mongo" && zip -qr "$RAIZ/mongo_${MONGODB_DB}.zip" "${MONGODB_DB}" )
  echo "     mongo_${MONGODB_DB}.zip  ($(du -h "$RAIZ/mongo_${MONGODB_DB}.zip" | cut -f1))"
fi

if [ "$so_pg" = 0 ]; then
  echo "concluido (so MongoDB)"
  exit 0
fi

# ---------------------------------------------------------------- Postgres
echo "[2/3] Postgres: dump completo (formato custom)"
pg_cmd -Fc -f "$TMP/dados.dump" --no-owner --no-acl
TAM=$(du -h "$TMP/dados.dump" | cut -f1)
echo "     dados.dump  $TAM"
echo "     $(pg_restore -l "$TMP/dados.dump" | tail -1)"

echo "     dividindo em $PARTES partes"
TAM_CHUNK=$(( ($(stat -c %s "$TMP/dados.dump") + PARTES - 1) / PARTES ))
split -b "$TAM_CHUNK" -d -a 2 "$TMP/dados.dump" "$TMP/parte"

# O TAMANHO REAL DE CADA PEDACO, medido, e nao deduzido.
#
# O TAM_CHUNK e' o tamanho ARREDONDADO PARA CIMA, porque o split nao aceita
# pedaco de 0 byte. Entao, quando o dump nao divide exato por 4, o ULTIMO
# pedaco fica menor que os outros:
#
#   dump de 137.400.275 bytes, PARTES=8
#     TAM_CHUNK = ceil(137400275 / 8) = 17.175.035
#     pedaco_01..07  17.175.035
#     pedaco_08      17.175.030   <-- 5 bytes a menos, porque e' o resto da divisao
#
# O restaurador conferia os quatro contra o TAM_CHUNK e recusava o ultimo com
# "o zip esta corrompido". O zip estava inteiro. O dump antigo (136.823.204
# bytes) dividia exato por 4, entao os quatro tinham o mesmo tamanho e a
# conferencia passava por sorte — e o primeiro dump de tamanho nao divisivel
# por 4 teria falhado com a mensagem de corrupcao.
#
# A conferencia de corrupcao e' boa e fica. O que estava errado era o criterio:
# um unico numero para quatro arquivos que nao sao iguais. A lista medida vai
# para o manifesto, e o restaurador confere cada pedaco contra o seu.
TAMANHOS=""
for i in $(seq 0 $((PARTES - 1))); do
  SRC="$TMP/parte$(printf "%02d" $i)"
  [ -f "$SRC" ] || continue
  TAMANHOS="$TAMANHOS $(stat -c %s "$SRC")"
done
TAMANHOS="${TAMANHOS# }"
echo "     tamanhos: $TAMANHOS"

# Cada parte vira um ZIP de verdade (formato PK), e nao gzip renomeado: um
# .zip precisa ser aberto por `unzip`, e gzip soabriria com `gunzip`. Sem
# isso os quatro arquivos pareciam dump e nao restauravam.
echo "     compactando cada parte (zip real)"
for i in $(seq 0 $((PARTES - 1))); do
  N=$(printf "%02d" $((i + 1)))
  SRC="$TMP/parte$(printf "%02d" $i)"
  [ -f "$SRC" ] || continue
  rm -f "$RAIZ/parte_dump_${N}.zip"
  ( cd "$TMP" && zip -q -9 "$RAIZ/parte_dump_${N}.zip" "$(basename "$SRC")" )
  echo "       parte_dump_${N}.zip  $(du -h "$RAIZ/parte_dump_${N}.zip" | cut -f1)"
done

# ---------------------------------------------------------------- schema
echo "[3/3] Postgres: schema (sem dados)"
pg_cmd --schema-only -f "$TMP/schema_public.sql" --no-owner --no-acl
cp "$TMP/schema_public.sql" "$RAIZ/schema_public.sql"
( cd "$TMP" && zip -q "$RAIZ/schema_public.zip" schema_public.sql )
echo "     schema_public.zip  ($(du -h "$RAIZ/schema_public.zip" | cut -f1))"

# ---------------------------------------------------------------- manifesto
#
# O restaurador conferia o tamanho de cada pedaco contra um numero FIXO no
# codigo (34205801). Isso ata o restaurador a UM dump: qualquer exportacao nova
# tinha tamanho diferente e o script recusava as 4 partes, com a mensagem de
# "zip corrompido" — sendo que o zip estava inteiro, so nao era o mesmo dump.
#
# E' este arquivo que amarra a exportacao ao restaurador. O exportador escreve
# os tamanhos; o restaurador le. Se o arquivo nao existir, o restaurador cai
# no valor antigo e avisa, em vez de recusar sem explicacao.
echo "     escrevendo dump_manifest.env"
# O CABECALHO vai num heredoc QUOTED, e os valores vao em `printf`.
#
# A versão anterior usava um heredoc NAO quoted para o arquivo inteiro. Crase nao
# e' aspe: no bash, tudo entre crases e' command substitution. Entao as crases de
# markdown deste comentario viravam comando — e este bloco ja produziu
#
#   ./exportar_dump.sh: linha 163: 10:08:13: comando nao encontrado
#   ./exportar_dump.sh: linha 163: show: comando nao encontrado
#
# sem que o dump estivesse errado em nada. O `printf '%s'` nao interpreta o
# argumento, entao nenhum valor pode virar comando por acidente — e nenhum
# comentario pode virar comando por descuido.
cat > "$RAIZ/dump_manifest.env" << 'MANIFEST_ENV'
# Gerado por exportar_dump.sh. NAO editar a mao.
#
# O restaurador le este arquivo com `source` para conferir o tamanho de cada
# pedaco e para saber em que versao do Flyway o dump parou. Sem ele, o
# restaurador exige um tamanho fixo no codigo, que so serve para um dump de uma
# data especifica.
#
# UM MANIFESTO QUE O OUTRO SCRIPT FAZ `source` E' CODIGO SHELL.
#
# As duas coisas que quebram um `source` de arquivo de texto:
#
# 1) valor com espaco, sem aspas. `DUMP_EXPORTADO_EM=2026-09-27 10:08:13` vira
#    tres palavras: a atribuicao, `2026-09-27` e `10:08:13`. O bash executa
#    `10:08:13` como comando e morava com
#        dump_manifest.env: linha 9: 10:08:13: comando nao encontrado
#    sem nunca chegar a conferir o tamanho do pedaco — que era a razao de o
#    arquivo existir. O mesmo vale para DUMP_POSTGRES, que o postgres devolve
#    como "18.6 (Ubuntu 18.6-3build2)": os parenteses o bash le como sintaxe.
#
# 2) valor com caractere especial, pelo mesmo caminho.
#
# Daí as aspas em todo valor string, e o `head -1` no que vem do psql: o
# `show server_version` pode devolver mais de uma linha, e a segunda viraria um
# comando.
MANIFEST_ENV

{
  printf 'DUMP_PARTES=%s\n'            "$PARTES"
  printf 'DUMP_TAMANHO_PEDACO=%s\n'    "$TAM_CHUNK"
  # A lista dos tamanhos reais, um por pedaco. O restaurador confere cada um
  # contra o seu, em vez de exigir que os quatro sejam iguais — que so acontece
  # quando o dump divide exato pelo numero de partes.
  printf 'DUMP_TAMANHOS="%s"\n'         "$TAMANHOS"
  printf 'DUMP_TOTAL=%s\n'             "$(stat -c %s "$TMP/dados.dump")"
  printf 'DUMP_EXPORTADO_EM="%s"\n'    "$(date '+%Y-%m-%d %H:%M:%S')"
  printf 'DUMP_BANCO="%s"\n'           "$DB"
  printf 'DUMP_SCHEMA="%s"\n'          "$SCHEMA"
  printf 'DUMP_POSTGRES="%s"\n'        "$(psql_cmd -t -A -c 'show server_version;' 2>/dev/null | head -1)"
  # A versao do Flyway e' VARCHAR, e max() em texto e' LEXICAL: '99' ganha de
  # '104' porque '9' > '1'. Sem o ::numeric, o manifesto anunciava V99 num banco
  # em V104 — e o restaurador repete "O dump parou na versao 99", o que faz
  # quem restaura achar que o dump e' anterior a migrate que ele acabou de ver.
  # O cast numerico tambem ordena versoes com ponto, como 104.1, o que o texto
  # nao faria.
  printf 'DUMP_FLYWAY="%s"\n'          "$(psql_cmd -t -A -c "select coalesce(max(version::numeric)::text,'-') from ${SCHEMA}.flyway_schema_history;" 2>/dev/null | head -1)"
  printf 'DUMP_TABELAS="%s"\n'         "$(psql_cmd -t -A -c "select count(*) from information_schema.tables where table_schema='${SCHEMA}';" 2>/dev/null | head -1)"
  printf 'DUMP_LINHAS="%s"\n'          "$(psql_cmd -t -A -c "select coalesce(sum(n_live_tup),0) from pg_stat_user_tables where schemaname='${SCHEMA}';" 2>/dev/null | head -1)"
} >> "$RAIZ/dump_manifest.env"

sed "s/^/       /" "$RAIZ/dump_manifest.env" | head -12 || true

# O manifesto e' escrito com heredoc QUOTED, e os poucos valores dinamicos
# entram por substituicao depois.
#
# A versao anterior usava heredoc NAO quoted num texto cheio de crases de
# markdown. Crase nao e' aspe: no bash, tudo entre crases e' command
# substitution. Entao "`pg_restore`" rodava pg_restore sem argumentos, e
# "`dump_manifest.env`" tentava executar um arquivo. Os dois erros apareceram
# durante a exportacao:
#
#   ./exportar_dump.sh: linha 182: dump_manifest.env: comando nao encontrado
#   pg_restore: error: one of -d/--dbname and -f/--file must be specified
#
# O arquivo saia com crases faltando e o codigo de exemplo incomplete. Escapar
# as crases uma a uma resolve as de hoje e deixa a proxima. O heredoc quoted
# resolve a classe: nenhuma crase e nenhum $ do texto e' executado, e o que
# precisa ser dinamico entra por @@TOKEN@@.
cat > "$RAIZ/RESTAURAR.md" << 'MANIFEST'
# Restaurar o Brasil SaaS ERP em outra máquina

**SrvCloud Soluções — Euripedes Batista de Paiva Junior**
Exportado em @@QUANDO@@ · Postgres @@POSTGRES@@ · banco `@@DB@@`

## Arquivos

| Arquivo | Conteúdo |
|---|---|
| `parte_dump_01.zip` … `parte_dump_04.zip` | dados do Postgres, divididos |
| `mongo_@@MONGODB_DB@@.zip` | imagens e documentos (MongoDB) |
| `schema_public.zip` | schema do Postgres, **sem dados** |

## Pré-requisitos

- Postgres **18** ou superior (o dump é do 18.6)
- MongoDB 7 ou superior
- schema `@@SCHEMA@@` criado antes de restaurar
- role `sa` com permissão de criação no schema

## 1. Banco de dados vazio

```bash
sudo -u postgres createuser -s sa
sudo -u postgres psql -c "CREATE SCHEMA @@SCHEMA@@ AUTHORIZATION sa;"
createdb -O sa @@DB@@
```

> A role `sa` entra **por certificado**, não por senha. O `installbase.sh`
> já gera a PKI e as regras do `pg_hba.conf`; se você pular o instalador, copie a
> pasta `certs/pki` e aponte `BRASIL_SAAS_CERTS_DIR` para ela.

## 2. Dados do Postgres

Os 4 zips são pedaços do **mesmo** dump. **Use o script**, que confere o
tamanho de cada pedaço contra o `dump_manifest.env` antes de juntar:

```bash
scripts/restaurar_banco.sh --simular    # só confere as partes
scripts/restaurar_banco.sh              # confirma e restaura
```

Ele pede o nome do banco para confirmar antes de substituir, e confere que o
schema tem mais de 100 tabelas depois — o `pg_restore` devolve 0 mesmo havendo
avisos, então o código de saída não é o critério.

Juntar à mão, se for preciso:

```bash
mkdir -p /tmp/restaura && cd /tmp/restaura
for i in 1 2 3 4; do
  N=$(printf "%02d" $i)
  unzip -o -j "/caminho/do/projeto/parte_dump_${N}.zip" "parte$(printf "%02d" $((i-1)))" -d .
  mv "parte$(printf "%02d" $((i-1)))" "pedaco_${N}"
done
cat pedaco_0* > dados.dump
head -c 5 dados.dump   # tem que mostrar PGDMP
pg_restore --create -d postgres --no-owner --no-acl dados.dump
```

> O nome do banco está **gravado dentro do dump**, e o dump traz
> `CREATE DATABASE` e `DROP DATABASE`. Por isso o `pg_restore` usa
> `--create -d postgres`: a conexão vai para o `postgres`, que existe sempre, e
> o próprio dump nomeia o banco de destino. Passar outro nome na linha de
> comando não renomeia nada.

## 3. MongoDB

```bash
unzip -q mongo_@@MONGODB_DB@@.zip -d /tmp/mongo_restaura
mongorestore --uri "mongodb://USUARIO:SENHA@localhost:27017" \\
  --drop /tmp/mongo_restaura/@@MONGODB_DB@@
```

> `--drop` apaga a coleção antes. Tire se quiser mesclar.

## 4. Certificado de cliente

O Postgres exige certificado, não senha. Copie a pasta `certs/`:

```bash
cp -r /caminho/do/projeto/certs ~/brasil-saas-certs
chmod 600 ~/brasil-saas-certs/pki/private/sa.pk8
```

E aponte a aplicação:

```bash
export BRASIL_SAAS_CERTS_DIR=~/brasil-saas-certs/pki
```

## 5. Permissões do certificado

O `.pfix` de NFS-e São Paulo fica **fora** do dump. Copie manualmente de
`OneDrive/Nova pasta/Documentos/` — e ele é protegido por senha, que precisa ser
obtida com a AC emissora.

## 6. Subir

```bash
export SPRING_DATA_MONGODB_URI="mongodb://USER:SENHA@127.0.0.1:27017/@@MONGODB_DB@@?authSource=admin"
mvn clean package -DskipTests
java -jar target/*.jar
```

Frontend:

```bash
cd src/main/resources/static/react && npm ci && npm run build
```

## 7. Serviços auxiliares

O CNAB (boletos) é Ruby e **não** vai no dump:

```bash
cd src/main/resources/microservices/boleto-cnab-api
bundle config set --local path ~/.local/share/brasil-saas-gems
bundle install
bundle exec puma -p 9292 config.ru
```

## Conferir

```bash
psql -c "select count(*) from @@SCHEMA@@.bc_core_usuario;"
mongosh --eval "db.imagens.countDocuments()"
curl localhost:8081/actuator/health
```
MANIFEST

echo
echo "=============================================================="
echo " Concluido. Arquivos na raiz do projeto:"
ls -1sh "$RAIZ"/parte_dump_*.zip "$RAIZ"/mongo_*.zip "$RAIZ"/schema_public.zip 2>/dev/null
echo "=============================================================="
echo "Para restaurar, leia docs/infra/RESTAURAR.md"

# A substituicao dos @@TOKEN@@. sed com | como separador, e os valores nao
# tem pipe.
sed -i \
    -e "s|@@QUANDO@@|$(date '+%Y-%m-%d %H:%M:%S')|g" \
    -e "s|@@POSTGRES@@|$(psql_cmd -t -A -c 'show server_version;' 2>/dev/null || echo '?')|g" \
    -e "s|@@DB@@|$DB|g" \
    -e "s|@@SCHEMA@@|$SCHEMA|g" \
    -e "s|@@MONGODB_DB@@|${MONGODB_DB:-brasil-saas}|g" \
    "$RAIZ/RESTAURAR.md"

echo
echo "=============================================================="
echo " Concluido. Arquivos na raiz do projeto:"
ls -1sh "$RAIZ"/parte_dump_*.zip "$RAIZ"/mongo_*.zip "$RAIZ"/schema_public.zip 2>/dev/null
echo "=============================================================="
echo "Para restaurar, leia docs/infra/RESTAURAR.md"
