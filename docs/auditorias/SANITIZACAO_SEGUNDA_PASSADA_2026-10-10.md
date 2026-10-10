# Sanitização estrutural — segunda passada (2026-10-10)

## Estado de entrada

A primeira passada foi incorporada à `main` pela PR #142:
- Compras é o dono do processo MM operacional; `supplychain` fica restrito a ATP e Control Tower.
- Estoque e WMS permanecem separados: domínio/saldos versus operação de armazém.
- O controller enterprise de Contabilidade foi movido para o pacote `contabilidade`, preservando `/api/contabilidade/enterprise/*`.
- A segunda entrada de catálogo de serviços no menu foi removida; o cadastro continua acessível em Cadastro e o catálogo operacional em Serviços.

PR: https://github.com/euripedesdark/brasil-saas-erp/pull/142

## Segunda passada — o que pode ser apagado hoje?

**Conclusão conservadora:** além da entrada de menu removida na PR #142 e dos backups já tratados no inventário anterior, esta análise não confirma outro arquivo de produção que possa ser apagado com segurança imediata. Nome parecido, classe com mesmo nome ou conteúdo parcialmente semelhante não é prova suficiente para excluir.

| Arquivo / item | Motivo | Impacto |
|---|---|---|
| Entrada duplicada `menu.serviceCatalog` em Serviços | Mesmo destino `/cadastro/servicos` já acessível pelo grupo Cadastro; removida na PR #142 | Baixo; elimina uma segunda entrada visual, sem retirar rota nem tela |
| `src/main/java/br/com/brasil_saas/shared/web/ApiResponse.java` | **Não apagar agora.** Envelope `{success, code, data}` usado por múltiplos controllers | Exclusão pode quebrar serialização/contratos de API |
| `src/main/java/br/com/brasil_saas/shared/dto/ApiResponse.java` | **Não apagar agora.** Envelope `{success, message, data, errors[], timestamp}`; há controllers consumidores | Exclusão pode quebrar compilação e contratos JSON |
| `src/main/java/br/com/brasil_saas/shared/model/ApiResponse.java` | **Não apagar agora.** Envelope de erro/meta consumido por filtro, autenticação e handler global | Exclusão pode afetar respostas de erro e autenticação |
| `src/main/java/br/com/brasil_saas/core/config/AuthServiceProperties.java` | **Não apagar agora.** Consumida por `AuthServiceClient` e testes | Exige migrar os consumidores e garantir um único bean/configuração |
| `src/main/java/br/com/brasil_saas/shared/identity/AuthServiceProperties.java` | **Não apagar agora.** Consumida por `IdentityService` e testes; mesmo prefixo de configuração, mas uso distinto | Fusão sem migração pode alterar configuração de autenticação |
| `src/main/java/br/com/brasil_saas/bi/controller/RelatorioController.java` e `src/main/java/br/com/brasil_saas/core/controller/RelatorioController.java` | Candidatos a revisão; namespaces e serviços diferentes | Não excluir antes de comparar rotas, DTOs, permissões e consumidores |
| `src/main/java/br/com/brasil_saas/bi/service/RelatorioService.java` e `src/main/java/br/com/brasil_saas/core/service/RelatorioService.java` | Mesmo nome, domínios diferentes | Fusão prematura pode misturar relatórios de BI com geração/entrega de relatórios |
| `src/main/resources/static/react/src/components/Financeiro.jsx` e `src/main/resources/static/react/src/components/financeiro/Financeiro.jsx` | Mesmo nome em caminhos diferentes; confirmar imports e rotas da aplicação ativa | Apagar o arquivo errado pode quebrar navegação ou build |
| `src/main/java/br/com/brasil_saas/supplychain/controller/SupplyChainEnterpriseController.java` e `src/main/java/br/com/brasil_saas/enterprise/controller/SupplyChainEnterpriseController.java` | Prefixos diferentes: `/api/supply-chain/enterprise` e `/api/enterprise`; responsabilidades também diferentes | Ambos devem ser preservados até eventual extração de serviços compartilhados, sem remoção de contratos |

## Terceira passada — fusões candidatas, não exclusões imediatas

1. **Envelope de API:** comparar os três `ApiResponse`, definir contrato canônico e migrar consumidores em lotes pequenos. Manter compatibilidade JSON durante a transição.
2. **Configuração do Auth Service:** consolidar `AuthServiceProperties` em uma classe canônica e atualizar `AuthServiceClient`, `IdentityService`, configurações e testes; verificar se o prefixo duplicado registra beans concorrentes.
3. **Relatórios:** mapear endpoints e consumidores BI/Core. Compartilhar utilitários comuns somente quando o contrato for realmente igual; manter os serviços de domínio separados se a finalidade divergir.
4. **Financeiro frontend:** identificar qual componente está ligado ao router e ao build de produção; só então mover/renomear ou retirar o duplicado.
5. **Supply Chain enterprise:** preservar os dois prefixos HTTP. Se houver lógica duplicada, extrair serviço comum sem mudar as rotas públicas.
6. **Compras × Supply Chain:** manter solicitação, aprovação, cotação, mapa comparativo, geração de pedido e recebimento no domínio Compras. ATP, cálculo temporal e Control Tower ficam em Supply Chain; retirar gradualmente nomenclatura ambígua, sem quebrar `/api/compras/supply-chain/*` enquanto houver consumidores.

## Quarta passada — validação ponta a ponta

A validação precisa rodar em ambiente com PostgreSQL de teste e dump compatível, quando disponível. Não executar migrations destrutivas nem usar dump de produção como banco de escrita.

| Fluxo | Caminho mínimo | Verificações |
|---|---|---|
| Compras | solicitação → aprovação/rejeição → cotação → mapa comparativo → pedido → recebimento parcial/final | alçadas, tenant, saldo recebido, fornecedor bloqueado, transações e idempotência |
| Vendas | orçamento/pedido → reserva → faturamento → entrega → devolução | preço, estoque, permissões, vínculo fiscal/financeiro e estorno |
| WMS | recebimento → endereço/lote → reserva → picking → packing → expedição | rastreabilidade, concorrência, saldo e isolamento por empresa |
| Produção | estrutura/BOM → MRP → ordem → apontamento → consumo → produto acabado | disponibilidade de componentes, custo e baixa/entrada de estoque |
| Financeiro | título a pagar/receber → aprovação → liquidação → conciliação | saldo, duplicidade de liquidação, auditoria e permissões |
| Contabilidade | lançamento → partidas → balancete/DRE → fechamento → ECD quando aplicável | débito = crédito, período fechado, integração e isolamento por empresa |
| RH | colaborador → ponto/evento → folha → encargos/obrigações | cálculo, competência, acesso restrito e trilha de auditoria |
| Fiscal | documento de entrada/saída → validação tributária → transmissão/retorno → escrituração | ambiente de homologação, certificado, idempotência, status e vínculo financeiro/estoque |

## Portões antes de excluir ou fundir

- [ ] Referências Java/React e imports atualizados.
- [ ] Rotas registradas e chamadas do frontend mapeadas.
- [ ] Contrato HTTP/JSON comparado antes/depois.
- [ ] Permissões e isolamento por empresa preservados.
- [ ] Migrations históricas preservadas; alterações de schema somente aditivas e versionadas.
- [ ] Testes unitários e de integração adicionados ou atualizados.
- [ ] `mvn clean verify` e build/testes do React executados em CI.
- [ ] Fluxos ponta a ponta validados com banco de teste.
- [ ] Nenhuma exclusão aprovada apenas por semelhança de nome ou hash.

## Limitação desta passada

Esta é uma auditoria estática baseada no código e nos inventários disponíveis no GitHub. Ela não afirma que os fluxos ponta a ponta foram executados nem que o dump foi restaurado. Os candidatos acima precisam de verificação de referências e testes antes de qualquer remoção.
