---
id: 2026-09-29-busca-inteligente-fiscal
status: backend pronto
data: 2026-09-29
---

# BUSCA INTELIGENTE DE CÓDIGO FISCAL

> O usuário cadastra produto pensando no negócio, não na classificação. A
> evidência de que isso custa caro está no próprio banco.

---

## 1. O PROBLEMA, MEDIDO

| Fato | Onde |
|---|---|
| 4 produtos com NCM `1905.21` (arroz) — e arroz é capítulo 10 | `bc_cad_produto` |
| 2 produtos com `3402.20` (detergente) — **código que não existe** | idem |
| 7 medicamentos em `3004.90`, que tem **68** códigos possíveis | idem |
| 172 produtos com `ncm = 'SEED NCM'` | idem |

A causa: as telas de CFOP, CEST, NCM, ISSQN e imposto **filtram por descrição ou
por código exato**. E no ISSQN não havia busca por texto nenhum. Quem cadastra um
café não sabe que é `09012100`; sabe que é café. A única pista que sobrava era a
palavra "descafeinado" na descrição oficial.

---

## 2. O QUE FAZ

`GET /api/fiscal/busca?q=<termo>&tabela=<ncm|issqn|cfop>&limite=<n>`

O ranking é do servidor. Cada linha volta com **o motivo da correspondência** —
sem isso a busca vira adivinhação, e ninguém confia numa busca em que não
entende o resultado.

| Nível | Peso | O que casa |
|---|---|---|
| Código exato | 100 | `22030000` |
| Prefixo do código | 90 | `2203.00`, `220300` → `22030000` |
| Palavra-chave exata | 80 + peso | `cerveja` → `22030000` |
| Prefixo de palavra | 70 + peso | `cervej` → `22030000` |

**A normalização é o que faz o usuário não precisar saber a pontuação oficial:**

| O usuário digita | O que a busca entende | Acha |
|---|---|---|
| `107` | `107` contra `10701000` | `01.07.01.000` Suporte técnico |
| `1.07` | `107` | idem |
| `2203.00` | `220300` contra `22030000` | `22030000` Cervejas de malte |
| `220300` | `220300` | idem |
| `cerveja` | palavra-chave | `22030000` |
| `papel higienico` | sem acento | `48181000` |
| `banco de dados` | palavra curada | `01.07.01.000` |
| `5102` | código exato | CFOP `5102`, pontuação 100 |

Todos verificados ao vivo, contra o ERP no ar.

---

## 3. AS DUAS MARCAS DE PALAVRA-CHAVE

| Origem | Quantidade | O que é |
|---|---|---|
| `descricao-oficial` | 37.230 | tokens da descrição oficial. **Derivado, sem invenção** |
| `curado` | 36 | sinônimo de negócio, em `bd/busca_fiscal/palavras_curadas.csv` |

O nível 2 existe porque a descrição oficial é escrita no linguajar do fisco:
*"Cervejas de malte"* não responde a quem digita *"cerveja"* nem *"long neck"*.
Por isso a origem fica declarada linha a linha — curadoria não se confunde com
dado oficial.

**Todo código do arquivo curado foi verificado existir** no cadastro
correspondente. Nenhum foi inventado. `01.07.01.000` e `01.05.01.000` são de
`bc_fis_issqn`; os demais de `bc_fis_ncm`.

---

## 4. UMA DECISÃO QUE FOI FORÇADA PELA MEDIDA

**A normalização de texto é feita em Java, não em SQL.**

Remover acento mantendo só letras depende da collation. Nesta base, que é
`pt_BR.UTF-8`, a classe `[a-z]` resolve pela ordenação do locale e:

```sql
select regexp_replace('Cafe ACAO 1.07', '[^a-z0-9]+', ' ', 'g');
--  afe 1 07     <- o C e o ACAO engolidos
```

Verifiquei também que `'A' ~ '[a-z]'` é `falso` nesta collation. Uma função de
normalização que dá resultado diferente conforme o locale do servidor é pior do
que nenhuma: o mesmo dado muda de resultado entre dois ambientes, e uma busca
que não acha o que o usuário digitou não tem como o usuário desconfiar do
resultado errado.

Então: **o Java normaliza e grava; o Python gera o CSV; o banco só filtra por
igualdade e prefixo, que não dependem de collation.** A função
`fiscal_normaliza_codigo` ficou no banco porque usa só dígito, e aí a classe de
caractere não entra.

Os dois arquivos repõem a mesma regra, escrita com o mesmo texto:
`NormalizacaoFiscal.java` e `gerar_palavras.py`.

---

## 5. OS ARQUIVOS

| Arquivo | Papel |
|---|---|
| `V109__busca_inteligente_fiscal.sql` | tabela, função de código, índices. **Sem carga** |
| `bd/busca_fiscal/gerar_palavras.py` | gera o CSV, com a normalização do Java |
| `bd/busca_fiscal/palavras_curadas.csv` | nível 2, com a origem declarada |
| `bd/busca_fiscal/carregar_palavras.sql` | `COPY` para a tabela |
| `fiscal/busca/NormalizacaoFiscal.java` | normalização, e o porquê de ser Java |
| `fiscal/busca/BuscaFiscalService.java` | ranking e motivo da correspondência |
| `fiscal/busca/ConsultaCatalogo.java` | consulta aos três catálogos |
| `fiscal/model/PalavraChave.java`, `repository/…` | persistência |
| `fiscal/controller/BuscaFiscalController.java` | o endpoint |

A carga segue o padrão que o projeto já usa para dado oficial
(`scripts/seed/converter_oficiais.py` → CSV → `carregar_oficiais.sql`).

```bash
PGPASSWORD=... PGUSER=sa python3 bd/busca_fiscal/gerar_palavras.py \
    --saida src/main/resources/db/seed/processed/fiscal_palavras.csv
sudo -u postgres psql -d brasil-saas -f bd/busca_fiscal/carregar_palavras.sql
```

---

## 6. O QUE FALTA

**A tela.** O endpoint existe e está testado, e é o que o produto, o serviço e a
tributação vão consumir. Falta o componente de busca no cadastro de produto, que
é onde o ganho aparece para o usuário.

---

## 7. TRÊS ARMADILHAS QUE A IMPLEMENTAÇÃO ENCONTROU

Nenhuma delas é óbvia, e as três derrubaram o boot antes de aparecer.

| # | Armadilha | Sintoma | Por quê |
|---|---|---|---|
| 1 | Função criada adiante, fora do Flyway | `é necessário ser o dono da função` | o Flyway conecta como `sa`; eu tinha criado como `postgres` |
| 2 | Tabela criada adiante, fora do Flyway | `SQL State 42501` | 169 das 171 tabelas do schema pertencem a `sa`. Eu era a exceção |
| 3 | `peso SMALLINT` no banco, `Integer` no Java | `Schema-validation: wrong column type` | `ddl-auto: validate` existe para isso. `Integer` mapeia `int4`; a coluna é `int2` |

E uma quarta, de processo: **o jar carrega a migration dentro dele.** Corrigir o
`.sql` no disco e reiniciar sem reempacotar não muda nada — o Flyway continua
lendo a versão antiga de dentro do jar.

---

## 8. COMO VERIFICAR

```bash
T=$(curl -s -X POST http://127.0.0.1:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"username":"euripedes","password":"ALTERE_ME"}' | grep -oE '"accessToken":"[^"]+"' | cut -d'"' -f4)

curl -s -G "http://127.0.0.1:8080/api/fiscal/busca" --data-urlencode "q=cerveja" \
     -H "Authorization: Bearer $T" -H "X-Empresa-Id: 1"

sudo -u postgres psql -d brasil-saas -c "
  select tabela, count(*), count(*) filter (where origem='curado')
    from brasil_saas.bc_fis_palavra_chave group by 1;"
```

