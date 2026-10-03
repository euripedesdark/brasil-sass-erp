# Documentacao

A raiz do repositorio tem so o `README.md`. Tudo mais esta aqui, organizado por
assunto.

## Onde procurar

| assunto | pasta |
|---|---|
| relatorios de teste e de sessao | [`relatorios/`](relatorios/) |
| auditorias e mapas de cobertura | [`auditorias/`](auditorias/) |
| plano de trabalho | [`roadmap/`](roadmap/) |
| frontend React | [`frontend/`](frontend/) |
| build, deploy, restore, microsservicos | [`infra/`](infra/) |
| changelog e tarefas | [`guia/`](guia/) |
| pesquisa e quem investigates | [`pesquisa/`](pesquisa/) |
| incidentes e coordenao entre IAs | [`incidentes/`](incidentes/) |
| decisao fiscal pendente: NCM ambiguos e inexistentes | [`RELATORIO-NCM-PENDENTES.md`](RELATORIO-NCM-PENDENTES.md) |
| decisoes de arquitetura fechadas | [`ARQUITETURA-IDENTIDADE.md`](ARQUITETURA-IDENTIDADE.md) |
| geracao de menus | [`modulos/`](modulos/) |
| referencia de API | [`api/`](api/) |

## Leitura obrigatoria antes de mexer

- [`relatorios/TESTE-SUITE-E-RENAME-INPI.md`](relatorios/TESTE-SUITE-E-RENAME-INPI.md) — o rename para o INPI, as duas armadilhas que derrubaram o boot, e a suite de testes
- [`ISSQN-PDFS-PENDENTE.md`](ISSQN-PDFS-PENDENTE.md) — os dois PDFs de ISSQN estao no repo e os dados **nao** estao no banco
- [`infra/RESTAURAR.md`](infra/RESTAURAR.md) — como restaurar o banco

## Nome: porque o banco tem hifen e o schema nao

```
banco    brasil-saas      o hifen e seguro: vive na URL do JDBC e no pg_hba
schema   brasil_saas      o hifen nao e: '-' nao e identificador SQL valido
datalake brasil_saas_dl   e exigiria aspas em 88 migrations e em todo SQL
Mongo    brasil-saas      nome de banco do Mongo, nao e identificador SQL
pacote   br.com.brasil_saas   Java nao aceita hifen em pacote
```

## Coerencia de nomes

| | nome |
|---|---|
| razao social | `SRVCLOUD CONSULTORIA LTDA` — titular do registro, **nao** a marca |
| nome fantasia / marca | `Brasil SaaS` |
| titulo da aba | `Brasil SaaS ERP` |
| rodape do PDF | `Gerado por Brasil SaaS ERP` |

Antes de um replace de nome, procure o nome em tres lugares que **nao sao nome**:
caminho de disco, regex escapada (`br\\.com\\.brasil-saas`) e URL. Nenhum dos
tres aparece num grep do nome, e qualquer um dos tres derruba o boot. Ver secao
2 do relatorio.

## Pendencias conhecidas

| | |
|---|---|
| `LazyInitializationException` em `/api/rh/funcionarios` e `/api/producao/romaneios` | correcao e `join fetch`, **nao** `EAGER` |
| `bc_fis_regra_tributaria` com 58 linhas falsas do seed | limpar e pôr em `NUNCA_SEMEAR` |
| PDFs de ISSQN | dados nao carregados, ver [`ISSQN-PDFS-PENDENTE.md`](ISSQN-PDFS-PENDENTE.md) |
| pasta em disco | continua `BRASIL-SAAS-ERP`; renomear afeta git remote, `WorkingDirectory` e OneDrive |
