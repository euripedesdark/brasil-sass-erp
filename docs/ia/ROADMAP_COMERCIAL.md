> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Roadmap comercial — Brasil SaaS ERP

Objetivo: **mesmo grau de uso** de SAP Business One / Sankhya para PME brasileira,
sem copiar UI — equivalência de **processo e regra**.

> Realidade: isso é trabalho de **meses/anos**, não de um único PR.
> Este documento prioriza o que falta para **vender e operar**.

---

## Estado atual (resumo)

| Modulo | Backend | Frontend | Pronto para vender? |
|--------|---------|----------|---------------------|
| Cadastros | Alto | Alto | Sim (base) |
| Vendas (pedido) | Medio | Medio | Parcial |
| Compras | Medio | Medio | Parcial |
| Estoque | Medio | Medio | Parcial |
| Financeiro | Medio-alto | Medio | Parcial |
| Fiscal (tabelas + entrada) | Medio | Medio | Nao (falta NF-e) |
| Producao | Basico | Basico | Nao |
| Servicos/OS | Medio | Medio | Parcial |
| RH | Basico | Basico | Nao |
| BI/IA | Esqueleto | Esqueleto | Nao |

**Problema recorrente:** APIs existem; telas nem sempre chamam os endpoints corretos
(ex.: Vendas front chamava `/status` e `DELETE`; back expoe `/faturar` e `/cancelar`).

---

## Fase 0 — Paridade front ↔ back (em andamento)

- [x] Alinhar `PedidoVendaService.js` com `POST .../faturar` e `.../cancelar`
- [x] Dashboard operacional com atalhos + indicadores BI
- [ ] Auditoria tela a tela: cada controller tem tela e botoes de acao de negocio
- [ ] Listagens sempre com `empresaId` do usuario logado

## Fase 1 — Fluxo de ouro (obrigatorio para comercializar)

1. **Venda:** orcamento → confirmado → faturar → estoque ↓ → titulo a receber
2. **Compra:** pedido → recebimento → estoque ↑ → titulo a pagar
3. **NF-e:** emissao + XML/PDF + status SEFAZ (autorizada/cancelada/cce)
4. **Caixa/banco:** baixa de titulo + extrato + conciliacao minima

## Fase 2 — Operacao diaria PME

- Multi-deposito, inventario, lote/serie (se o segmento exigir)
- Tabela de preco e politica de desconto
- Comissao de vendedor
- Boleto/PIX ou integracao bancaria
- Relatorios: DRE gerencial, aging, curva ABC

## Fase 3 — Compliance e industria

- SPED / obrigacoes acessorias conforme regime
- BOM + OP completa + apontamento + custo
- Workflow de aprovacao (desconto, pedido, pagamento)

## Fase 4 — Diferenciacao SaaS

- Multi-empresa / planos / billing
- Portais cliente e fornecedor
- BI real + alertas
- IA aplicada a processo (sugestao de compra, risco de credito)

---

## Criterio de "mesmo nivel de uso"

Um cliente PME consegue, **so com o sistema**:

1. Cadastrar cliente/produto/fornecedor
2. Vender e emitir documento fiscal valido
3. Comprar e entrar mercadoria
4. Controlar estoque minimo confiavel
5. Receber e pagar com baixas no financeiro
6. Fechar o mes com relatorios basicos

Ate a Fase 1 estar fechada, **nao** posicione como substituto Sankhya/SAP B1.
Posicione como: *ERP SaaS brasileiro em evolucao, com base de cadastros e
operacao, fiscal em implementacao*.

---

## Proximos PRs sugeridos (ordem)

1. Fechar `PedidoVendaService` implementacao (estoque + titulo no `faturar`)
2. Tela fiscal de **saida** (NF-e) espelhando entrada
3. Inventario de estoque
4. Conciliacao bancaria
5. Parametrizacao empresa (serie NF, CSC, certificado)
