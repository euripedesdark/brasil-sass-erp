---
id: 2026-09-29-colisao-migration-v108
status: resolvido
data: 2026-09-29
severidade: alta
---

# INC-001: Duas IAs escolheram o mesmo número de migration

## O QUE ACONTECEU

Enquanto a Space Bunny empacotava o ERP, outra IA criou
`V108__auth_source.sql`. O arquivo apareceu primeiro em `target/classes` — resíduo
do build dela — e entrou no jar da Space Bunny.

No boot seguinte, o Flyway recusou subir:

```
org.flywaydb.core.api.FlywayException: Found more than one migration with version 108
```

O ERP entrou em **loop de restart**. Duas tentativas de reinício falharam com
`Unable to start web server` — a porta 8080 ainda estava em disputa com o processo
anterior — e foi preciso parar o serviço, esperar, e subir limpo.

## POR QUE FOI GRAVE

O V108 da Space Bunny **já estava aplicado no banco**:

```
108 = regra tributaria ipi oficial
109 = busca inteligente fiscal
110 = assistente erp auditoria
```

Ou seja: a outra IA não conseguia usar o número 108 de jeito nenhum. E o ERP é
produção — a indisponibilidade foi de minutos, e o mecanismo de
`Scheduled restart` significa que ela se repetiria sozinho.

## COMO FOI RESOLVIDO

A outra IA renumerou a dela para `V111`, e o ERP voltou. O comportamento foi o
certo dos dois lados: as duas IAs perceberam o número ocupado em vez de
insistir.

## POR QUE ACONTECEU

**Número de migration é espaço global, e ninguém estava coordenar nele.**

O `ia-tarefa` resolve isso para *trabalho*: quem está fazendo, declara. Não existe
o equivalente para *numeração de migration*, que é sequencial e compartilhado.
Duas IAs que implementam coisas diferentes no mesmo repositório chegam a V108 ao
mesmo tempo, porque cada uma conta a partir do que vê.

O detalhe que agravou: a migration da outra IA estava apenas em
`target/classes`, não em `src/main/resources/db/migration/`. Ela foi parar no
jar por resíduo de build, e o Flyway viu duas. Se tivesse ficado no fonte, o
conflito apareceria no commit, que é mais fácil de ver.

## ESTRATÉGIA DE COORDENAÇÃO

Duas regras, e a segunda é a que resolve:

**1. Antes de criar uma migration, confirmar o número no banco.**

```bash
sudo -u postgres psql -d brasil-saas -c \
  "select max(version::int) from brasil_saas.flyway_schema_history;"
```

O maior aplicado é o próximo livre. Consultar o banco e não a pasta vale mais,
porque **o banco é a verdade** — uma migration pode estar no fonte e ainda não
aplicada, e uma pode estar aplicada e ter sumido do fonte.

**2. Se o Flyway reclamar de versão duplicada, parar e renumerar — não apagar.**

Apagar o arquivo do outro e "resolver" apaga trabalho de outra IA. Renumerar é
barato: a migration ainda não rodou, então só o número muda.

## COMO EVITAR O CASO DO JAR

`mvn clean` antes de empacotar, sempre. Resíduo de build em `target/classes` entra
no jar e vira problema em produção. Foi o que transformou um conflito de número
em indisponibilidade.

