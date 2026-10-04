> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

---
id: 2026-09-29-carga-regra-tributaria
status: aplicado
data: 2026-09-29
migration: V108__regra_tributaria_ipi_oficial.sql
---

# CARGA DE `bc_fis_regra_tributaria` — IPI por NCM

> Documentacao exigida pelo dono: origem, data, arquivo, criterio, campos
> preenchidos e campos pendentes. Está tudo aqui, e tambem dentro da migration.

---

## 1. O QUE FOI CARREGADO

| | |
|---|---|
| Linhas | **73.605** |
| Empresas | 7 (a tabela e' `NOT NULL` em `empresa_id`) |
| Com aliquota de IPI | **72.884** (99,0%) |
| Com `aliquota_ipi` NULA | 721 (103 NCMs sem IPI na fonte, x 7 empresas) |
| Aliquotas distintas | **43** |
| **Campos preenchidos por deducao** | **0** |

Verificado depois de aplicado:

```
 total | com_ipi | empresas | aliquotas | invented
-------+---------+----------+-----------+----------
 73605 |   72884 |        7 |        43 |        0
```

A coluna `invented` conta linhas com **qualquer** um destes preenchido:
`cst_icms`, `aliquota_icms`, `cst_pis`, `cst_cofins`, `aliquota_st`, `cfop`.
**Zero.** Nenhum valor foi estimado.

---

## 2. A BUSCA ANTES DE CARREGAR

A diretriz era: usar primeiro o que ja existe no projeto. A busca foi exaustiva.

| Fonte | Resultado |
|---|---|
| `parte_dump_01..08.zip` (8 PGDMP, 137 MB) | tabela **vazia** nos dumps |
| `dump/schema_brasil_saas_10mil.sql` | INSERT **sintetico**: `aliquota = 1 + (g % 1000)` |
| `dump/seed_desenvolvimento.sql` | INSERT **sintetico**: `ncm='SEED NCM'`, `cst='SEED '`, taxas como `954.3501` |
| `bd/fproduto_fiscal.csv` | 36 bytes — **so o cabecalho** |
| `bc_migration_log` (23 registros reais) | a tabela **nao aparece** |
| `db/seed/` | NCM (xlsx oficial), ISSQN (58 CSV) — **nenhum com CST** |
| `scripts/seed/` | carrega 3 tabelas, **zero** mencao a regra |
| `V4__fiscal.sql` | so cria a tabela |
| `docs/fase-08-fiscal.md` | 1 linha de inventario |

> **Nenhuma carga real existia. Os dois INSERTs que existiam eram ruido
> sintetico** — exatamente o que a diretriz proibe usar.

---

## 3. A FONTE USADA

| | |
|---|---|
| **Origem** | OCA/l10n-brazil, tag **18.0** — repositorio publico |
| **Data da consulta** | 2026-09-29 |
| **Arquivos** | `l10n_br_fiscal.ncm.csv` (3,9 MB) e `l10n_br_fiscal.tax.csv` |
| **URL** | `https://raw.githubusercontent.com/OCA/l10n-brazil/18.0/l10n_br_fiscal/data/` |
| **Copia no repositorio** | `src/main/resources/db/seed/processed/regra_tributaria_ipi.csv` (728 KB, 10.515 linhas) |
| **porque essa fonte** | E' a **mesma** que o `V100__cfop_e_cest_do_oca.sql` ja usou para CFOP e CEST. Nao foi introduzida dependencia nova |

**Cadeia de derivacao, campo a campo:**

```
l10n_br_fiscal.ncm.csv
    code            = "0101.21.00"   (com ponto, 8 digitos)
    tax_ipi_id:id   = "tax_ipi_nt"   -> 11.540 de 11.543 NCMs com IPI
        |
        v
l10n_br_fiscal.tax.csv
    id              = "tax_ipi_nt"
    percent_amount  = "3.25"          -> 61 definicoes tax_ipi_*
        |
        v
normalizacao: tira o ponto do code  ->  "01012100"  (8 digitos, formato do ERP)
cruzamento com bc_fis_ncm (10.515)   ->  10.412 casam  (99,0%)
```

**Criterio aplicado:** uma regra por NCM, com `cfop`, `uf_origem` e `uf_destino`
**nulos**. E' a regra mais estreita que os dados permitem — "para este NCM, o IPI
e' X" — porque **IPI nao varia por CFOP nem por UF**. Qualquer recorte alem disso
seria invencao.

**NCM e descricao** vieram da propria `bc_fis_ncm` do projeto, que foi carregada
da tabela oficial (Resolucao Gecex nº 926/2026, vigente em 06/09/2026).

**Descricao truncada:** 623 descricoes passam de 100 caracteres (`nome` e'
`varchar(100)`; o maior tinha 1.069). Foram cortadas em 97 + "...". **O texto
completo permanece em `bc_fis_ncm.descricao`, que e' `TEXT`** — nada se perde, e
`ncm` e' a chave de juncao.

---

## 4. OS CAMPOS QUE FICARAM PENDENTES, E POR QUE

| Campo | Por que ficou NULO |
|---|---|
| `cst_icms` | O CST e' escolhido pela **situacao fiscal da operacao**. Nao e' derivado do CFOP nem do NCM. A propria OCA modela CST como campo da linha da nota: `l10n_br_fiscal.cst.csv` tem `cst_type = all` e **nenhuma ligacao com CFOP**. Preencher seria inventar a relacao |
| `aliquota_icms` | Definida por **lei complementar estadual**, e a incidencia por NCM e' **por UF**. Nao existe tabela nacional. A OCA so tem catalogo de taxas possiveis (`tax_icms_0`, `_1`, `_10`, `_12`), sem dizer qual NCM se aplica a qual UF |
| `cst_pis`, `aliquota_pis` | Dependem do **regime tributario** (Simples Nacional x Normal), que e' escolha da empresa |
| `cst_cofins`, `aliquota_cofins` | Idem |
| `aliquota_st` | Depende de **convenios estaduais** e do par NCM/CEST |
| `cfop`, `uf_origem`, `uf_destino` | Seriam recorte artificial: IPI nao depende de nenhum dos tres. Deixados nulos de proposito, nao por falta de dado |

> **Resumo do que e' possivel e' possivel.** IPI e' nacional e vem de tabela
> oficial. ICMS e' estadual. PIS/COFINS dependem do regime. CST depende da
> operacao. Com os dados publicos que existem, **IPI e' o unico campo que se
> pode preencher com honestidade.**

---

## 5. AS DUAS COLUNAS NOVAS

```sql
ALTER TABLE bc_fis_regra_tributaria
    ADD COLUMN IF NOT EXISTS origem           VARCHAR(64),
    ADD COLUMN IF NOT EXISTS origem_referencia VARCHAR(180);
```

Tres motivos, e o terceiro e' o que mais importa:

1. **A tabela e' por empresa** (`empresa_id NOT NULL`), mas a TIPI e' nacional.
   Com o schema atual, o dado e' duplicado 7 vezes. Inevitavel sem mudar o
   schema — e e' por isso que o campo de origem importa.
2. Sem procedencia, nao ha como distinguir **dado oficial** de **dado de semente**
   na mesma tabela. As duas coisas ja coexistem: `dump/seed_desenvolvimento.sql`
   insere `ncm='SEED NCM'` nessa tabela.
3. `origem` torna a carga **idempotente** sem depender de indice unico — que a
   tabela nao tem. A grain real e' `(empresa, ncm, cfop, uf_origem, uf_destino)`,
   e o NCM sozinho nao a determina, entao um `UNIQUE(empresa_id, ncm)` seria
   errado e impediria regras legitimas do mesmo NCM com CFOPs diferentes.

Como ler a tabela a partir de agora:

```sql
-- dado de carga oficial
select * from bc_fis_regra_tributaria where origem = 'oca-l10n-brazil/tipi';

-- NCMs sem IPI na fonte (ficaram nulos)
select * from bc_fis_regra_tributaria where origem = 'sem-ipi-na-fonte';

-- dado de semente: NAO usar em documento fiscal
select * from brasil_saas.bc_fis_regra_tributaria where origem is null;
```

---

## 6. A DECISAO QUE EU TOMEI, E QUE E' SUA PARA REVOGAR

A tabela e' `NOT NULL` em `empresa_id`. Carreguei para as **7 empresas**, o que
duplica a TIPI 7 vezes (73.605 linhas em vez de 10.515).

**Alternativa que eu NAO fiz, e que talvez queira:** derivar a regra por UF no
momento da venda, e deixar `bc_fis_regra_tributaria` como tabela por UF — que e'
mais o desenho correto, porque ICMS e' estadual. Isso implicaria uma migration
de remodelagem, e nao fiz sem ordem.

---

## 7. O QUE FALTA PARA A VENDA USAR ISSO

Carregar a tabela **nao liga a tributacao na venda.** Ainda falta:

1. Um `RegraTributariaService` — hoje `grep -rln "RegraTributaria"` devolve
   **so o model e o repository**. Nao ha service.
2. `PedidoVendaServiceImpl` nao tem **uma unica linha** de tributacao
   (`grep -niE "icms|ipi|pis|cofins"` em `vendas/` → nada).
3. Corrigir o NCM dos produtos: **172 tem `SEED NCM`** e os **44 da base de
   demonstracao tem 6 digitos** (`3004.90`), quando o projeto inteiro trabalha
   com 8 (`01012100`). Sem isso, nenhum produto casa com a regra — foi o que a
   verificacao de join mostrou.

> O passo 3 **nao** foi feito, e continua valendo a sua regra: sem correcao
> automatica. Alem disso, alterar `bc_cad_produto.ncm` e' um dado cadastral, e
> quem deve definir o NCM certo de cada produto e' quem cadastra o produto.

---

## 8. COMO VERIFICAR

```bash
cd ~/BrasilCloudERP
PGPASSWORD=ALTERE_ME psql -h 127.0.0.1 -U postgres -d brasil-saas -c "
select count(*) total,
       count(*) filter (where aliquota_ipi is not null) com_ipi,
       count(distinct empresa_id) empresas,
       count(distinct aliquota_ipi) aliquotas,
       count(*) filter (where cst_icms is not null or aliquota_icms is not null
                          or cst_pis is not null or cst_cofins is not null
                          or aliquota_st is not null or cfop is not null) invented
from brasil_saas.bc_fis_regra_tributaria;"

# a fonte, no repositorio
head -3 src/main/resources/db/seed/processed/regra_tributaria_ipi.csv
wc -l src/main/resources/db/seed/processed/regra_tributaria_ipi.csv
```

