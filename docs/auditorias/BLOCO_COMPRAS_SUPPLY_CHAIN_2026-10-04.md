# Bloco funcional — Compras / Supply Chain — 2026-10-04

Este bloco prioriza Compras e sua integração direta com Estoque e Financeiro, deixando Fiscal fora do ciclo de desenvolvimento deste lote.

## Fechado neste bloco

### Isolamento multiempresa
- Pedido de compra criado agora usa a empresa do token, ignorando qualquer `empresaId` enviado pelo cliente.
- Busca, recebimento parcial/integral e cancelamento do pedido passam a exigir a empresa do token.
- Operações de recebimento usam lock pessimista já filtrado pela empresa.
- Fornecedor utilizado no pedido é validado dentro da empresa.
- Título de contas a pagar usa o `pessoa_id` do fornecedor, e não o `fornecedor_id` diretamente.

### Compras
- Pedido sem itens é rejeitado.
- Quantidade <= 0 é rejeitada.
- Valor unitário negativo é rejeitado.
- Fluxo existente permanece: pedido → recebimento → estoque → título a pagar → parcelas.
- Recebimento parcial continua acumulativo.

### Supply Chain
- Solicitação → aprovação → cotação → mapa → pedido permanece integrado.
- Fornecedores da cotação agora são validados por empresa.
- Mapa comparativo passou a devolver pontuação e indicação de vencedor:
  - menor valor: 70 pontos;
  - menor prazo: 30 pontos.
- Geração do pedido continua usando o fornecedor selecionado.

### 3-way match
A conferência de fatura passa a exigir explicitamente:
- pedido;
- recebimento;
- NF-e de entrada autorizada.

A NF-e deve pertencer ao tenant e, quando já vinculada a pedido, deve estar vinculada ao pedido conferido.

## O que ainda precisa ser aprofundado em Compras
- cotação com resposta de fornecedor via portal;
- aprovação por alçada/valor;
- pedido de compra com edição antes do recebimento;
- contratos de compra;
- comparação item-a-item de quantidade, preço, frete e impostos;
- integração automática com entrada fiscal quando a NF-e de compra chegar;
- devolução parcial vinculada ao recebimento/NF-e;
- indicadores de prazo/preço/fornecedor;
- testes automatizados de concorrência e isolamento tenant.

## Próximo bloco
A prioridade segue fora do Fiscal: aprofundar Vendas + WMS/Estoque e depois Produção/Financeiro/Contabilidade. Fiscal permanece preservado para o fechamento.
