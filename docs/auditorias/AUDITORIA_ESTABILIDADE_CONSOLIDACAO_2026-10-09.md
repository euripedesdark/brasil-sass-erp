# Auditoria inicial de estabilidade e consolidação — 2026-10-09

> Levantamento técnico preliminar, baseado na branch `main` em `207cddb8816f524e2da1eab4e0f628887fe756eb`. **Não certifica paridade SAP, execução local dos testes nem prontidão para produção.**
>
> Este documento é preservativo: **nenhum arquivo antigo foi excluído** e **nenhuma migration aplicada foi alterada**.

## PRs confirmados

- #38 FI-AA/PM, V173; #39 mapeamentos Spring e regras de ativos; #77 contrato de fornecimento/V179; #79 TMS/eSocial; #80 DIFAL/menus; #81 ICMS-ST/intercompany.
- Todos os seis PRs estão mesclados. Isso comprova integração ao histórico, não aprovação de testes ponta a ponta.

## Bloqueios confirmados no CI do commit de referência

1. Build Maven falha (run [37916411216](https://github.com/euripedesdark/brasil-sass-erp/actions/runs/37916411216), job 113773601902) por referências a métodos que não existem no contrato atual: `EfdPeriodoService.moedaOuZero(BigDecimal)`; métodos de `DepositoRepository` esperados por `PedidoVendaServiceImpl`; e `MovimentacaoEstoque.setDocumentoTipo`/`setDocumentoId`. Examinar **todas** as mensagens do log, não apenas estes exemplos, antes de declarar o build corrigido.
2. Flyway falha em banco vazio (run [37916411236](https://github.com/euripedesdark/brasil-sass-erp/actions/runs/37916411236), job 113773684536), na V110, SQLSTATE 42P01: `bc_core_empresa` não existe antes da FK de `bc_ia_config`. As 70 migrations são reconhecidas, mas isso **não** prova que sejam executáveis. Diagnosticar a origem da tabela-base e o bootstrap/snapshot legado; **não editar V110 aplicada** nem usar `flyway repair` sem histórico e plano de migração.
3. Build Docker falha e o deploy foi **ignorado**, não executado (run [37916411197](https://github.com/euripedesdark/brasil-sass-erp/actions/runs/37916411197)).
4. O frontend, checagem de qualidade, cobertura e validação de microsserviços apresentam sucesso em jobs separados; não comprovam o sistema integrado.

## Candidatos à análise de redundância — **não apagar automaticamente**

| Candidato | Evidência | Tratamento seguro |
|---|---|---|
| `core/service/EmpresaStripeService.java.bak2-20261005-182640` | Existe junto do `.java` atual; este inclui tratamento diferenciado de erros Stripe. | Comparar conteúdo e testes; preferir atual somente após prova de equivalência funcional e preservação no Git. |
| `financeiro/service/impl/StripeFinanceServiceImpl.java.bak2-20261005-182640` | Backup com 422 linhas e atual com 510. | Comparar métodos e fluxos Stripe, idempotência de webhooks e segurança; não assumir que maior implica melhor. |
| `enterprise/controller/SupplyChainEnterpriseController.java` e `supplychain/controller/SupplyChainEnterpriseController.java` | Nomes iguais, **rotas distintas** (`/api/enterprise` e `/api/supply-chain/enterprise`). PR #39 tratou colisão de beans. | Preservar ambos; não são duplicatas funcionais. |
| `contabil/controller/ContabilidadeEnterpriseController.java` e `contabilidade/controller/ContabilidadeController.java` | Exibem balanços/balancetes com modelos/caminhos distintos. | Mapear tabelas, regras de competência e fechamento antes de qualquer unificação; preservar endpoints. |
| Diversos arquivos em `src/main/resources/microservices/` | Fontes de outros projetos, bibliotecas e versões de esquemas. | Inventariar dependências, licenças, builds e versões; não remover por nome idêntico. |

## Prioridades para paridade funcional ERP empresarial

1. **P0 — compilação, Flyway, boot e CI:** corrigir integrações Java sem quebrar modelos existentes; estabelecer bootstrap reversível e smoke em PostgreSQL limpo e com schema legado.
2. **P0 — qualidade e segurança:** cobertura por fluxo, testes de isolamento de empresas, permissões, logs, segredos, migrations, idempotência e concorrência.
3. **P1 — contabilidade integrada:** lançamento por evento, razão, reconciliação, períodos, fechamento/reabertura com trilha e dupla contabilidade sem divergência.
4. **P1 — compras e vendas ponta a ponta:** 3-way match, múltiplas NFs, recebimento parcial, estoque, financeiro, comissão, crédito e devolução.
5. **P1 — WMS, PCP/MRP, ATP/CTP:** disponibilidade temporal/local, reservas, lead times, rastreabilidade, qualidade, custeio e planejamento.
6. **P1 — ativos, manutenção, RH, TMS e contratos:** exercício ponta a ponta com persistência, contas, aprovações, estornos, integrações e relatórios.
7. **P2 — fiscal e conformidade:** ICMS-ST/DIFAL mais integração com documentos, apuração, SPED e reforma tributária; validação da legislação e cenários, jamais inferir produção de uma tela/API isolada.
8. **P2 — experiência operacional/BI:** métricas confiáveis e relatórios reconciliados, dashboards, acessibilidade, navegação, documentação e observabilidade.

### Critério de conclusão

Nenhum processo será classificado 'equivalente ao SAP' apenas por CRUD, endpoint, tela ou teste unitário. Exigir regra de negócio, exceções, auditoria, autorização, isolamento tenant, banco, telas, integração e teste ponta a ponta com evidências.

## Procedimento para consolidar arquivos

1. Criar inventário de classes, componentes, configuração, migrations e artefatos legados; atribuir risco e referências.
2. Para cada par suspeito, comparar responsabilidades, todas as APIs e métodos, contratos SQL, cenários de teste e histórico Git.
3. Selecionar implementação mantida por compatibilidade e correção, usando recência apenas como desempate; migrar a funcionalidade exclusiva da antiga.
4. Adicionar testes de regressão e de compatibilidade antes de excluir; usar branch/PR revisável e manter histórico Git para recuperação.
5. **Nunca apagar automaticamente snapshots/dumps, scripts de recuperação, certificados, schemas fiscais, migrations históricas ou código com referência dinâmica.**

## Limitações desta auditoria

Análise via GitHub remoto; não houve execução local completa da aplicação, banco real, revisão exaustiva dos ~16 mil arquivos nem diff total de todos os candidatos. As conclusões de segurança devem ser revalidadas no código e no CI após cada mudança.
