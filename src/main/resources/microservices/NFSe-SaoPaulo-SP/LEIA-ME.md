---
id: 2026-09-26-nfse-sp-biblioteca
status: confirmado
data: 2026-09-26
---

# Biblioteca NFS-e São Paulo — o que foi verificado e o que está errado

Fonte: `src/main/resources/microservices/NFSe-SaoPaulo-SP/` (24 arquivos, 5,2 MB).

Este registro vai para a tabela `bc_fis_biblioteca_modulo` junto com o restante
deste material. Aqui fica o que a leitura dos arquivos mostrou.

## O que tem na pasta

| Arquivo | Tamanho | O que é | Lido |
|---|---|---|---|
| `NFe_Web_Service-v3.3.8.pdf` | 4,9 MB | manual oficial do web service, 86 páginas, **8 serviços SOAP** | parcial |
| `Cod.-de-servico-SP-x-Campinas.pdf` | 240 KB | tabela de serviços do município de São Paulo, 304 códigos | sim, integral |
| `esquema nota servico sp/*.xsd` | 110 KB, 19 arquivos | XSDs oficiais v01 | sim, os 4 relevantes |
| `alteracao_codigos.xlsx` | 12 KB | 191 registros de vigência de código | sim |
| `atribuicao-de-codigos.xlsx` | 12 KB | 30 códigos encerrados em 31/12/2025 e substitutos | sim |
| `schemas-v01-2.zip` | 22 KB | provável duplicata dos XSDs | não |

## Correção: o PDF não é de Campinas

O nome do arquivo diz `Cod.-de-servico-SP-x-Campinas.pdf`. **O conteúdo é do
município de São Paulo.** O cabeçalho do documento é:

> CODIGOS DE SERVIÇOS DO MUNICIPIO DE SÃO PAULO "VERSUS" UTILIZADOS PELA
> UNIVERSIDADE

Conferido: os 304 códigos do PDF batem com os 304 do
`nfse-sp-api/codigos-servico-sp.json`, **zero divergência**. O `2919 → 01.07`
(Suporte Técnico em Informática) está no PDF, na linha 112, e é o que a IM 2130033
usa.

O nome do arquivo está errado e levou ao erro de achar que a tabela era de
Campinas. Vale corrigir o nome.

## Achado: 8 dos 304 códigos foram encerrados

`atribuicao-de-codigos.xlsx` lista **30 códigos encerrados em 31/12/2025**, cada
um com os substitutos. Oito deles estão na tabela que a API usa:

| Código encerrado | LC 116 | Substitutos |
|---|---|---|
| `1520` | 07.01 | 1521, 1522, 1523 |
| `1546` | 07.01 | 1547, 1548, 1549 |
| `1589` | 07.01 | 1590, 1591 |
| `1627` | 07.01 | 1628, 1629 |
| `5870` | 15.03 | 5909, 5910, 5911, 5912, 5913, 5914 |
| `2340` | 16.01 | 2341, 2342, 2343, 2344 |
| `2143` | 30.01 | 2144, 2145, 2146 |
| `8274` | 12.07 | 8275, 8276, 8277 |

O `codigos-servico-sp.json` foi gerado em 25/09/2026 e **não incorporou essa
mudança**.

Por que isso é bug e não detalhe: o código tem 4 dígitos, então passa no
`pattern` do XSD e a API aceita. A recusa só vem da prefeitura, e chega como
`[1001] The 'CodigoServico' element is invalid - Pattern constraint failed` — que
não diz que o código foi encerrado, diz que o formato é errado. A pessoa procura
formato e não acha.

O serviço `SUP-001` do cadastro usa `2919`, que **não** está na lista de
encerrados. Então o serviço cadastrado está certo; o risco é qualquer serviço
novo que alguém cadastre com um dos oito.

## Os 8 serviços SOAP do manual

O manual lista oito. A gem `nfse_prefeitura_sp` implementa quatro:

| Serviço SOAP | Na gem | Observação |
|---|---|---|
| `EnvioRPS` | sim | |
| `EnvioLoteRPS` | parcial | só `teste_envio_lote_rps` |
| `CancelamentoNFe` | sim | |
| `ConsultaCNPJ` | sim | |
| **`ConsultaNFe`** | **não** | aceita `ChaveRPS` ou `ChaveNFe` — dá para consultar por RPS |
| `ConsultaLote` | não | |
| `InformacoesLote` | não | |
| `ConsultaGuia` | não | não lido |

### ConsultaNFe é a que interessa

`PedidoConsultaNFe_v01.xsd` pede uma de duas chaves:

- `ChaveRPS` = `InscricaoPrestador` + `SerieRPS` + `NumeroRPS`
- `ChaveNFe` = a chave nacional de 44 posições

O `bc_fis_nfse` já guarda `serie_rps`, `numero_rps` e `chave_nota_nacional`
(migration V93), e a IM está na configuração. **Dá para consultar a nota na
prefeitura depois de emitida**, com o que já é gravado.

`RetornoConsulta` traz `Sucesso`, `Alerta[]`, `Erro[]` e `NFe[]`, e `tpNFe` traz
`ChaveNFe`, `StatusNFe`, `ChaveRPS`, `DataEmissaoNFe`, `NumeroLote` e outros.

Isto muda o que vale a pena fazer:

- **Gravar o retorno da chamada continua valendo**, porque a consulta não diz por
  que a emissão foi recusada. A recusa existe só no momento do envio.
- **A consulta é a rede de segurança** para nota emitida e gravação local falha
  depois — hoje o XML se perde nesse caso.

## A chave nacional de São Paulo capital

Observado em produção: `35503081200000000000191000000000002626097518490221`

Os 5 primeiros são `355030`, o IBGE de São Paulo capital — não `cUF` + `AAMM`
como na NF-e. A tabela `res.city.csv` do OCA traz `3550308` (com o dígito, 7
posições), o que é a forma do OCA e não da chave.

O ERP tem São Paulo capital na tabela de município (`SÃO PAULO`, ibge `3550308`).
A diferença entre `355030` e `3550308` importa se algum dia alguém validar a
chave por prefixo, e ainda não foi lido o manual para dizer qual é a regra.

## O que a API faz hoje, e o que não faz

- Monta o XML e envia **sem validar contra o XSD oficial**. A validação é da
  prefeitura, e o erro volta em inglês e em texto longo.
- Não tem tabela de código válido com vigência.
- Não consulta a nota depois de emitida.

## Em aberto

- `ConsultaGuia` — o que consulta
- Se `tpNFe` traz o XML assinado em elemento próprio ou só a assinatura
- `EnvioLoteRPS` vs `EnvioRPS`: quando usar cada um
- Catálogo de códigos de erro da prefeitura. O `[1001]` é o único que
  apareceu, e veio de validação de schema do XMLDSig, não de um catálogo
- Os 82% do manual que não foram lidos
