> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

---
id: 2026-09-29-investigacao-carga-regra-tributaria
status: investigacao
data: 2026-09-29
---

# DE ONDE VIRIA A CARGA DE `bc_fis_regra_tributaria`?

> Pergunta do dono: *"Quem criou? De onde viria a carga? Existe carga pronta?
> Existe importador pronto? Existe documentacao?"*
>
> **Resposta: a tabela nao tem origem. Nao existe carga, nem fonte, nem plano.
> Ela foi modelada adiante do dado.**

---

## 1. QUEM CRIOU

| | |
|---|---|
| Onde | `src/main/resources/db/migration/V4__fiscal.sql` |
| Quando | commits de **20 a 22/09** (`Euripedes`, `qwen.ai[bot]`) — o esqueleto inicial |
| Como | Uma unica migration, que cria o modulo fiscal inteiro |
| O que ela diz | `COMMENT ON TABLE bc_fis_regra_tributaria IS 'Regras de tributação por NCM/CFOP/UF'` |

O cabecalho do proprio `V4`:

```
-- V4: Módulo fiscal — NF-e, NFS-e, NFC-e, CT-e, MDF-e, impostos, SPED, tabelas oficiais
```

**Nao e' uma tabela esquecida no meio do caminho.** E' uma das tabelas de um
modulo inteiro modelado de uma vez, ao lado de `bc_fis_nfe`, `bc_fis_nfce`,
`bc_fis_apuracao`, `bc_fis_esped`. Nenhuma delas foi populada com dado real
nesse dia: `bc_fis_nfe` tem **3** linhas, `bc_fis_nfce` tem **58** de semente, e
`bc_fis_regra_tributaria` tem **0**.

---

## 2. A INFRAESTRUTURA DE CARGA EXISTE, E E' BOA

Descobri isto, e muda o quadro:

| Peca | Estado |
|---|---|
| `src/main/resources/db/seed/issqn/` | **58 arquivos** (um por UF), 22 MB so `MG.csv` |
| `src/main/resources/db/seed/ncm/` | `Tabela_NCM_Vigente_20260906.xlsx`, 792 KB, **fonte oficial** |
| `src/main/resources/db/seed/processed/` | `ncm_oficial.csv`, `municipios_oficial.csv` |
| `scripts/seed/converter_oficiais.py` | conversor com **decisoes documentadas** |
| `scripts/seed/carregar_oficiais.sql` | carga por `COPY`, grava em `bc_migration_log` |

O docstring do conversor, que e' a coisa mais cuidadosa que achei no projeto
inteiro:

```
Decisões documentadas:
 * NCM: só itens de 8 dígitos (capítulos/posições/subposições são cabeçalho);
   pontos removidos p/ caber em bc_fis_ncm.codigo VARCHAR(8)
 * ISSQN: aliquota em PERCENTUAL (5 = 5%); dt_fim < hoje são descartadas
 * Municípios: distinct(codigo_ibge, nome_municipio, uf) do próprio ISSQN
 * CEP: não processado aqui (preenchimento sob demanda via API ViaCEP, Fase 7)
```

> **Isto responde a uma duvida minha anterior.** O `V4` defineu
> `bc_fis_ncm.codigo VARCHAR(8)` porque o conversor descarta os pontos e usa so
> os **8 digitos**. O NCM de 6 digitos que eu gravei nos produtos da base de
> demonstracao foi **um erro meu**, e a base do projeto sempre foi 8 digitos.

**Mas o conversor trata exatamente 3 saidas:** `ncm_oficial.csv`,
`issqn_oficial.csv`, `municipios_oficial.csv`. E o carregador popula exatamente 3
tabelas: `bc_fis_ncm`, `bc_cad_municipio`, `bc_fis_issqn`.

```
$ grep -icE "regra_tributaria|cfop|cest" scripts/seed/carregar_oficiais.sql
  0
```

---

## 3. AS CINCO FONTES QUE EU VERIFIQUEI — E AS CINCO NAO SERVEM

### 3.1 O sistema legado — tinha a tabela, e ela esta vazia

O esquema `public` do banco legado tem **`fproduto_fiscal`**, com o formato
exatamente certo:

```
codproduto | cest | icms | pis | cofins | iss
```

E: **0 linhas.** E ela **nunca foi migrada** — nao aparece em nenhum dos 23
registros reais do `bc_migration_log`.

### 3.2 A auditoria de cargas — a prova definitiva

`bc_migration_log` guarda o registro de **tudo** que ja foi carregado. Das 81
linhas, 58 sao `SEED` e **23 sao reais**:

```
migration  |         tabela_origem         |         tabela_destino      | origem | destino
V16       | public.tncm                   | bc_fis_ncm              |  10515 |   10515
V16       | public.tservico_lc116         | bc_fis_servico_lc116    |    200 |     200
V16       | public.tcnae_servico          | bc_fis_cnae_servico     |    856 |     856
V16       | public.tissqn                 | bc_fis_issqn            | 1759790 | 1759790
V16       | public.tmunicipio             | bc_cad_municipio        |   5342 |    5342
V18-ofic  | ncm_oficial.csv               | bc_fis_ncm              |  10515 |   10515
V18-ofic  | municipios_oficial.csv        | bc_cad_municipio        |   4920 |    5342
V18-ofic  | issqn_oficial.csv             | bc_fis_issqn            | 1765612 | 1759790
```

**`bc_fis_regra_tributaria` nao esta em nenhum desses registros.** Nem no legado
(`V16`/`V17`), nem nas cargas oficiais (`V18-ofic`).

O legado tinha 5 tabelas fiscais de referencia — `tncm`, `tissqn`, `tmunicipio`,
`tcnae_servico`, `tservico_lc116` — e **nenhuma delas e' tabela de regras**.

### 3.3 Os arquivos oficiais — nao tem CST nem ICMS

| Arquivo | Colunas | Tem CST? | Tem ICMS por NCM x CFOP x UF? |
|---|---|---|---|
| `Tabela_NCM_Vigente_20260906.xlsx` | `codigo, descricao, dt_ini, dt_fim, ato, numero, ano` | **nao** | **nao** (so `aliquota_ipi`) |
| `issqn/<UF>.csv` | `codigo_ibge, uf, nome_municipio, codigo_servico, incidencia, aliquota, dt_ini, dt_fim` | **nao** | **nao** (so alíquota de ISS por servico) |

O NCM oficial traz **IPI**. O ISSQN oficial traz **alíquota de serviço**. Nenhum
dos dois traz **CST de ICMS** — que e' o campo que a regra precisa resolver.

### 3.4 CFOP e CEST — foram outra vez, por migration à mao

```
V100__cfop_e_cest_do_oca.sql   (1.728 linhas, 4 INSERTs literais)
```

E o comentario dessa migration e' **o melhor documento do caso**, porque
descreve exatamente o problema que voce tema:

> "As duas tabelas EXISTIAM no ERP, as duas telas EXISTIAM, e as duas estavam
> com zero linhas. A tela de CFOP e a de CEST abriam, carregavam, mostravam
> 'nenhum encontrado' e nao diziam nada. Sem erro, sem aviso, sem log — so a
> tabela vazia. Nao e tela de cadastro: sao tabelas de referencia, e o cadastro
> delas e' por migration mesmo.
> CFOP 619 linhas / CEST 1.043 linhas — a partir das tabelas do OCA l10n-brazil
> (`l10n_br_fiscal/data`)."

**Ou seja: alguem ja encontrou exatamente esta classe de problema, corrigiu para
CFOP e CEST, e a `bc_fis_regra_tributaria` ficou de fora.**

### 3.5 A documentacao — uma linha de inventario

`docs/fase-08-fiscal.md`, linha 12:

```
- `bc_fis_regra_tributaria` — Regras de tributação por NCM/CFOP/UF
```

Lista, e nada mais. Nenhum plano de carga, nenhuma fonte, nenhuma decisão.

---

## 4. RESPOSTA DIRETA

| Pergunta | Resposta |
|---|---|
| **Quem criou?** | O `V4__fiscal.sql`, em 20-22/09, no esqueleto inicial do modulo fiscal |
| **De onde viria a carga?** | **De lugar nenhum que exista no projeto** |
| **Existe carga pronta?** | **Nao.** Nenhum arquivo, script ou migration |
| **Existe importador pronto?** | **Sim, e bom** — `converter_oficiais.py` + `carregar_oficiais.sql`, mas para NCM, ISSQN e municipios. Estender e' trabalho pequeno |
| **Existe documentacao?** | Uma linha de inventario |

> **A tabela foi desenhada adiante do dado.** Nao e' carga abandonedada: e' schema
> sem fonte. Nao ha "carga que nunca foi importada" — **nao ha carga**.

---

## 5. A PREGUNTA QUE FICA, E QUE EU NAO VOU RESPONDER SOZINHO

Se a fonte nao existe no projeto, ela precisa vir de fora. Os candidatos reais:

| Candidato | O que se sabe | O que falta saber |
|---|---|---|
| **OCA l10n-brazil** | Ja e' a fonte de CFOP e CEST, por `V100`. Tem `fiscal.icms`, `fiscal.product_fiscal_template` | **Nao verifiquei se tem CST x NCM x CFOP x UF.** E' a unica pista concreta do projeto |
| Tabela da SEFAZ por UF | E' dado publico | Trabalho de dados, e regra muda por estado |
| Legado do cliente | `fproduto_fiscal` tem o formato, mas 0 linhas | Nao ha dado a recuperar |
| **Contratar/calcular por legislacao** | O CST depende de origem, destino, NCM, CFOP e regime | Nao e' carga: e' motor de regra, e o mais caro dos quatro |

**Nenhuma destas eu starto sem sua ordem.** A sua regra — *"nao semeie CST, ICMS,
IPI, PIS, COFINS em massa"* — esta correta e eu a respeito.

---

## 6. O QUE ISSO FAZ COM A FILA

A prioridade 2 (tributacao) tem um **bloqueio de dado que nao e' de codigo**, e
que so se resolve escolhendo a fonte. Isso **confirma a sua reordenacao**:

| | |
|---|---|
| **1. Importacao de NF-e** | 445 linhas prontas, backend pronto. **Nada bloqueia.** |
| **2. Tributacao** | Depende de decidir a fonte da carga. **Antes disso so da para fazer telas de cadastro, que nao alimentam nada** |
| **3. A1 + SEFAZ** | Comercial |
| **4. NFC-e** | Depende de 1, 2 e 3 |

E uma correcao de dado que eu devo fazer, porque e' minha: os **44 produtos da
base de demonstracao** estao com NCM de 6 digitos, e a base do projeto e' 8. Nao
e' migration — e' carga.

---

## 7. COMO VERIFICAR

```bash
cd ~/BrasilCloudERP
grep -n "bc_fis_regra_tributaria" src/main/resources/db/migration/V4__fiscal.sql
head -30 scripts/seed/converter_oficiais.py          # as 3 saidas documentadas
grep -icE "regra_tributaria|cfop|cest" scripts/seed/carregar_oficiais.sql   # 0
ls src/main/resources/db/seed/*/                      # issqn, ncm, processed
head -14 src/main/resources/db/migration/V100__cfop_e_cest_do_oca.sql      # o precedente
grep -i "regra_tributaria" docs/fase-08-fiscal.md    # 1 linha

PGPASSWORD=ALTERE_ME psql -h 127.0.0.1 -U postgres -d brasil-saas -c \
  "select count(*) from brasil_saas.bc_fis_regra_tributaria;" \
  -c "select count(*) from public.fproduto_fiscal;" \
  -c "select migration, tabela_origem, tabela_destino from brasil_saas.bc_migration_log
      where migration not like 'SEED%';"
```

