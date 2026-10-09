# Vendas e devoluções — complemento de 9 de outubro de 2026

Base: `main` em `b1a97866620709dfbbe41d2109f092447eb9e95e`, após a incorporação do PR #100. O incremento anterior passou nos checks de backend, frontend, migrações, contratos, qualidade, segurança e construção Docker.

## Problemas e correções

1. O faturamento selecionava o depósito pelo tipo PADRAO, mas a devolução exigia literalmente o código PADRAO. Um depósito com código CD-PRINCIPAL e tipo PADRAO permitia faturar e impedia devolver. A devolução agora usa a mesma seleção por tipo e alternativa por primeiro depósito ativo da empresa.
2. A devolução buscava o saldo por produto em uma consulta vinculada a outro critério de depósito. Agora bloqueia o saldo pela empresa, depósito selecionado e produto, e registra o movimento no mesmo depósito.
3. A seleção do depósito em vendas e devoluções ignora registros excluídos, além de exigir atividade e empresa correta.
4. Aprovação/rejeição agora bloqueia a devolução, como o recebimento, para serializar essas transições. Uma decisão ou recebimento concluído não pode ser repetido.
5. A solicitação bloqueia o pedido já filtrando sua empresa na consulta.
6. A criação do pedido aceitava status FATURADO/CANCELADO sem executar os respectivos processos. Agora começa somente ABERTO e aceita somente PEDIDO/ORCAMENTO; tipo e status são normalizados. O formulário atual já envia ABERTO e os dois tipos aceitos.

Nenhuma migration ou interface de frontend foi alterada. Os campos do DTO permanecem iguais.

## Verificação

- 19 testes de serviços aprovados: 11 de vendas e 8 de devolução.
- Casos de devolução cobrem depósito com código diferente de PADRAO, saldo existente/novo, movimento no depósito correto, ausência de depósito ativo, decisão repetida e recebimento repetido.
- Casos novos de criação recusam status de etapas futuras e tipo desconhecido antes de qualquer gravação; orçamento com tipo/status em minúsculas continua válido após normalização.
- Os 8 testes de integração `CoberturaFrontendBackendTest` passaram após a troca das consultas de depósito, incluindo inicialização dos repositories Spring Data.
- Relatórios locais consolidados: 274 testes únicos, sem falhas, erros ou ignorados. A suíte completa foi executada no incremento anterior; os testes afetados e a integração de endpoints foram repetidos neste complemento.
- `git diff --check` aprovado.

## Limites e próximos processos

Receber a devolução repõe estoque e registra o movimento; este serviço ainda não efetua restituição financeira, nota fiscal de devolução ou estorno contábil. Essas etapas não são presumidas como concluídas. Também falta validar concorrência real entre operações distintas, alocação de lotes/endereços e reservas repartidas entre depósitos.

A meta de equivalência funcional permanece dependente do encadeamento completo dos processos e integrações reais descritos na auditoria do PR #100.
