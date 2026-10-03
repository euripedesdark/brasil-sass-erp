# Restaurar o Brasil SaaS ERP em outra máquina

**SrvCloud Soluções — Euripedes Batista de Paiva Junior**
Exportado em 2026-09-27 11:27:35 · Postgres 18.6 (Ubuntu 18.6-3build2) · banco `brasil-saas`

## Arquivos

| Arquivo | Conteúdo |
|---|---|
| `parte_dump_01.zip` … `parte_dump_04.zip` | dados do Postgres, divididos |
| `mongo_brasil-saas.zip` | imagens e documentos (MongoDB) |
| `schema_public.zip` | schema do Postgres, **sem dados** |

## Pré-requisitos

- Postgres **18** ou superior (o dump é do 18.6)
- MongoDB 7 ou superior
- schema `brasil_saas` criado antes de restaurar
- role `sa` com permissão de criação no schema

## 1. Banco de dados vazio

```bash
sudo -u postgres createuser -s sa
sudo -u postgres psql -c "CREATE SCHEMA brasil_saas AUTHORIZATION sa;"
createdb -O sa brasil-saas
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
unzip -q mongo_brasil-saas.zip -d /tmp/mongo_restaura
mongorestore --uri "mongodb://USUARIO:SENHA@localhost:27017" \\
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
psql -c "select count(*) from brasil_saas.bc_core_usuario;"
mongosh --eval "db.imagens.countDocuments()"
curl localhost:8081/actuator/health
```
