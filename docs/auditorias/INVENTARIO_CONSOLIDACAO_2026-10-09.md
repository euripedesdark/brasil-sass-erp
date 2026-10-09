# Inventário de candidatos à consolidação — 09/10/2026

Escopo: código próprio Java e frontend React. Nome ou hash coincidente é apenas evidência de candidatura; referências, contratos e contexto determinam a exclusão. Bibliotecas, dumps, certificados e migrations históricas são preservados.

Arquivos examinados: 1231.

## Mesmo conteúdo, inclusive com nomes diferentes


## Mesmo nome em caminhos diferentes

- `src/main/java/br/com/brasil_saas/shared/web/ApiResponse.java`; `src/main/java/br/com/brasil_saas/shared/dto/ApiResponse.java`; `src/main/java/br/com/brasil_saas/shared/model/ApiResponse.java`
- `src/main/java/br/com/brasil_saas/shared/identity/AuthServiceProperties.java`; `src/main/java/br/com/brasil_saas/core/config/AuthServiceProperties.java`
- `src/main/resources/static/react/src/components/Financeiro.jsx`; `src/main/resources/static/react/src/components/financeiro/Financeiro.jsx`
- `src/main/java/br/com/brasil_saas/bi/controller/RelatorioController.java`; `src/main/java/br/com/brasil_saas/core/controller/RelatorioController.java`
- `src/main/java/br/com/brasil_saas/bi/service/RelatorioService.java`; `src/main/java/br/com/brasil_saas/core/service/RelatorioService.java`
- `src/main/java/br/com/brasil_saas/supplychain/controller/SupplyChainEnterpriseController.java`; `src/main/java/br/com/brasil_saas/enterprise/controller/SupplyChainEnterpriseController.java`

## Consolidação realizada

- `EmpresaStripeService.java.bak2-20261005-182640` → `EmpresaStripeService.java`: corpo preservado integralmente; a diferença são imports adicionais. Nenhum recurso exclusivo no backup.
- `StripeFinanceServiceImpl.java.bak2-20261005-182640` → `StripeFinanceServiceImpl.java`: todos os fluxos anteriores continuam presentes. A versão atual acrescenta desserialização de webhooks incompatíveis com a versão da API, tratamento explícito de falhas, listas de pagamentos/webhooks e status de configuração. O descarte silencioso de eventos do backup foi substituído pelo tratamento atual. Nenhum recurso exclusivo a migrar. Um import `List` repetido no atual foi removido.

## Candidatos preservados

- Controllers SupplyChain: rotas `/api/enterprise` e `/api/supply-chain/enterprise` diferentes; preservar contratos.
- Pacotes `contabil` e `contabilidade`: representações e serviços distintos; requer reconciliação contábil antes de unificar.
- `frontend/` é estrutura de migração modular, enquanto a aplicação ativa está em `src/main/resources/static/react/`; diretórios vazios não provam abandono.
- Scripts repetidos na raiz e em `scripts/`: dependem de diretório relativo e procedimentos operacionais; não excluir com base no nome.
