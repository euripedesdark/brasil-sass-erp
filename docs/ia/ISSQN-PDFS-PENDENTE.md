> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Pendente: os PDFs de ISSQN nunca foram carregados

Data: 27/09/2026
Status: **nao feito** — os PDFs estao no repo, os dados nao estao no banco

Este arquivo existe para nao se perder o que foi apurado sobre os dois PDFs de
ISSQN que entraram no repo e o que ainda falta fazer com eles.

---

## Os arquivos

```
src/main/resources/db/seed/issqn/TABELA_ISSQN_2026.V1.1.pdf          952.973 bytes, 88 paginas
src/main/resources/db/seed/issqn/lista-de-servicos-lei-complementar-n-1162003-atualizada-29112024.pdf.pdf
                                                                        137.103 bytes, 9 paginas
```

O primeiro e a tabela do art. 53 da LC 1800/90 (CTM). O segundo e a lista de
servicos da LC 116 com a regra de incidencia.

---

## O que foi apurado, e nao implementado

### `TABELA_ISSQN_2026.V1.1.pdf` — 6 colunas

```
Item LC116 | Item LM5232 | CNAE | Cod. Tributacao do Servico | Cod. Tributacao do Municipio | Descricao
  1.01          1.01    1.023.01-2         —                1.01/102301        ANALISTA DE SISTEMAS
  1.02          1.02    1.023.05-5         —                1.02/102305        PROGRAMADOR
```

- **1.998 CNAEs distintos**, vinculados
- **573 codigos de tributacao municipal** distintos, no formato `1.01/102301`

### `lista-de-servicos-...pdf` — a regra de incidencia

```
31.01  Servicos tecnicos em edificacoes...    LEP   5%
34.01  Servicos de investigacoes particulares LEP   5%
```

- **LEP** — ISS devido no municipio onde o prestador esta estabelecido
- **LPS** — ISS devido no municipio onde o servico foi prestado (excecoes do
  art. 3º da LC 116)

---

## O que existe no banco hoje

| tabela | linhas | origem |
|---|---|---|
| `bc_fis_servico_lc116` | 200 | migration V16 |
| `bc_fis_cnae_servico` | 856 | migration V100 |
| `bc_fis_issqn` | 1.759.790 | os 27 CSVs por UF |
| `bc_fis_regra_tributaria` | 58 | seed — **linhas falsas**, ver `TESTE-SUITE-E-RENAME-INPI.md` secao 6.2 |

**Nenhuma** tabela com CNAE ligado a codigo de tributacao municipal, que e o
dado unico dos PDFs.

---

## A lacuna real

### 1. O ERP nao sabe o CNAE de um servico

`bc_fis_issqn` nao tem coluna de CNAE. A tabela `bc_fis_cnae_servico` guarda
856 pares, e a lista antiga do OCA — o PDF traz **1.998**, mais que o dobro.

### 2. O `codigo_municipal` esta preenchido com copia do `codigo`

Em **todas** as 1.759.790 linhas:

```
codigo           | 04.05.01.000
codigo_municipal | 04.05.01.000     <- igual
```

O PDF traz o formato verdadeiro, `04.05/40501`, que e o que a prefeitura de
Sao Paulo exige no XML. A diferenca de formato na base municipal e na ordem de
gravacao do codigo.

### 3. A regra LEP/LPS nao existe

Nao ha lugar no banco que responda **qual** municipio tributa. E a duvida real
do ISS — a aliquota vem dos CSVs, mas a incidencia depende de onde o servico
foi prestado.

### 4. O codigo de LM 5232 nao existe

Presente no PDF, ausente no ERP. Baixa prioridade.

---

## O que NAO esta no PDF

Nada por municipio. Os 27 CSVs tem **1.765.610 linhas** (codigo × municipio ×
UF, com aliquota e vigencia) e o ERP tem 1.759.790.

Os PDFs nao tem essa dimensao — sao 88 e 9 paginas. **A granularidade municipal
continua vindo dos CSVs.** O PDF complementa, nao substitui.

E nao ha lacuna de municipio: `bc_fis_issqn` referencia 5.340 codigos IBGE e
`bc_cad_municipio` tem 5.343. **Zero faltando.** Os 3 que tem tabela e nao tem
ISSQN sao Santa Cruz de Minas (MG), Sao Paulo (SP) e Brasilia (DF).

---

## O que foi pedido e nao foi feito

> "cria uma tabela so dos servicos do issqn ja que temos as cidades e os
> estados"

Nao foi feito. E a tabela de servicos que **ja existe** —
`bc_fis_servico_lc116`, 200 linhas — e o que o pedido descreve, com uma
diferenca de chave que vale registrar:

| | codigo |
|---|---|
| `bc_fis_servico_lc116` | `01.01` — item da LC 116, 2 niveis |
| `bc_fis_issqn` | `01.01.01.000` — codigo nacional, 5 niveis |

Nao e um gap de 200 contra 333: **sao granularidades diferentes.** O `01.01` e
o pai do `01.01.01.000`. O join e `left(issqn.codigo, 5)`, e
**1.754.523 das 1.759.790 linhas resolvem** assim. Uma unica nao resolve.

---

## Como fazer, quando for fazer

Tres dados de natureza diferente, que nao devem ir para a mesma tabela:

1. **CNAE ↔ codigo tributacao municipal** — tabela nova, 1.998 linhas. E o que
   resolve a recusa `[1001] XML nao compativel com Schema`.
2. **Regra LEP/LPS por item da LC 116** — coluna nova, ou tabela
   `bc_fis_servico_lc116_incidencia`. Decide *qual* municipio tributa.
3. **Codigo de LM 5232** — baixa prioridade.

O caminho seguro e o mesmo que o rename usou: **migration nova** para criar a
estrutura, e um **gerador Python** para extrair o PDF e popular, com dry-run
antes. Os dois PDFs sao de tabela, nao de API, entao nao ha endpoint para
consultar.

Nao jogar os PDFs direto no `bc_fis_issqn`: sao 3 dados de naturezas
diferentes em 97 paginas, e essa tabela ja tem 1,76 milhao de linhas com
descricao duplicada.
