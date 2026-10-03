# Importação de NFe por XML

Lê o XML que o fornecedor mandou e dá entrada no estoque, com os dados fiscais
do produto (NCM, CEST, CFOP) preenchendo o que estiver faltando no cadastro.

## Por que XML e não consulta à SEFAZ

A SEFAZ exige o certificado do destinatário e cobra cota por consulta. Ler o
arquivo que o fornecedor já mandou não gasta cota e funciona com o ERP
desconectado.

O ERP **não tem cliente de consulta à SEFAZ funcionando**: `NFeServiceImpl` tem
89 linhas e três `TODO`. "Consultar na SEFAZ" hoje não é opção, é escrever o
cliente do zero.

## Os dois passos

| Endpoint | O que faz |
|---|---|
| `POST /api/fiscal/entradas/importar/analisar` | Lê o XML e devolve o plano. **Não grava nada.** |
| `POST /api/fiscal/entradas/importar/confirmar` | Grava a nota, cria o que foi marcado e dá entrada no estoque. |

A separação existe porque importar mexe no estoque, cria produto e grava
documento fiscal. Num botão só, o erro sai caro e não tem volta fácil: o
estoque já recebeu quantidade a mais e a movimentação já foi gravada.

Parâmetros de `FormData`, nos dois: `arquivo` (obrigatório), `pessoaId`
(opcional — se vazio, procura o emitente pelo CNPJ), `criarProdutos`
(`true`/`false`, padrão `false`).

## Casamento de item com produto

Três degraus, e a tela mostra em qual degrau cada item casou:

1. **GTIN (`cEAN`)** — confiável. O mesmo produto recebe `cProd` diferente em
   cada fornecedor, mas o código de barras é o mesmo em qualquer lugar.
2. **`cProd` do fornecedor** — confiável dentro de um mesmo fornecedor.
3. **Nada** — o item fica `SEM_PRODUTO` e **não mexe no estoque**.

O terceiro degrau não existe de propósito. Casar por nome parece sensato e não
é: `"CHURRASQUEIRA ELETRICA PORTATIL SEM FUMACA GRILL 2000W GRANDE LINHA
PREMIUM"` é o mesmo produto com grafia diferente em notas de fornecedores
diferentes, e é também o nome de um produto diferente. Errar ali não dá erro na
tela — dá entrada errada no estoque.

O item sem produto **não impede** a entrada: a nota é gravada inteira, e só o
item fica sem movimentação. Perder a nota inteira por causa de um item é pior,
porque a conciliação com o fornecedor depende da nota existir.

## O desconto

O valor que entra no estoque é **a soma dos itens menos o desconto da nota**,
não o `vNF` nem o `vProd` do item.

Numa das notas de teste a soma dos itens dá 377,24 e o `vNF` dá 293,98, porque
a nota tem `vDesc` de 83,26. Entrar pela soma infla o estoque em 83,26; entrar
pelo `vProd` item a item faz o mesmo.

## O que o ERP normaliza, e o que ele perde

| Situação no XML | O que o ERP grava | Por quê |
|---|---|---|
| `cEAN` = `"SEM GTIN"` | `NULL` | O campo é obrigatório no XSD, então o emissor escreve um **texto** no lugar. Guardar esse texto casaria o item com todos os outros da mesma nota, porque o valor é idêntico. `normalizaEan` só aceita 8 a 14 dígitos. |
| NCM com 6 dígitos | Como veio | `847130` no XSD é um código de 6 aceito, e o valor real depende do item. Completar com zero vira `84713000`, que é **outro produto**. |
| `cEAN` só no `cEANTrib` | Usa o `cEAN` | Campomissão, o emissor escreve no substituto tributário. |
| Nota sem `infNFe` | Recusa | Um `pom.xml` ou um PDF renomeado de `.xml` passaria e devolveria um plano vazio, que o usuário confirmaria como nota. |
| PDF em vez de XML | Recusa com a dica certa | É o erro mais comum: o usuário manda o que a prefeitura mandou no e-mail. |

## A assinatura digital não é conferida

Isto é decisão, não omissão. O XML é assinado pela SEFAZ com certificado
ICP-Brasil, e validar assinatura contra a lista de revogados é trabalho de
biblioteca, não de parser. O que o serviço garante é o estrutural: se o XML não
parsear, a entrada não entra.

Para uso interno, com nota que o próprio fornecedor entregou, o risco é baixo.
**Para dar entrada de estoque a partir de XML de terceiro, precisa de validação
de assinatura antes.**

## Testes

```bash
# 1. Gabarito: segunda implementação, em Python, extrai os mesmos campos
#    direto do XML. As duas leituras têm que concordar.
python3 scripts/gabarito_nfe.py

# 2. Confere o que foi GRAVADO contra o gabarito, lendo o Postgres.
#    Teste mais forte que o plano: prova que a gravação guardou o que a
#    leitura viu.
python3 scripts/conferir_importacao_nfe.py
```

Os dois XML de teste ficam em `~/Downloads` e **não** são versionados: são
nota de fornecedor, com CNPJ e endereço de terceiro.

Resultado da última rodada: **40 campos** na comparação de leitura e **34
campos** na de gravação, com uma única divergência — o `SEM GTIN`, que vira
`NULL` de propósito.

## O que os scripts acharam

O gabarito encontrou dois casos que um teste "olhei e acreditei" não pegaria:

- **`vDesc` de 83,26** numa nota cuja soma de itens é 377,24 e cujo `vNF` é
  293,98. Sem o desconto lido, o estoque entraria inflado.
- **`"SEM GTIN"`** no campo de código de barras. Sem a normalização, os dois
  itens sem GTIN de uma mesma nota casariam entre si.
