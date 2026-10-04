> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

---
id: 2026-09-29-qualificacao-ncm-produtos
status: aplicado
data: 2026-09-29
---

# QUALIFICACAO DO NCM DOS PRODUTOS

> Passo 1 da fila. Regra do dono: sem correcao automatica por suposicao.
> Aplicado: **22 produtos**. Nao aplicados: **194**, com motivo.

---

## 1. O QUE FOI APLICADO

`bd/correcoes/ncm_deterministico.sql` — 12 `UPDATE`, que corrigem **22 produtos**.

O criterio foi determinismo verificavel: **cada NCM de 6 digitos tem
EXATAMENTE UM codigo de 8 digitos na tabela oficial.** A correcao e a expansao do
proprio nivel de 6 digitos do NCM oficial — derivacao, nao suposicao.

| Antes (6 digitos) | Depois (8 digitos) | Descricao oficial | Produtos |
|---|---|---|---|
| `2203.00` | `22030000` | Cervejas de malte | 4 |
| `2202.10` | `22021000` | Aguas, incluindo as aguas minerais | 4 |
| `0901.21` | `09012100` | Nao descafeinado | 3 |
| `1602.10` | `16021000` | Preparacoes homogeneizadas | 2 |
| `1701.99` | `17019900` | Outros | 2 |
| `4818.10` | `48181000` | Papel higienico | 1 |
| `3004.31` | `30043100` | Que contenham insulina | 1 |
| `0702.00` | `07020000` | Tomates, frescos ou refrigerados | 1 |
| `0201.30` | `02013000` | Desossadas | 1 |
| `2005.20` | `20052000` | Batatas | 1 |
| `2009.19` | `20091900` | Outros | 1 |
| `0803.90` | `08039000` | Outras | 1 |

A cada linha corresponde a descricao que **confirma** o producto. Nenhuma
correcao foi aceita so porque o formato batia.

**Verificacao depois de aplicado:** os 22 produtos com NCM numerico de 8 digitos
casam **22 de 22** com `bc_fis_ncm`. Nenhum caso solto.

---

## 2. A PROVA DE QUE A CADEIA ESTA CERTA

O caminho completo, do cadastro ate a regra, produziu as alquotas reais:

| Produto | NCM | IPI | Conferido |
|---|---|---|---|
| Cerveja Long Neck | `22030000` | **3,90%** | e' a alíquota real da TIPI para cervejas |
| Coca-Cola 2L | `22030000`… `22021000` | **2,60%** | e' a alíquota real para refrigerantes |
| Acucar Cristal 1kg | `17019900` | 0,00% | nao tributado |
| Cafe Torrado e Moido 500g | `09012100` | 0,00% | nao tributado |

Nao foi eu que escolhi 3,90%. Veio de `l10n_br_fiscal.tax.csv`, cruzando o NCM
oficial com o NCM cadastrado. **E' validacao externa, e nao o contrario.**

**22 de 22 produtos com NCM numerico ja resolvem regra.**

---

## 3. O QUE NAO FOI APLICADO, E POR QUE

### 3.1 Ambiguos — 18 produtos, 10 NCMs

O NCM de 6 digitos expande para **varios** codigos de 8. Escolher um e' decisao
fiscal, e a escolha muda a nota.

| NCM | Produtos | Candidatos | Situacao |
|---|---|---|---|
| `3004.90` | 7 (Dipirona, Paracetamol, Ibuprofeno, Losartana, Metformina, Omeprazol, Loratadina) | **68** | O 8 digito depende do principio ativo e da forma farmaceutica. Cada medicamento tem codigo proprio |
| `0207.14` | 1 | 14 | — |
| `2106.90` | 1 | 8 | — |
| `3004.10` | 1 | 7 | — |
| `0713.33` | 1 | 6 | — |
| `1507.90` | 2 | 3 | — |
| `3401.11` | 1 | 2 | — |
| `3304.99` | 1 | 2 | — |
| `1905.20` | 2 | 2 | — |
| `0401.10` | 1 | 2 | — |

### 3.2 Inexistentes — 4 produtos, 2 NCMs

**Estes codigos nao existem na tabela oficial.** Nao e' ambiguidade: e' erro.

| NCM cadastrado | Produtos | O que acontece |
|---|---|---|
| `1905.21` | Arroz Tipo 1 5kg (2) | **Arroz e' capitulo 10.** A tabela tem `10063011`, `10063019`, `10063021`… O `1905.21` nao existe |
| `3402.20` | Detergente Neutro 500ml, Detergente Concentrado 5L (2) | O capitulo 3402 existe, mas **nao tem 3402.20** em 8 digitos. O mais proximo e' `34023100` |

Estes precisam de classificacao nova, e ela depende do produto concreto.

### 3.3 Sem classificacao — 172 produtos

`ncm = 'SEED NCM'`. Sao as linhas de semente do `V4`. Nao ha nada a corrigir:
nao existe informacao. A correcao delas depende de o produto existir de verdade
no cadastro.

### Resumo

| Faixa | Produtos | Situacao |
|---|---|---|
| **A. 8 digitos numericos** | **22** | **corrigido e verificado** |
| B. 6 digitos, ambiguo | 18 | decisao fiscal pendente |
| C. 6 digitos, inexistente | 4 | classificacao errada |
| D. `SEED NCM` | 172 | sem classificacao |

---

## 4. O QUE ISSO NAO BLOQUEIA

Produto sem NCM valido **nao impede a venda**. O que impede e' a **emissao** do
documento fiscal, e a mensagem tem que dizer qual produto esta sem NCM. E' o
mesmo principio do estoque: continua visivel, com aviso.

Para o PDV, nenhum dos 194 pendentes e' travamento. Para a NFC-e, sao a fila de
pendencia de emissao.

---

## 5. COMO VERIFICAR

```bash
Q() { sudo -u postgres psql -d brasil-saas "$@"; }

Q -c "select case
        when ncm ~ '^[0-9]{8}\$' then 'A. 8 digitos (corrigido)'
        when ncm like '%.%'       then 'B. 6 digitos'
        else 'C. SEED' end faixa, count(*)
      from brasil_saas.bc_cad_produto
      where deleted_at is null and ncm is not null group by 1 order by 1;"

# o caminho completo: produto -> regra -> IPI
Q -c "select p.nome, p.ncm, r.aliquota_ipi
      from brasil_saas.bc_cad_produto p
      join brasil_saas.bc_fis_regra_tributaria r
        on r.ncm = p.ncm and r.empresa_id = p.empresa_id
      where p.deleted_at is null and p.ncm ~ '^[0-9]{8}\$' limit 10;"

cat bd/correcoes/ncm_deterministico.sql
```

