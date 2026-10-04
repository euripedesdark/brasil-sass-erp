> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

# UX e CI — relatório de entrega

Data: 2026-09-28
Escopo: frontend React e pipelines GitHub Actions. Nenhum Java, migration ou firewall foi alterado por este trabalho.

## 1. Alterações realizadas

### Frontend
- src/main/resources/static/react/frontend.smoke.test.js
  - Smoke test existente foi ampliado; continua executado diretamente com Node, sem runner externo.
  - Verifica o build canônico, imports relativos de JS/JSX e componentes usados pelos Route de App.jsx.
- package.json
  - Não alterado: já aponta test para frontend.smoke.test.js.
- package.json da raiz
  - Mantido fora do build do ERP. Ele pertence ao conjunto Neon da raiz e não possui script de build do frontend. O caminho canônico do ERP continua sendo src/main/resources/static/react.

### CI/CD
- .github/workflows/ci-cd.yml
  - Removido continue-on-error do teste backend.
  - Removido -Dsurefire.failIfNoSpecifiedTests=false.
  - Adicionada contagem explícita de testes Surefire; zero testes falha o job.
  - Smoke frontend tornou-se obrigatório.
  - Adicionado database-migration-smoke com PostgreSQL 18, Flyway validate/migrate/info, versão 106 e verificação de tabelas.
  - Deploy staging deixou de pular silenciosamente por secrets ausentes.
- .github/workflows/cd-deploy.yml
  - Deploy staging e production agora validam secrets antes do SSH.

## 2. Diagnósticos corrigidos

TesteGeralSistema e TesteModelosSimples começam com Test e portanto são encontrados pelo padrão Test* do Surefire. O defeito efetivo era continue-on-error: true, que podia deixar o job verde após falha real.

O frontend já tinha npm test configurado e frontend.smoke.test.js já existia no caminho canônico. O problema era a profundidade do teste: ele verificava apenas a presença de poucos arquivos. O smoke agora percorre os fontes, valida imports relativos e confere os componentes usados pelas rotas de App.jsx.

## 3. Proteções respeitadas

Não foram alterados controllers/services Java protegidos de outras IAs, migrations, os quatro cadastros ou shared/tenant/EmpresaDaGravacao.java.

Também não foram alterados ci-code-quality.yml, ci-database.yml, microservices-validation.yml ou test-coverage.yml nesta rodada.

Nenhum secret foi criado. A ausência de secret agora é erro explícito em deploy aplicável.

## 4. Como verificar

Frontend:
cd src/main/resources/static/react && npm ci && npm test && npm run build

Backend:
mvn -B test

O workflow deve imprimir Executed tests: N e falhar se N for zero.

Migrations:
o job Database Migration Smoke deve iniciar PostgreSQL 18, validar, aplicar e informar migrations, confirmar versão 106 e confirmar tabelas no schema brasil_saas.

Deploy:
em main ou tag aplicável, secrets ausentes devem gerar Missing STAGING_* ou Missing PRODUCTION_* em vez de um job verde sem deploy.

## 5. Risco e primeiro ponto de inspeção

1. Database Migration Smoke.
2. Backend Build e contagem Surefire.
3. Frontend Build, npm test e vite build.
4. Deploy, se os secrets estiverem configurados.

## 6. Pendência

ci-code-quality.yml e test-coverage.yml ainda possuem etapas com continue-on-error. Ficam como segunda passada para transformar qualidade/cobertura em checks realmente bloqueantes.


## 7. Segunda etapa — correção visual e responsividade UX (2026-09-28)

A etapa seguinte foi feita sobre a base do commit c17255c46bd2928358991e181b1c2e56e3c91265.

### Objetivo

Corrigir o miolo visual do ERP sem descaracterizar o shell existente. A barra lateral, a barra superior e a imagem de fundo foram preservadas. O problema atacado foi a superfície dos módulos, que estava excessivamente clara/branca, com ações pouco organizadas e dimensionamento inconsistente.

### Alterações

- base.css: superfície operacional escura/translúcida; cartões, painéis, tabelas, inputs, dropdowns, calendário, toolbar, botões e diálogos padronizados; grid determinístico de 12 colunas; utilitários de formulário/ações; breakpoints e dimensões proporcionais por vw/vh/%.
- Layout.css: shell preservado; conteúdo com dimensionamento previsível; sidebar/header usam proporções de viewport nos breakpoints intermediários; telas pequenas usam a sidebar compacta existente.
- Vendas.css: removido auto-fit do grid; formulário usa 12 colunas explícitas; ações da tabela não quebram aleatoriamente; dialog proporcional à viewport; breakpoints explícitos.
- Vendas.jsx: corrigido emAcao com finally/setEmAcao(null), evitando botão/linha permanecer travado após faturamento.

### Princípio de dimensionamento

Não foi utilizado auto-fit, auto-fill ou estratégia que escolha a quantidade de colunas de forma variável. Desktop usa grid explícito de 12 partes; campos ocupam quantidades definidas; diálogos e conteúdo usam vw; espaçamentos/alturas relevantes usam vw/vh; tablets e mobile entram em breakpoints explícitos.

### Commits desta etapa

As alterações foram gravadas em quatro commits consecutivos sobre c17255c46bd2928358991e181b1c2e56e3c91265:
- 9aa4154fe73c581207f61403d14fcc3f13f61089 — superfície visual compartilhada.
- dbc4d1cd04ebad84c9ba8495ad09f2febdae2d83 — dimensionamento do shell.
- 9113eb12aa96b079b692609d9c13c07aeeb1b63f — responsividade específica de Vendas.
- 948ce302b803abcf81c282d20e056e24bc016ab6 — correção do estado da ação de faturamento.

O intervalo completo pode ser conferido comparando c17255c46bd2928358991e181b1c2e56e3c91265...main.

### Validação

A validação de build não pôde ser executada no runtime desta sessão porque o clone direto do GitHub falhou por indisponibilidade de resolução de rede. Portanto, não é correto declarar npm run build como aprovado nesta etapa.

Verificação recomendada:
cd src/main/resources/static/react && npm ci && npm test && npm run build

### Histórico consolidado das duas etapas

Etapa CI/CD — c872fce2b12fee42fbe8fb402965b121e44653ae:
- backend deixou de usar continue-on-error;
- removido -Dsurefire.failIfNoSpecifiedTests=false;
- contagem explícita de testes Surefire;
- zero testes falha o job;
- smoke frontend obrigatório e reforçado;
- smoke de migration com PostgreSQL/Flyway;
- deploy staging deixou de ignorar secrets ausentes.

Etapa documentação — c17255c46bd2928358991e181b1c2e56e3c91265:
- relatório corrigido conforme estrutura real do frontend;
- registrado que frontend.smoke.test.js já existia;
- package.json raiz mantido;
- corrigida a interpretação dos padrões de descoberta do Surefire;
- registradas proteções para arquivos de outras IAs.

Etapa UX — commits 9aa4154, dbc4d1c, 9113eb1, 948ce30:
- redesenho da superfície dos módulos;
- responsividade determinística por percentuais/breakpoints;
- padronização de formulários/tabelas/botões/dialogs;
- correção do estado de faturamento de Vendas.

Nenhum controller/service Java protegido, migration, cadastro protegido ou EmpresaDaGravacao.java foi alterado nesta etapa.


## 9. Continuação UX — padronização de módulos (2026-09-28)

Após a primeira correção do shell/conteúdo e de Vendas, foi feita uma segunda varredura para eliminar layouts responsivos legados que ainda utilizavam dimensionamento automático.

Arquivos tratados:
- `Dashboard.css`: atalhos e KPIs passaram para grid explícito de 12 colunas, com breakpoints definidos.
- `admin/ModuloSelector.css`: selector de módulos passou para 12 colunas, com 4/6/12 colunas por breakpoint.
- `producao/Producao.css`: formulário passou de `auto-fit/minmax` para 12 colunas explícitas; tabela, cards, loading e dialog foram alinhados à superfície escura do ERP.
- `financeiro/Financeiro.jsx`: cards financeiros passaram de `auto-fit/minmax` para 12 colunas explícitas e receberam a mesma linguagem visual do ERP; breakpoints 3/6/12 colunas.
- `vendas/Vendas.css`: grid legado foi substituído definitivamente por 12 colunas.
- `shared/base.css`: galeria de imagens passou para 6/4/2 colunas por breakpoint, sem algoritmo automático de quantidade de colunas.

Também foi corrigida a estrutura do `<style>` inline de Financeiro para que os media queries permaneçam dentro do CSS válido do componente.

### Regra consolidada

A partir desta etapa, os grids de módulos que foram revisados não escolhem dinamicamente a quantidade de colunas por `auto-fit`/`auto-fill`. A quantidade de colunas é definida pelo código e alterada somente nos breakpoints explícitos. O tamanho disponível é usado para dimensionar a área, não para decidir de forma variável a estrutura.

### Validação de fonte

Os seis arquivos acima foram relidos diretamente do GitHub após a alteração. Os únicos textos restantes contendo os termos `auto-fit`/`auto-fill` são comentários documentais que explicam a remoção do comportamento; não são declarações CSS ativas.

Nenhum arquivo Java protegido pelas outras IAs foi alterado nesta etapa.
