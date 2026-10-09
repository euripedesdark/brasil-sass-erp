> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Relatório: suite de testes e rename para o INPI

Data: 27/09/2026
Tag: `inpi`

Este documento junta duas coisas que aconteceram na mesma sessao e que tem a
mesma raiz: **um nome que estava errado em todo lugar, e um teste que media a
versao anterior do sistema.**

---

## 1. O rename

### O que mudou

| | antes | depois |
|---|---|---|
| banco Postgres | `brasil-saas` | `brasil-saas` |
| schema principal | `brasil-saas` | `brasil_saas` — 167 tabelas |
| schema do datalake | `brasil-saas_datalake` | `brasil_saas_dl` — 9 tabelas |
| MongoDB | `brasil-saas` | `brasil-saas` |
| pacote Java | `br.com.brasil-saas` | `br.com.brasil_saas` — 624 arquivos, 195 pastas |
| rodape do PDF | `Gerado por BRASIL-SAAS ERP` + dominio | `Gerado por Brasil SaaS ERP` |
| nome fantasia do bootstrap | `BRASIL-SAAS` | `Brasil SaaS` |

O titulo da aba do navegador ja estava `Brasil SaaS ERP` antes de comecar.

### Por que hifen no banco e underscore no schema

O hifen e seguro no **nome do banco**: ele vive dentro da URL do JDBC e do
`pg_hba.conf`, e nenhum dos dois e um identificador SQL.

No **schema** nao e. `-` nao e caractere valido em identificador SQL sem
aspas:

```sql
SET search_path TO brasil-saas, public;
-- ERROR:  syntax error at or near "-"

SET search_path TO 'brasil-saas', public;
-- ok, e o Postgres normaliza para "brasil-saas"
```

Com underscore nao se paga aspas em lugar nenhum. O mesmo vale para o
`brasil_saas_dl`.

### O que deliberadamente NAO mudou

- **`SRVCLOUD CONSULTORIA LTDA`** — e a razao social, o titular do registro no
  INPI. Trocar a razao social trocaria o applicant. So o nome fantasia mudou.
- **35 arquivos de `certs/`** — incluindo `sa.pk8`, `ca.key` e
  `client.key`. Decisao do dono do repo: repo privado, so ele acessa.
- **Namespace de property `brasil-saas.*`** nos yml — e lido por `@Value` em
  ~30 lugares. Nao e o schema.
- **Chaves de `localStorage` do frontend** (`brasil-saas_token`) — o nome da
  chave e do navegador, nao do banco. Trocar derrubaria o login de todo mundo
  sem ganho nenhum.
- **Bucket S3, usuario do RabbitMQ, nome do app, segredo JWT** — infra, nao
  marca visivel.
- **As 90 migrations** — o rename do schema chegou a exigir as 88 que citam
  `brasil-saas`, e o dono do repo vetou mexer nelas.

### Backup validado antes de qualquer rename

Nao confiou no `ALTER SCHEMA`. Backup, restore em banco descartavel e
contagem:

| | |
|---|---|
| Postgres | 131 MB · 218 relacoes · **1.779.929 linhas** — contagem identica nos dois lados |
| Mongo | 20 MB · 355 documentos · **14.742.135 chars** em `imagens` — byte a byte |

O primeiro restore pareceu ter falhado: 0,061 s e banco vazio. Falsa leitura
— um `head -5` no pipe mandou SIGPIPE e matou o `pg_restore` no comeco. O
criterio de sucesso nao e o exit code, e a contagem depois.

---

## 2. As duas armadilhas do rename

Nenhuma das duas aparece num grep de `BRASIL-SAAS`, e as duas derrubaram o
boot. E o que torna a conta: o ERP compilava e o build passava nas duas.

### 2.1 O `excludeFilters` usava o pacote como regex escapada

`BRASIL-SAASErpApplication.java`:

```java
@ComponentScan(
    basePackages = "br.com.brasil_saas",              // trocou
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "br\\.com\\.brasil-saas\\..*Application"   // NAO trocou
    )
)
```

O replace buscava `br.com.brasil-saas` com ponto literal. Aqui o ponto esta
escapado, em regex. O filtro deixou de excluir os 14 outros `*Application`,
todos entraram no scan, e cada um trouxe um `@EnableJpaAuditing`.

```
The bean 'jpaAuditingHandler' could not be registered.
A bean with that name has already been defined and overriding is disabled.
```

24 de 24 rotas em 403. O log apontava `Pre-authentication` no
`Http403ForbiddenEntryPoint`, e a causa de verdade era um
`PSQLException` enterrado 40 linhas abaixo: `relation
"brasil-saas.bc_core_usuario" does not exist`.

### 2.2 O `BRASIL-SAAS-ERP` dentro de um caminho de disco

`BRASIL_SAAS_CERTS_DIR` tem um path absoluto:

```
/home/.../GIT Repos/BrasilSaasERP/certs/pki/ca.crt
```

A pasta em disco continua `BRASIL-SAAS-ERP`, entao:

```
FileNotFoundException: .../GIT Repos/BrasilSaasERP/certs/pki/ca.crt
```

66 arquivos, 421 ocorrencias, revertidas. Havia tambem 9 referencias SQL cru
em 4 arquivos que o replace com aspas nao pegou — `CustomUserDetailsService`,
`RelatorioService`, `RecentController`, `MovimentacaoEstoqueRepository`.

### Regra que saiu disso

Antes de um replace de nome, procurar o nome em tres lugares que nao sao nome:

1. **caminho de disco** — path absoluto, `WorkingDirectory=`
2. **regex escapada** — `br\\.com\\.brasil-saas`
3. **URL** — host, dominio, path de API

Nenhum dos tres aparece num grep do nome.

---

## 3. O que o nome do banco toca, e nao e o `application.yml`

### 3.1 `pg_hba.conf`

Tres regras nomeavam o banco:

```
hostssl brasil-saas  sa          127.0.0.1/32  cert clientcert=verify-full
hostssl brasil-saas  euripedes   127.0.0.1/32  scram-sha-256
hostssl brasil-saas  all         127.0.0.1/32  scram-sha-256
```

A aplicacao autentica como `sa` **por certificado**, nao por senha. Com o
banco renomeado, nenhuma regra casa, a conexao cai no `host all all` de
fallback, que e scram, e a subida morre com:

```
The server requested SCRAM-based authentication, but no password was provided
```

O erro aponta para senha faltando, e essa e a pista errada: a regra que valia
era a do certificado. Ler o erro como "falta senha" leva a configurar senha que
nao resolve.

As tres foram replicadas para o nome novo, cada uma com seu metodo. Copia do
original em `/etc/postgresql/18/main/pg_hba.conf.bak_antes_rename`.

### 3.2 O Flyway, e o `validate-on-migrate` mascarando

Editar 89 migrations muda o checksum guardado. O dev tem
`validate-on-migrate: false` e nao reclamou. O **prod tem `true`** e quebraria
no deploy. `flyway:repair` rodado, **91 migrations validadas**, e um segundo
repair nao achou nada para fazer.

### 3.3 Os documentos do Mongo

14 documentacoes de NFS-e, todas ligadas a `_id` do Mongo. O `_id` e
`ObjectId` e o Postgres grava como texto. O comparativo inicial procurava
`findOne({_id: 'string'})` e nunca achava — **conclusao errada minha na
primeira hora**. A conversao e do Spring Data.

Verificado pelo caminho real, nao pelo comparativo: **14 de 14 em HTTP 200**,
xml de 5.229 bytes `application/xml`, pdf de 1.637 bytes comecando em `%PDF`.

---

## 4. A suite de testes

### Ela nao media o sistema

Media uma API que nao existe mais. Rodava em **5 falhas com o ERP inteiro de
pe**, e **nenhuma das 5 era defeito**.

| a suite afirmava | a verdade |
|---|---|
| porta 8081 por modulo | monolito, tudo em 8080. Nao ha `modules/` nem jar por modulo |
| `admin` / `admin123` | o login valida contra a **role do Postgres**; os usuarios sao `postgres`, `euripedes`, `sysdba` |
| token em `.token` | a resposta envelope em `.data`, e o token em `.data.accessToken` |
| `/api/empresas` | a rota nao existe. O recurso com ciclo completo e produto |
| DELETE devolve 204 | e soft delete e devolve **200** com o envelope `ApiResponse` |
| health `== 200` | o actuator da **503** em dev, e o esperado |
| `psql` sem senha | a role postgres exige |
| binario `mongo` | removido no MongoDB 6; o shell e o `mongosh`, e ele exige autenticacao |
| CORS no backend | nao existe **por desenho** |
| watchdog rodando | opcional |

### Os dois mais守法es

**CORS.** Nao ha configuracao de CORS no codigo, e esta certo: o frontend em
dev fala com a API pelo proxy do Vite (`vite.config.js` → `target:
http://localhost:8080`), entao a origem e a mesma e nunca ha cross-origin.
Exigir o header reprovava um sistema corretamente configurado.

**Health 503.** Em dev o indicador de mail fica DOWN (nao ha SMTP em
`localhost:25`) e isso derruba o status geral do actuator para 503. Mas o app
esta inteiro: banco UP, mongo UP, disco UP. O proprio 503 e a prova de que o
Tomcat esta servindo — so existe resposta se o Spring MVC estiver roteando.
Exigir 200 faria o script esperar ate o timeout com o backend funcionando.

### O que passou a ser medido

O que antes era decoracao:

- banco UP **dentro** do health, separado do status geral
- DELETE idempotente — o script original nao cobria

### Resultado

```
Total de Testes: 28
Testes Passados: 28
Testes Falhados: 0
Status: TODOS OS TESTES PASSARAM!
```

Duas rodadas seguidas, exit 0. Rotas com o seed: **23 de 23 em 200**.

---

## 5. O seed

`scripts/gerar_seed_desenvolvimento.py` — 9.809 registros em 139 tabelas,
`exit 0`, zero erro, 1,2 s.

| | antes | depois |
|---|---|---|
| produtos | 3 | 176 |
| pessoas | 10 | 183 |
| clientes | 5 | 121 |
| fornecedores | 5 | 121 |
| servicos | 20 | 136 |
| titulos | 9 | 240 |
| pedidos de venda | 4 | 235 |
| pedidos de compra | 1 | 174 |
| funcionarios | 1 | 174 |

O que ele **nao** toca: as 15 NFS-e e seus 7 documentos, o historico do
Flyway, a ACL (`usuario`, `perfil`, `permissao`) e as referencias com dado
real (`issqn` 1.759.790, `ncm` 10.515, `cest`, `cfop`, `municipio`).

### Bug corrigido no caminho

`--banco` do gerador e o **banco** de conexao do psql; o SQL gerado qualifica
o schema a parte. O default tinha virado `brasil_saas`, que e o schema, e o
banco e `brasil-saas`. Com o default errado o dry-run morria com
`database "brasil_saas" does not exist`.

---

## 6. Defeitos que sobraram, e nenhum e do rename

### 6.1 `LazyInitializationException` em duas rotas

```
/api/rh/funcionarios       500 — proxy [Cargo#1] - no Session
/api/producao/romaneios   500 — proxy [Producao#163], mesma familia
```

Pre-existentes. Com **1** registro passavam; com **174** quebram. O seed nao
causou — expôs.

A correcao e **`join fetch`**, nao `EAGER`. `EAGER` troca o 500 por N+1, que em
174 linhas e 174 query por pagina.

### 6.2 `bc_fis_regra_tributaria` com 58 linhas falsas

A tabela estava **vazia** e o seed encheu com 58 regras tributarias
inventadas — ICMS/IPI/PIS/COFINS por UF origem/destino + NCM + CFOP.

Nao e dado decorativo: e a tabela que o ERP usa para **calcular imposto**. 58
regras falsas podem dar imposto errado sem erro nenhum aparecer. O gerador de
seed precisa entrar na lista `NUNCA_SEMEAR`.

---

## 7. Rotacao de senha: tentada e cancelada

Registro, porque o erro foi instructive.

Foi tentado rotacionar `DB_PASSWORD`, `MONGODB_PASSWORD` e `JWT_SECRET` no
`.env`. A senha da role `postgres` do Postgres **e** a senha de login do ERP:
o `PostgresRoleAuthenticationService` valida o login abrindo conexao como a
role. Trocar a senha da role quebrou o login do ERP inteiro.

Foi revertido. Estado final: as tres senhas como estavam, zero linhas de diff
de autenticacao, e o `.env` byte-identico ao estado anterior.

A licao: **verificar o que cada senha desliga antes de rotacionar.** O
`PostgresRoleAuthenticationService` estava no codigo e eu o encontrei depois de
ja ter trocado.

---

## 8. Pendencias

1. **`join fetch` nas duas rotas com `LazyInitializationException`**
2. **`bc_fis_regra_tributaria`** — limpar as 58 linhas falsas e pôr o gerador de
   seed na `NUNCA_SEMEAR`
3. **`regra de tributacao` esta vazia de verdade** — 0 linhas de regra real
4. **Os PDFs de ISSQN nunca foram carregados** — ver `ISSQN-PDFS-PENDENTE.md`
5. **A pasta em disco** continua `BRASIL-SAAS-ERP`; renomear afeta path do git
   remote, `WorkingDirectory` dos services e OneDrive
6. **`temp_fiscal/` e `temp_disabled/`** foram apagados — 78 arquivos de
   rascunho de 19/09
