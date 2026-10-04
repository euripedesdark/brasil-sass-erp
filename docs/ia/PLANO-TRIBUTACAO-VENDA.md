> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

---
id: 2026-09-29-plano-tributacao-venda
status: levantamento
data: 2026-09-29
---

# PRIORIDADE 1: TRIBUTACAO DA VENDA

> Medido antes de implementar. Achado principal: **a estrutura esta toda certa e
> vazia.** Nenhuma migration de schema e necessaria.

---

## 1. O QUE JA EXISTE (e estava funcionando)

| Peca | Estado |
|---|---|
| `bc_cad_produto.ncm` | **`varchar(8)`** |
| `bc_cad_produto.cfop_padrao` | **`varchar(4)`** |
| `bc_cad_produto.cest` | **`varchar(7)`** |
| Tela de produto | **ja tem** `ncm`, `cfopPadrao`, `cest` (`CadastroProdutos.jsx` linhas 42-44, 164-165) |
| `ProdutoRequest` | ja aceita, com `@Size(max=8)`, `max=4`, `max=7` |
| `bc_fis_regra_tributaria` | **schema completo**: `nome, ncm(8), cfop(4), uf_origem(2), uf_destino(2), cst_icms(5), aliquota_icms, cst_ipi, aliquota_ipi, cst_pis, aliquota_pis, cst_cofins, aliquota_cofins, aliquota_st, ativa, prioridade` |
| `RegraTributaria.java` + repository | existem |
| Acervo carregado | CFOP **619**, NCM **10.515**, CEST **1.043**, ISSQN **1.759.790** |

**O schema da tributacao esta correto e completo. Nao falta coluna.**

---

## 2. AS QUATRO LACUNAS REAIS

### Lacuna 1 — a tabela de regras esta **vazia**

```
select count(*) from brasil_saas.bc_fis_regra_tributaria;   ->  0
```

**E a regra que resolve CST e alíquota esta em zero linha.** Sem ela, nao ha como
descobrir o CST de um produto, que e' o que o XML fiscal exige.

### Lacuna 2 — **ninguem** usa a regra

`grep -rln "RegraTributaria" src/main/java` devolve **dois** arquivos:

```
fiscal/model/RegraTributaria.java
fiscal/repository/RegraTributariaRepository.java
```

**Nao ha service. Nao ha controller.** O modelo e o repository foram escritos e
pararam ali.

### Lacuna 3 — o calculo da venda nao tem uma linha de tributacao

```
grep -rniE "ncm|cfop|cest|icms|ipi|tributa" src/main/java/br/com/brasil_saas/vendas/
  -> NADA
```

O `CalculoDesconto` que fiz resolve desconto. **Nao existe equivalente para
imposto.** A `PedidoVendaResponse` expoe 5 campos de valor, e nenhum e' fiscal.

### Lacuna 4 — o NCM gravado nos produtos nao identifica um NCM fiscal

Este e' um **defeito de dado**, e parte dele e' meu (base de demonstracao da
Fase 1).

| Onde | Formato | Exemplo |
|---|---|---|
| `bc_cad_produto.ncm` | **6 caracteres** | `3004.90` |
| `bc_fis_ncm.codigo` | **8 caracteres** | `30049000` |

O NCM do produto e' um **prefixo** do fiscal, e a correspondencia e' ambigua:

| NCM do produto | produtos | NCM fiscais candidatos |
|---|---|---|
| `3004.90` | 7 | **68** |
| `0207.14` | 1 | 14 |
| `2106.90` | 1 | 8 |
| `3004.10` | 1 | 7 |
| `1507.90` | 2 | 3 |

E dos 216 produtos com NCM, **172 tem `SEED NCM`** — valor de semente, invalido.

**A coluna e' `varchar(8)` e aceita 8 digitos.** Entao isto **nao e' migration**:
e' correo de dado dos 44 produtos da base de demonstracao (meu) e dos 172 de
semente.

---

## 3. O QUE ISSO MUDA NO ESTIMADO DA PRIORIDADE 1

Eu escrevi antes que a prioridade 1 era *"9 endpoints CRUD + tela"*. **E' mais que
isso, e menos que uma migration.**

| | Custo |
|---|---|
| Telas de CFOP, CEST, NCM, imposto, ISSQN (9 endpoints CRUD) | o mais barato — sao CRUD |
| Semeiar `bc_fis_regra_tributaria` com as regras reais | **dado, nao codigo** — depende de fonte (SEFAZ, tabela do SEFAZ, ou legislacao) |
| Resolver tributacao de um item (NCM + CFOP + UFs -> CST e aliquotas) | **service novo, pequeno** |
| Ligar no `CalculoDesconto` / `PedidoVendaServiceImpl` | **a unica mudanca de risco** |
| Corrigir o NCM dos produtos | dado |

**A ordem de risco e' invertida em relacao a aparencia:** o mais barato (telas de
cadastro) nao serve para nada sozinho, e o mais caro (ligar no calculo da venda)
e' o que so faz sentido depois das regras existirem.

---

## 4. REGRA QUE DECIDI ADOTAR (para o calculo)

O principio do projeto: **o backend calcula, o frontend mostra.** Aqui fica mais
duro, porque imposto tem CST e aliquota que o usuario nao deve digitar na venda.

Proposta:

1. A regra e' resolvida **no backend**, no item do pedido, no momento do calculo.
2. A tela **nao tem campo de imposto** na venda. Ela mostra o resultado, se
   mostrar.
3. Se o produto nao tem NCM valido, a venda **nao e' bloqueada** — o PDV vende
   assim mesmo. O que falha e' a **emissao** do documento fiscal, e a mensagem
   tem que dizer qual produto esta sem NCM.

> Isso e' o mesmo principio que ja vale para o estoque: produto sem estoque
> continua visivel, com aviso. Produto sem NCM nao impede a venda; impede a nota.

---

## 5. PERGUNTA QUE PRECISO RESPONDIDA ANTES DE CODAR

> **De onde vem a populacao de `bc_fis_regra_tributaria`?**

A tabela tem o schema exato, e 0 linhas. Sem isso, a prioridade 1 entrega telas de
cadastro bonito que nao alimentam nada. As opcoes que eu vejo:

| Opcao | Observacao |
|---|---|
| Semear por NCM x CFOP x UF, com CST/aliquota fixos | viável como partida, mas simplifica o que a SEFAZ nao simplifica |
| Importar a tabela de regras oficial | e' dado publico, mas e' trabalho de dados, nao de codigo |
| Derivar de outra fonte que ja exista no sistema | **preciso verificar**: `bc_fis_imposto` (58 linhas) e `bc_fis_cest` (1.043) ja tem aliquota por produto |

Nao vou semear CST em massa sem sua ordem: aliquota errada em nota e' pior que
nota sem emitir.

---

