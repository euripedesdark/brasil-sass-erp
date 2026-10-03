# Restaurar o Brasil SaaS ERP em outra máquina

**SrvCloud Soluções — Euripedes Batista de Paiva Junior**
Exportado em 2026-09-25 18:02:00 · Postgres 18.6 (Ubuntu 18.6-3build2) · banco `brasil-saas`

## Arquivos

| Arquivo | Conteúdo |
|---|---|
| `parte_dump_01.zip` … `parte_dump_04.zip` | dados do Postgres, divididos |
| `mongo_brasil-saas.zip` | imagens e documentos (MongoDB) |
| `schema_public.zip` | schema do Postgres, **sem dados** |

## Pré-requisitos

- Postgres **18** ou superior (o dump é do 18.6)
- MongoDB 7 ou superior
- schema `brasil-saas` criado antes de restaurar
- role `sa` com permissão de criação no schema

## 1. Banco de dados vazio

```bash
sudo -u postgres createuser -s sa
sudo -u postgres psql -c "CREATE SCHEMA brasil-saas AUTHORIZATION sa;"
createdb -O sa brasil-saas
```

## 2. Dados do Postgres

Os 4 zips são pedaços do **mesmo** dump. Junte antes de restaurar.

**Atenção à numeração:** dentro dos zips os pedaços se chamam `parte00` a
`parte03` — começam em zero. O `01` a `04` é só o nome do zip. A diferença
importa, e já causou erro: um laço que procure `parte01` dentro de
`parte_dump_01.zip` não encontra, porque lá dentro está `parte00`.

Use este script, que faz a junção na ordem e não depende de adivinhar o nome
interno:

```bash
#!/usr/bin/env bash
set -euo pipefail
PROJETO="/caminho/para/o/projeto"
mkdir -p /tmp/restaura && cd /tmp/restaura
rm -f pedaco_* dados.dump
i=1
while [ $i -le 4 ]; do
  N=$(printf "%02d" "$i")
  # Dentro do zip N está o pedaço N-1.
  unzip -o -j "$PROJETO/parte_dump_${N}.zip" "parte$(printf "%02d" $((i-1)))" -d .
  mv "parte$(printf "%02d" $((i-1)))" "$(printf "pedaco_%02d" "$i")"
  i=$((i+1))
done
# Glob ordena por nome: pedaco_01, pedaco_02... a ordem importa, é um dump só.
cat pedaco_* > dados.dump
echo "dados.dump: $(stat -c%s dados.dump) bytes"
pg_restore -d brasil-saas --no-owner --no-acl dados.dump
```

O `cat pedaco_*` sem nome de saída solto o resultado na tela e não cria o
arquivo. E o glob é o que garante a ordem: `pedaco_01` … `pedaco_04` ordenam
lexicograficamente, que é a ordem do dump.

```bash
#!/usr/bin/env bash
mkdir -p /tmp/restaura && cd /tmp/restaura
rm -f pedaco_*
i=1
while [ $i -le 4 ]; do
  N=$(printf "%02d" $i)
  unzip -o -j "/caminho/parte_dump_${N}.zip" "parte$(printf "%02d" $((i-1)))" -d .
  mv "parte$(printf "%02d" $((i-1)))" "$(printf "pedaco_%02d" $i)"
  i=$((i+1))
done
cat pedaco_* > dados.dump
pg_restore -d brasil-saas --no-owner --no-acl dados.dump
```

## 3. MongoDB

```bash
unzip -q mongo_brasil-saas.zip -d /tmp/mongo_restaura
mongorestore --uri "mongodb://USUARIO:SENHA@localhost:27017" \
  --drop /tmp/mongo_restaura/brasil-saas
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
export SPRING_DATA_MONGODB_URI="mongodb://USER:SENHA@127.0.0.1:27017/brasil-saas?authSource=admin"
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
psql -c "select count(*) from brasil-saas.bc_core_usuario;"
mongosh --eval "db.imagens.countDocuments()"
curl localhost:8081/actuator/health
```
