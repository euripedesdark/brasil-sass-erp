> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

# PDV — fluxo congelado (Fase 2A)

> 29/09/2026 · decisao do dono: antes da tela, o fluxo.
> Escopo da 2A: pesquisa, carrinho, subtotal, desconto, total, estoque.
> Fora do escopo: fechamento avancado, formas multiplas de pagamento, TEF,
> comanda, delivery.

---

## 1. A REGRA QUE NAO SE NEGOCIA

**O backend calcula. O frontend exibe.**

O carrinho nunca soma, nunca multiplica, nunca aplica desconto. Ele manda
`quantidade`, `valorUnitario` e os descontos para
`POST /api/vendas/pedidos` e **pinta o que a resposta devolve**:

```json
{
  "valorProdutos": 55.80,
  "valorDescontoItens": 5.58,
  "valorDescontoPedido": 0.00,
  "valorDescontoTotal": 5.58,
  "valorTotal": 50.22
}
```

Motivo, e nao escolha de estilo: o bug do desconto contado duas vezes vivia no
servidor. Se a tela recalcular, um `toFixed` Javascript qualquer volta a
divergir do financeiro. Como o total da tela e' o total do banco, nao ha como
eles discordarem.

**Numeros da tela sao sempre os cinco campos acima.** Nada de
`itens.reduce(...)` no componente.

---

## 2. O FLUXO

```
   [1] PESQUISA          [2] CARRINHO          [3] DESCONTO
   busca produto    ->   adiciona item     ->  item ou pedido
   por nome/codigo        soma quantidade        (XOR valor/percentual)
                                 |
                                 v
   [5] FATURAR          [4] ESTOQUE
   POST /faturar     <-   valida antes de
   baixa estoque            permitir
   gera titulo             faturar
```

### [1] Pesquisa de produto

| Item | Decisao |
|---|---|
| Endpoint | `GET /api/cadastro/produtos?page=0&size=20&busca=<texto>` |
| Busca | nome **ou** codigo **ou** codigo de barras |
| Filtro | so `ativo = true` e da empresa logada (o backend ja filtra) |
| Tecla | Enter busca; a lista filtra enquanto digita, com debounce de 250 ms |
| Sem resultado | "Nenhum produto encontrado para 'texto'" — nao tela vazia sem explicacao |
| Produto sem saldo | **aparece** na lista, com selo vermelho "sem estoque". Esconder o produto e' pior que o caixa descobrir na hora de fechar. |
| Servico | nao entra na 2A. `bc_cad_servico` e' coisa da Fase 2B. |

### [2] Carrinho

| Item | Decisao |
|---|---|
| Adicionar | soma a quantidade **se o produto ja estiver no carrinho**; senao cria linha |
| Mesma unidade | considerar `unidade` no agrupamento: 2 CX + 1 UN sao duas linhas |
| Alterar quantidade | campo direto na linha, sem dialogo. Zero apaga a linha. |
| Quantidade fracionaria | aceita (peso, KG). Campo numerico, ate 3 casas, como o banco |
| Quantidade zero ou negativa | bloqueia e mostra "quantidade deve ser maior que zero" |
| Remover linha | botao de lixeira na linha, sem confirmacao. Desfazer nao entra na 2A. |
| Cliente | select no topo do carrinho. **"Consumidor nao identificado"** e' o padrao e e' valido |
| Cliente do armazem | selecting mostra o limite de credito: "limite R$ 150.000, usado R$ 0" |

### [3] Desconto

Duas formas, mutuamente exclusivas — o backend recusa as duas juntas com 400,
e a tela **nao deixa** chegar nisso.

| Onde | Campo | Envia |
|---|---|---|
| Na linha | valor em R$ | `itens[i].valorDesconto` |
| No carrinho | valor **ou** percentual | `valorDesconto` **ou** `percentualDesconto` do pedido |

Regra visivel na tela, escrita na propria tela:

> Desconto da linha e desconto do pedido somam. O desconto do pedido incide
> sobre o que sobrou depois das linhas.

A tela mostra o percentual do pedido calculado por extenso, para o caixa ver:
`10% de R$ 49,64 = R$ 4,96`. **Esse numero e' exibicao, nao calculo do total** —
o total vem do backend.

Desconto maior que o subtotal: o backend recusa com 422. A tela mostra a
mensagem como veio.

### [4] Validação de estoque

**Duas checagens, em momentos diferentes, e as duas importam.**

| Momento | O que acontece |
|---|---|
| Ao adicionar no carrinho | consulta o saldo e mostra o disponivel na linha. **Nao bloqueia** — o caixa pode montar a venda e resolver antes |
| Ao faturar | o backend recusa com 422 *"Estoque insuficiente para o produto X"* |

O bloqueio real e no backend. A tela nao substitui essa regra, ela a antecipa
para o caixa nao descobrir no fim.

Saldo insuficiente mostra o que faltou:

> Estoque insuficiente: Cerveja Long Neck — disponível 0,00, solicitado 1,00

### [5] Faturar

| Item | Decisao |
|---|---|
| Botao | "Finalizar venda" — so habilitado com pelo menos uma linha |
| Chamada | `POST /api/vendas/pedidos/{id}/faturar` |
| Sucesso | mostra o numero do pedido, o total e o titulo gerado. Fecha o carrinho. |
| Falha | a mensagem do backend vai para a tela, palavra por palavra. Nao traduzir erro do servidor. |
| Duplo clique | botao desabilitado durante a chamada. Sem isso, dois fetches e titulo duplicado. |

`faturar` tambem baixa o estoque, gera o titulo com parcelas e a comissao do
vendedor. A tela so mostra o resultado.

---

## 3. O QUE A TELHA CONSOME DA API

| Chamada | Quando | Para que serve |
|---|---|---|
| `GET /api/cadastro/produtos` | pesquisa | lista com nome, codigo, preco, unidade, estoque |
| `GET /api/estoque/saldos?produtoIds=...` | ao adicionar | saldo por produto |
| `GET /api/cadastro/clientes?page=0&size=50` | abrir o carrinho | select de cliente |
| `POST /api/vendas/pedidos` | finalizar | cria o pedido, devolve os cinco valores |
| `POST /api/vendas/pedidos/{id}/faturar` | finalizar | baixa estoque, gera titulo |

O `POST /vendas/pedidos` devolve os cinco campos. A tela **nao** volta para
buscar o pedido separado para pintar o total: usa o que veio na resposta.

---

## 4. CASOS OBRIGATORIOS DE REGRESSAO

Validar **a cada entrega**, nos tres segmentos.

### Supermercado (`demo.super`)

| # | Caso | Esperado |
|---|---|---|
| 1 | `SUP-001` Arroz, 2 x 27,90, 5,58 na linha | 55,80 / 5,58 / 0,00 / 5,58 / **50,22** |
| 2 | **`SUP-009` Cerveja, saldo 0** | listado com selo; faturar recusa 422 |
| 3 | **`SUP-013` Cafe, saldo 0** | listado com selo; faturar recusa 422 |
| 4 | 10% no pedido sobre 2 x 50,00 com 10,00 na linha | desconto do pedido **9,00** (sobre 90,00) |
| 5 | valor e percentual juntos | prevented na tela; 400 se chegar |

### Farmacia (`demo.farma`)

| # | Caso | Esperado |
|---|---|---|
| 6 | `FAR-001` Dipirona, 3 x 11,90, 10% | 35,70 / 3,57 / **32,13** |
| 7 | `FAR-005` saldo 0 | selo e recusa no faturar |
| 8 | `FAR-009` Insulina, 12 unidades | 12 e o maior saldo; a unica controlada |

### Tecnologia (`demo.tech`)

| # | Caso | Esperado |
|---|---|---|
| 9 | venda de **servico**, sem estoque | 2 x 149,90 - 29,98 = **269,82** |
| 10 | carrinho so com servico | nao tenta consultar saldo de produto |

**Os dois zerados sao `SUP-009` e `SUP-013` e ficam como regressao permanente.**
Se um dia o saldo deles deixar de ser 0, o caso de "venda sem estoque" deixa
de existir e o PDV passa a parecer que valida estoque.

---

## 5. FORA DA 2A

Fechamento de caixa com varias formas de pagamento, TEF, comandas de bar,
delivery, cupom fiscal. A 2A entrega a venda; o resto entra depois com dado
real de cada uma.

---

## 6. COMO SE VALIDA

```bash
cd bd/demo && ./carregar.sh --reset && ./carregar.sh
```

Sobe a base limpa com os saldos de referencia. Os casos 1 a 10 tem valor
esperado escrito, entao a verificacao e comparar numero, nao olhar a tela.

Tudo que o teste criar e removido depois. O script de limpeza tem que rodar
**dentro de transacao**, e nao com `+2` fixo no saldo — ja errei isso uma vez
hoje e deixei o arroz em 178 em vez de 180.
