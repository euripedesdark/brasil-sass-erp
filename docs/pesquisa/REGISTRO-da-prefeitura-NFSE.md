# Registro da prefeitura de NFS-e (São Paulo) — pesquisa

Documento de pesquisa. **Não é documentação do sistema**: descreve o que foi
observado contra a Prefeitura de São Paulo e o que foi escrito depois, para que
a implementação possa ser feita de novo do zero sabendo o que já se sabe.

Quando a functionality for implementada de vez, este arquivo deve ser
substituído por documentação do código — com o caminho da classe, o nome do
endpoint e a migration. O que está aqui é a **pesquisa** que antecede.

## Ambiente

| | |
|---|---|
| IM do prestador | `2130033` |
| CNPJ remetente | `00000000000191` |
| URL da prefeitura | `https://nfews.prefeitura.sp.gov.br/lotenfe.asmx` |
| Ambiente | produção |

Serviços, de fora para dentro:

```
4567  nfse-failover.rb    proxy, escolhe quem está de pé        (padrão do ERP)
4568  nfse-sp-api         API Java — implementação primária      (assina, mTLS, SOAP)
4569  nfse-sp-bridge      bridge Ruby + gem nfse_prefeitura_sp  (fallback)
```

O `brasil_saas.fiscal.nfse.url` do ERP tem como padrão
`http://127.0.0.1:4567/api/nfse-sp`, ou seja, **o failover está no caminho
padrão**.

## O que a gem expõe

Quatro serviços SOAP, em `nfse_prefeitura_sp/lib/nfse_prefeitura_sp/services/sync/`:

- `envio_rps.rb` — emite
- `cancelamento_nfe.rb` — cancela
- `consulta_cnpj.rb` — consulta o prestador
- `teste_envio_lote_rps.rb` — teste de lote

**A gem expõe só 4 dos 8 serviços SOAP do manual.** Ela tem `envio_rps`,
`cancelamento_nfe`, `consulta_cnpj` e `teste_envio_lote_rps`. O manual oficial
(`NFe_Web_Service-v3.3.8.pdf`, 86 páginas) lista oito: acrescente
**`ConsultaNFe`**, `ConsultaLote`, `InformacoesLote` e `ConsultaGuia`.

Isto está registrado aqui porque eu escrevi antes o contrário, e está errado.

### ConsultaNFe existe, e o ERP já tem o que precisa

`PedidoConsultaNFe_v01.xsd` aceita duas chaves:

```
ChaveRPS  = InscricaoPrestador + SerieRPS + NumeroRPS
ChaveNFe  = (a chave nacional de 44 posições)
```

O `bc_fis_nfse` já guarda `serie_rps`, `numero_rps` e `chave_nota_nacional`
(migration V93), e a IM é a `2130033` da configuração. **Ou seja: dá para
consultar a nota na prefeitura depois de emitida**, usando o que já é gravado.

O que a consulta devolve (`tpNFe`): `Assinatura`, `ChaveNFe`, `DataEmissaoNFe`,
`NumeroLote`, `ChaveRPS`, `TipoRPS`, `DataEmissaoRPS`, `CPFCNPJPrestador`,
`RazaoSocialPrestador`, `EnderecoPrestador`, `EmailPrestador`, `StatusNFe`.

Ou seja, **devolve o XML assinado** (`Assinatura` é a assinatura; vale checar se
o manual define um elemento de XML próprio, que não foi lido ainda).

O que isso muda na recomendação:

- **Gravar o retorno na chamada continua valendo.** A consulta diz se a nota
  está lá e qual o status, mas **não diz por que a emissão foi recusada** — a
  recusa é da chamada de envio, e o motivo existe só naquele momento. Perder a
  recusa é perder a informação de como corrigir.
- **A consulta é a rede de segurança** para o caso que a nota foi emitida e a
  gravação local falhou depois. É a situação em que o XML se perde hoje.
- Falta ler no manual o que `ConsultaGuia` faz; o nome sugere consulta por
  guia, que é outro serviço.

## Respostas reais observadas

Todas de produção, IM 2130033. Arquivo completo em `retornos-reais-nfse.txt`
(6 registros, os bytes como vieram).

### Consulta de CNPJ — HTTP 200

```json
{"sucesso":true,"inscricao_municipal":"2130033","emite_nfse":true,"alertas":[]}
```

Não traz mensagem de erro. É o único sinal de que a IM está habilitada.

### Emissão recusada por schema — HTTP 422

O caso mais comum, e o que mais confunde porque a mensagem é longa e em inglês.

```json
{"sucesso":false,
 "erro":"A prefeitura recusou o RPS: [1001] XML não compatível com Schema.\
The 'CodigoServico' element is invalid - The value 'ZZZ999' is invalid according \
to its datatype 'http://www.prefeitura.sp.gov.br/nfe/tipos:tpCodigoServico' - \
The Pattern constraint failed."}
```

Quando este é o **único** erro, a causa é quase sempre o código municipal do
serviço. Três erros aparecem de uma vez se a requisição vier malformada de
outra forma:

```
[1001] The 'TipoRPS' element is invalid - The value '1' is invalid ... Enumeration constraint failed.
[1001] The 'CodigoServico' element is invalid - The value 'ZZZ999' is invalid ... Pattern constraint failed.
[1001] The 'AliquotaServicos' element is invalid - The value 'null' is invalid ... not a valid Decimal value.
```

Desses três, dois são erro de quem montou a requisição:

- `TipoRPS` **não** é numérico. A API Java usa a string `"RPS"` como padrão
  (campo `tipoRps` do `RpsRequest`). Mandar `1` reprova.
- `AliquotaServicos` no plural. O campo do `RpsRequest` é `aliquotaServicos`.
  Mandar `aliquotaServico` chega como `null` na prefeitura.

### Emissão aceita — HTTP 200

```json
{"sucesso":true,
 "chave_nfse":"2130033",
 "numero_nfse":"26",
 "codigo_verificacao":"CHGEIPFB",
 "chave_nota_nacional":"35503081200000000000191000000000002626097518490221",
 "alertas":[],
 "xml_assinado":"<PedidoEnvioRPS xmlns=\"http://www.prefeitura.sp.gov.br/nfe\">..."}
```

Dois cuidados:

- **`chave_nfse` não é chave.** O valor é `2130033`, que é a inscrição municipal.
  O nome é enganoso. O identificador que localiza a nota em qualquer ambiente é
  `chave_nota_nacional` (44 posições).
- **O XML vem embutido na resposta.** A prefeitura não tem serviço separado
  para baixar o XML depois. Se a resposta não for gravada, o XML assinado — que
  é a prova da nota, com prazo legal de 5 anos — se perde.

### Cancelamento — HTTP 200

```json
{"sucesso":true,"alertas":[]}
```

Aceita com IM + número da nota; o código de verificação é opcional. Isso importa
porque permite recuperar uma nota cuja gravação local falhou depois da emissão.

## Código municipal do serviço

O campo que a prefeitura exige é o **código da tabela municipal de São
Paulo**, não o LC 116. Para a IM 2130033, suporte técnico em informação é
**`2919`** (LC 116 01.07).

A referência está em
`src/main/resources/microservices/nfse-sp-api/codigos-servico-sp.json`, com
`lc116` → `codigo_sp` e 314 mapeamentos. O próprio arquivo avisa que um mesmo
LC 116 pode ter mais de um código em SP, e que o mapeamento é da tabela de
**Campinas** — a validação é contra a tabela da prefeitura de São Paulo, e
`2919` foi confirmado empiricamente contra a IM 2130033.

Erro conhecido: quando o serviço está sem código municipal, a prefeitura
recusa com **erro 306**, e o número de suporte técnico é **2919**.

## O que precisa ser gravado, e por quê

Uma linha por chamada, em tabela própria, com o corpo JSON no MongoDB.

Três razões, todas verificadas:

1. **A recusa só existe no momento da chamada.** Não há como recuperar depois.
2. **Erro repetido é o ciclo normal.** Corrigir e tentar de novo é o que a
   pessoa faz; sem o registro, cada tentativa mostra "algo deu errado" sem dizer
   o quê.
3. **O XML assinado vem na resposta da emissão.** Perdido ali, perdido para
   sempre.

Uma **linha por chamada**, e não uma coluna na nota, porque uma nota conversa
com a prefeitura mais de uma vez. Guardar o cancelamento no mesmo lugar da
emissão apagaria a emissão — que é justamente o registro que serve de prova.

### `sucesso` é triestado

| valor | significa | quando |
|---|---|---|
| `true` | a prefeitura aceitou | 200 com `numero_nfse` |
| `false` | a prefeitura recusou | 422 com `[1001]` e o campo que falhou |
| `null` | não houve resposta | conexão recusada, timeout, serviço fora |

A prefeitura recusa com **HTTP 422 e corpo**: houve resposta, e a resposta foi
não. Isso é `false`. Só falta de transporte é `null`. Implementar `sucesso` como
"deu exceção" põe toda recusa em `null` e faz a consulta "o que a prefeitura
recusou" perder justamente as recusas.

### `codigo` extraído da mensagem

`[1001] XML não compatível com Schema...` — a mensagem mede centenas de
caracteres e muda conforme o campo que falhou. O código é estável e cabe numa
coluna filtrável. Padrão: `[\d{3,5}]` no começo da mensagem.

### `mensagem` é o texto da prefeitura, não o embrulho

A recusa vem por status HTTP, então o corpo está dentro da exceção e a resposta
é nula. Sem abrir esse corpo, grava-se
`422 : "{"erro":"...","sucesso":false}"` — JSON dentro de string, obriga quem lê
a decifrar.

### Falha de gravação não desfaz a operação

Se gravar o retorno falhar, a emissão continua valendo. A nota existe na
prefeitura; reportar erro levaria a repetir e criar duplicidade, que é o
problema mais caro do módulo.

Gravar em transação própria (`REQUIRES_NEW`), porque a recusa precisa ser
gravada mesmo quando a operação está em rollback — e é no caminho da exceção
que ela mais importa.

## O proxy da 4567 tem um defeito de chunked

Ver `docs/pesquisa/pesquisa-failover-chunked.md`. Resumo: o proxy só lia o corpo quando
vinha `Content-Length`, e o ERP envia `Transfer-Encoding: chunked`. Sem
`Content-Length`, o corpo era descartado e a API recebia POST sem corpo
(`Required request body is missing`).

**Este defeito não explica a nota 14 ter funcionado.** O journal do failover
começa em 26/09 08:23 e o log da API não tem nenhuma linha de 25/09; a nota 14
foi criada em 25/09 22:59, quando nenhum dos três serviços estava no caminho. Os
logs do momento em que parou não existem mais, então a causa da interrupção
**não está estabelecida**. O chunked é um defeito real e independente, provado
por teste, e precisa ser corrigido — mas não se deve contar a história de que
era o motivo da queda.

Um detalhe do diagnóstico que vale reter: `curl` funcionava e o ERP não, porque
`curl --data-binary` manda `Content-Length` e o `RestClient` do Spring manda
chunked. Qualquer teste que só use `Content-Length` passa com o proxy quebrado.
