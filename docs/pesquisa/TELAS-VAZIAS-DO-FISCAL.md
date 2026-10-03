# Telas do fiscal que existiam e nao mostravam nada

Data: 26/09/2026

## O resumo

Quatro telas do menu Fiscal. Duas mostravam **zero linhas** e mostravam vazio
com cara de sistema funcionando. As outras duas tinham **campo de busca morto**:
digitava-se e nada acontecia.

Nenhum dos quatro problemas dava erro, log ou aviso. Eram telas que abriam,
carregavam, e respondiam "nenhum encontrado" para tudo. Quem usasse o ERP
acharia que nao tinha CFOP cadastrado, nao que o ERP nao tinha.

---

## 1. CFOP: 619 linhas, tela vazia

`bc_fis_cfop` existia, `CfopController` existia, `Cfop.jsx` existia. A tabela
tinha **0 linhas**.

Carregadas do OCA: `l10n-brazil/l10n_br_fiscal/data/l10n_br_fiscal.cfop.csv`.
619 registros, das quais 288 de entrada e 331 de saida.

Coluna `tipo` gravada como `ENTRADA`/`SAIDA`, a partir do `type_in_out` da CSV
(`in`/`out`). A coluna `tipo` existe em `varchar` e a tela ja usava esses dois
valores, entao a conversao bate com o que o filtro da tela espera.

## 2. CEST: 1.043 linhas, tela vazia

`bc_fis_cest` existia, `CestController` existia, `Cest.jsx` existia. A tabela
tinha **0 linhas**.

Carregadas de `l10n_br_fiscal.cest.csv`. 1.043 registros, 3 deles sem NCM na
origem (gravados com `ncm` vazio).

### Duas colunas que nao comportavam o formato do OCA

O ERP tem `codigo varchar(7)` e `ncm varchar(8)`. O OCA escreve pontuado:

```
codigo  01.001.00   9 caracteres   ->  nao cabe em varchar(7)
ncm     01.001.00   9 caracteres   ->  nao cabe em varchar(8)
```

A carga direta morre em `value too long for type character varying(7)`.
Normalizado, `01.001.00` vira `0100100` (7) e `01001000` (8), que cabem.

O NCM normalizado e **o mesmo formato que o ERP ja usava** em `bc_fis_ncm` —
as 10.515 linhas la estao sem ponto (`01012100`). Antes da normalizacao, o CEST
e o NCM eram dois formatos diferentes e a busca por NCM da tela nunca casaria
com a tabela de NCM.

## 3. Campo de busca morto em CFOP e CEST

O mesmo bug nas duas telas.

**CFOP**: o `<InputText placeholder="Buscar CFOP...">` estava no template sem
`value` e sem `onChange`. Nao havia estado. Digitava-se no campo e a tabela
mostrava as 619 linhas, sempre.

**CEST**: pior. Havia um `searchCodigo` que recebia o que era digitado e
**nunca era usado para filtrar**. Codigo morto, nao campo quebrado: o estado
existia, o input estava ligado, e o filtro nao existia.

Corrigido nos dois, cada um do jeito que o caso pede — ver abaixo.

---

## Por que a solucao da busca e diferente em cada tela

Esta e a parte que vale registrar, porque a escolha parece inconsistida e nao e.

**CFOP — filtro no cliente.** Sao 619 linhas, o endpoint nao pagina e devolve
tudo. O server inteiro cabe em memoria e o filtro responde sem latencia. Um
`useMemo` sobre a lista resolve, e nao ha por que pedir ao banco.

**CEST — filtro e paginacao no servidor.** Sao 1.043 linhas e o endpoint
pagina em 50 por omissao. Aqui o filtro no cliente e **errado**, e nao apenas
ineficiente:

- a tela so tinha os 50 CEST da primeira pagina, e acreditava ter 50 —
  `totalRecords` vinha do `length` da lista, e o rodape dizia "1-50 de 50" para
  1.043 registros;
- quem buscasse um CEST que estivesse na pagina 7 recebia "nenhum encontrado",
  com a linha existindo no banco.

Entao o filtro foi para o servidor, junto com a paginacao preguiçosa, seguindo
o padrao que a propria tela de NCM ja usava (`Ncm.jsx`, `lazy` +
`lazyParams`).

`CestService.listar()` ganhou assinatura `(page, rows, busca)`. O parametro
`descricao` do endpoint continua aceito, para nao quebrar quem chama; se os dois
chegarem, `busca` manda.

---

## A busca por NCM: 407 CEST com NCM incompleto

Ao testar a busca por NCM, `84713000` nao encontrava nada. Investigando:

```
ncm com 2 digitos:   10 CEST
ncm com 3 digitos:   17 CEST
ncm com 4 digitos:  180 CEST
ncm com 5 digitos:   54 CEST
ncm com 6 digitos:   94 CEST
ncm com 7 digitos:   49 CEST
ncm com 8 digitos:  636 CEST
```

`bc_fis_ncm` tem 10.515 linhas e **todas com 8 digitos**. O campo `ncms` do OCA
lista o NCM em varias precisoes: `847130`, `84714`, e ate `33`.

### Preencher com zero a esquerda nao resolve — e foi verificado

A solucao obvia seria `lpad(ncm, 8, '0')`. Foi conferida contra a tabela de
NCM antes de aplicar, e nao serve:

```
2 digitos:  10 CEST | existem como NCM real:   0
3 digitos:  17 CEST | existem como NCM real:   0
4 digitos: 180 CEST | existem como NCM real:   0
5 digitos:  54 CEST | existem como NCM real:   0
6 digitos:  94 CEST | existem como NCM real:   0
7 digitos:  49 CEST | existem como NCM real:  16
```

`33` viraria `00000033`, que nao e NCM de nada. So 16 dos 404 casariam, e mesmo
esses por coincidencia. **Inventar o digito que falta seria pior que deixar
incompleto**: a linha passaria a apontar para um codigo que nao existe, e o
erro apareceria longe da causa.

### O que a busca faz entao

Aceita o NCM gravado como **prefixo** do termo digitado. Se o OCA registrou o
CEST 2102800 para o NCM `847130`, ele vale para a familia `847130`, e
`84713000` — o codigo real, com 8 digitos, o que a pessoa de fato digita — esta
nessa familia. Sem essa metade da busca, os 407 CEST ficam inalcancaveis pelo
NCM.

Resultado verificado:

```
busca=84713000    -> 1  -> 2102800 [ncm 847130]
busca=847130      -> 1  -> 2102800 [ncm 847130]
busca=84714       -> 1  -> 2102900 [ncm 84714]
busca=38151210    -> 1  -> 0100100 [ncm 38151210]
busca=3815.12.10  -> 1  -> 0100100 [ncm 38151210]   (mesmo CEST, com e sem ponto)
busca=84716090    -> 1  -> 2103200 [ncm 84716090]
```

### Um bug que a propria busca produziu

Na primeira versao, 3 CEST sem NCM apareciam em **toda** busca. Com `ncm` vazio,
o padrao montado pelo `CONCAT` virava `'%'`, que casa com qualquer termo. Pior
tipo de bug de busca: a pessoa ve resultado que nao tem a ver com o que digitou
e acredita. Corrigido exigindo `ncm` nao vazio na clausula de familia.

---

## O que a carga nao guarda (e esta escrito, nao escondido)

1. **`ncms` e lista separada por virgula.** Fica so o primeiro NCM. Os
   secundarios se perdem, e a perda e real: um CEST pode valer para varios NCM,
   e quem cadastrar o produto pelo segundo NCM nao acha o CEST. Para fechar de
   vez: tabela de ponte `cest_ncm (cest_id, ncm)`, e a busca passa a consultar
   ela em vez de fazer `LIKE` sobre um campo de texto.
2. **10 colunas de uso do CFOP** (estoque, financeiro, ASSENT, industrializacao)
   nao sao gravadas. Sao configuracao da operacao, nao do codigo: quem emite
   decide. O ERP nao tem essa decisao hoje.
3. **`item` e `segment` do CEST** nao sao gravados.

---

## Migrations

| Arquivo | O que faz |
|---|---|
| `V100__cfop_e_cest_do_oca.sql` | 619 CFOP + 1.043 CEST |
| `V101__servicos_teste_com_codigos_municipais_validos.sql` | 20 servicos de teste, 18 com codigo municipal vigente |

Ambas com `ON CONFLICT`, entao rodar duas vezes nao duplica nem quebra. A V100
foi aplicada duas vezes seguidas de proposito, para probar.

`V100` foi gerada por `/tmp/opencode/gerar_v100.py`, lendo as CSV do OCA. As
1.662 linhas nao foram escritas a mao: a fonte e a CSV, e um arquivo montado a
mao envelhece sem ninguem perceber.

---

## Bug encontrado no caminho, que nao era do fiscal

Ha **duas migrations V99** e o Flyway recusava a subida do ERP. Causa: a
`V99__nfse_retorno_colunas_base.sql` esta **aplicada no banco** (`v99 nfse
retorno colunas base, success=true`, e a tabela `bc_fis_nfse_retorno` existe com
4 linhas), mas o **fonte tinha sumido** — o rollback para `783d30d3` levou o
arquivo. Só restava a copia compilada em `target/classes/`.

Se eu tivesse apenas apagado a copia orfa de `target/classes`, o proximo
`mvn clean` qualquer apagaria de vez o unico registro de como a tabela de
retorno da prefeitura foi criada. Por isso o fonte foi **recuperado da copia
compilada, byte a byte**, e o checksum conferido antes de subir:

```
V99__nfse_retorno_colunas_base.sql  1919639615  <- BATE
(registrado no flyway_schema_history) 1919639615
```

O checksum do Flyway nao e o CRC32 dos bytes do arquivo. O `ChecksumCalculator`
le linha a linha (`BufferedReader.readLine`, que trata `\n`, `\r` e `\r\n`) e
alimenta um CRC32 sem separador entre linhas. O script que reproduz isso ficou
em `/tmp/opencode/checksum_flyway.py`.

A migration dos servicos de teste, que nasceu como V99, foi renumerada para
**V101**, e ganhou `ON CONFLICT DO NOTHING` sem alvo — os 20 servicos ja tinham
sido aplicados a mao, e um `INSERT` seco derrubava o boot com violacao de chave.

`ON CONFLICT` sem alvo e necessario aqui porque a tabela tem **duas**
unicidades: `uk_servico_codigo (empresa_id, codigo)` e
`ux_cad_servico_codigo_municipal (empresa_id, codigo_tributacao_municipal)`.
Nomear so um deixaria a outra desarmada.

---

## O que NAO foi verificado

**As telas nao foram vistas no navegador.** Nao havia navegador conectado nesta
sessao. O que foi verificado: o build passa, os endpoints devolvem o que as
telas leem, e a logica de paginacao e filtro foi conferida contra as respostas
reais da API. O que a tela **parece** e o que a pessoa **vê** nao sao a mesma
coisa, e nao vou dizer que sao.

## Arquivos

```
src/main/resources/db/migration/V100__cfop_e_cest_do_oca.sql
src/main/resources/db/migration/V101__servicos_teste_com_codigos_municipais_validos.sql
src/main/java/br/com/brasil_saas/fiscal/repository/CestRepository.java   busca por codigo/descricao/NCM
src/main/java/br/com/brasil_saas/fiscal/controller/CestController.java  parametro busca
src/main/resources/static/react/src/services/CestService.js              listar(page, rows, busca)
src/main/resources/static/react/src/components/fiscal/Cest.jsx          lazy + filtro no servidor
src/main/resources/static/react/src/components/fiscal/Cfop.jsx          busca morta -> useMemo
```
