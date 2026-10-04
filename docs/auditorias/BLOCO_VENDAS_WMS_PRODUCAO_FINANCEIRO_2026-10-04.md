# Grande bloco funcional — Vendas, WMS, Produção, Qualidade e Financeiro

Data: 2026-10-04

## Escopo

Este lote deliberadamente não avança CT-e/MDF-e/NFS-e. O objetivo é consolidar o ERP fora do Fiscal.

## Vendas

- Pedido comercial exige cliente e itens.
- Quantidade e preço unitário passam por validação.
- Cliente é validado dentro da empresa do pedido.
- O fluxo existente de orçamento → confirmação → reserva → faturamento é preservado.
- Faturamento mantém baixa de estoque, contas a receber e comissão no mesmo ciclo transacional.
- Devolução permanece integrada ao pedido faturado e ao estoque.
- Devolução passa a rejeitar solicitação sem itens antes de persistir o cabeçalho.
- Itens de devolução recebem explicitamente o tenant.
- Quantidade devolvida não pode superar a quantidade vendida.

## WMS / Estoque

- Reserva continua verificando saldo físico, reservas ativas, endereço e lote.
- Reserva duplicada ativa para pedido/produto/endereço é bloqueada.
- Data de expiração passada é rejeitada.
- Expedição não pode ser concluída sem itens.
- Todos os itens precisam passar pela separação antes da expedição.
- Consultas de reservas durante a expedição são tenant-aware.
- Fluxo WMS preservado:
  reserva → onda → picking → packing → conferência → expedição.
- Packing e volumes continuam disponíveis na tela WMS.

## Produção

- OP exige quantidade planejada positiva.
- Produto final precisa existir na empresa.
- OP precisa possuir insumos.
- Cada insumo precisa existir na empresa.
- Quantidade dos insumos precisa ser positiva.
- Fluxo existente de consumo → custo → entrada do produto acabado permanece transacional.
- MRP agora separa leitura de simulação e escrita de materialização das sugestões.

## Qualidade

- Planos, inspeções e não conformidades passam a ter autorização explícita de leitura/escrita.
- Encerramento de não conformidade mantém validação de tenant.

## Financeiro

- Conciliação automática valida tolerância de dias.
- Vinculação de baixa verifica empresa e período.
- Fluxo existente de extrato → baixa → conciliação automática → fechamento é preservado.

## Regra do lote

O objetivo não é criar endpoints decorativos. Cada alteração fecha uma regra operacional ou reduz risco de inconsistência entre módulos.

## Próxima prioridade

Continuar em blocos grandes com:
1. Produção/MRP aprofundado e integração com Compras;
2. Financeiro/Contabilidade;
3. Projetos/Qualidade/Ativos/RH;
4. BI;
5. Fiscal somente no fechamento.
