---
id: 2026-09-26-duas-implementacoes-nfse
status: confirmado
data: 2026-09-26
---

# As implementações de NFS-e SP, e qual delas é o fallback

Há **quatro** coisas no material que falam com a prefeitura de São Paulo, e
nem todas emitem. A escolha de qual é a primária tem um efeito que não é óbvio:
**a que está como primária é a mais fraca em relatório de erro.**

## As duas que estão de pé

| | Java | Ruby |
|---|---|---|
| porta | 4568 | 4569 |
| pasta | `nfse-sp-api/` | `nfse-sp-bridge/` + gem `nfse_prefeitura_sp/` |
| XMLDSig | própria | da gem |
| SOAP | `javax.xml.soap` | `savon` |
| mTLS | `SslContext` | `OpenSSL::SSL` |
| papel | **primária** | **fallback** |

O roteador na 4567 é o **nginx**, não mais o script Ruby. Ver
`docs/pesquisa/proxy-nginx-e-bug-do-fallback.md`.

> **As duas falam o mesmo contrato desde 26/09/2026.** Até lá divergiam — a Ruby
> embrulhava em `error` com `success` em inglês — e o fallback emitia a nota
> enquanto o ERP respondia erro, marcando `FALHA_EMISSAO` sem guardar os
> identificadores. Foram as notas 29 e 30, já canceladas na prefeitura.
> Corrigido pelo contrato único, em `contrato_nfse`. Ver
> `docs/pesquisa/contrato-nfse-unico.md`.

## A terceira: `l10n_br_nfse_paulistana`

Pasta: `l10n-brazil/l10n_br_nfse_paulistana/`. Python, versão 18.0.1.1.0, do
OCA. Depende do pacote Python `nfselib.paulistana` e do `l10n_br_nfse` (o
nacional).

**É São Paulo capital, não Campinas.** "Nota Paulistana" é o nome do sistema
municipal de São Paulo. Conferido por conteúdo, não por nome:

- `constants/paulistana.py` declara `EnvioLoteRPS`, `TesteEnvioLoteRPS`,
  `ConsultaLote`, `ConsultaNFe` — batem com o catálogo do manual oficial
- `models/document.py:317` monta a assinatura de RPS campo a campo, somando as
  posições do manual

O que ela tem que as outras duas não têm:

| | Java 4568 | Ruby 4569 | Paulistana |
|---|---|---|---|
| `EnvioRPS` avulso | sim | sim | via `TesteEnvioLoteRPS` |
| **`EnvioLoteRPS`** (lote) | não | parcial | **sim** |
| **`ConsultaNFe`** | não | não | **sim** |
| **`ConsultaLote`** | não | não | **sim** |
| erro com código separado | não | **sim** | a ver |
| valida XML contra XSD antes | não | não | a ver |

Envio em lote e consulta de nota emitida são as duas lacunas do ERP, e é aqui que
estão. Não roda hoje: é código Odoo, e o ERP não é Odoo. Serve de referência de
implementação, não de dependência.

## A quarta: o proxy

Não é mais o `nfse-failover/` em Ruby. Em 26/09/2026 foi substituído pelo
**nginx** na mesma porta 4567, porque o script lia o corpo HTTP do socket e só
sabia ler com `Content-Length` — o ERP manda chunked, e o corpo era descartado.

| | antes | agora |
|---|---|---|
| roteador | `nfse-failover.rb`, Ruby, à mão | nginx 1.30.4 |
| serviço | `nfse-failover.service` | `nginx.service` |
| corpo chunked | **descartado** | repassado |
| corpo grande | — | íntegro (testado com 195 KB) |
| número de serviços | 3 | **2** |

O arquivo `nfse-failover.rb` continua no repositório, inativo. Detalhes e o bug
que o fallback expôs em
`docs/pesquisa/proxy-nginx-e-bug-do-fallback.md`.

## Teste lado a lado, mesmo payload

Payload com `codigoServico: "ZZZ999"` (inválido), enviado direto a cada porta.

**4568, Java:**

```json
{"erro":"A prefeitura recusou o RPS: [1001] XML não compatível com Schema.\
The 'CodigoServico' element is invalid - The value 'ZZZ999' is invalid according \
to its datatype 'http://www.prefeitura.sp.gov.br/nfe/tipos:tpCodigoServico' - \
The Pattern constraint failed.","sucesso":false}
```

HTTP 422.

**4569, Ruby:**

```json
{"error":{"success":false,"erro":{"codigo":"1001","descricao":"XML não compatível \
com Schema.The 'CodigoServico' element is invalid - ..."}}}
```

HTTP 422.

## A diferença que importa

O Ruby devolve **o código do erro separado** (`codigo: "1001"`). O Java devolve
o código **embaralhado dentro de uma string** em português, junto com o prefixo
`A prefeitura recusou o RPS: `.

Causa: `NfseSpService` e `NfseSpClient` na API Java **não extraem o código**. Não
há `1001` nem parse de erro em lugar nenhum do código Java — as ocorrências de
`codigo` que existem são `CodigoServico`, `CodigoVerificacao` e `CodigoCEI`, que
são tags do XML e não são erro. O Ruby faz o parse e devolve estruturado.

O que isso custa: para filtrar, contar ou decisive "a prefeitura recusou por
campo", é preciso ler o código dentro do texto. Com o Ruby, é uma coluna. Com o
Java, é `Pattern.compile("\\[(\\d{3,5})]")` applied na string.

## Consequência para o fallback

Manter as duas é o certo. Mas a escolha de qual fica na frente deveria ser
consciente, porque hoje está o inverso do desejável: a que reporta erro melhor
está como **fallback**, e a que reports o erro em texto corrida está como
**primária**.

Duas leituras possíveis, e a decisão é de quem opera:

- **A primária fica como está** e a API Java ganha a extração de código, para
  ficar equivalente. Vantagem: uma correção só, e as duas passam a ter o mesmo
  contrato.
- **O Ruby vira primária** e o Java vira fallback. Vantagem: nenhuma mudança de
  código, e o relatório de erro já sai bom. Desvantagem: perde-se a validação de
  schema, que a gem não faz (o Ruby **não** valida o XML contra o XSD antes de
  mandar — a prefeitura valida).

A segunda tem um custo que não é obvio: sem validação local, todo XML malformado
vira ida à prefeitura e volta recusado.

## O que ambas não fazem

- Não validam o XML contra o XSD oficial antes de enviar. `esquema nota servico
  sp/` tem os 19 XSD e nenhuma das duas os usa.
- Não consultam a nota depois de emitida. O `ConsultaNFe` existe no manual
  (ver `LEIA-ME.md` na pasta da biblioteca) e não está implementado em nenhuma
  das duas.
- Não têm tabela de código municipal com vigência. `codigos-servico-sp.json` tem
  304 códigos e 8 deles foram encerrados em 31/12/2025.

## Para verificar

```bash
# as duas respondem
for p in 4568 4569; do curl -s -m 5 -o /dev/null -w "$p %{http_code}\n" \
  http://127.0.0.1:$p/api/nfse-sp/status; done

# as duas falam com a prefeitura (payload inválido de propósito)
for p in 4568 4569; do
  echo "--- $p"
  curl -s -m 60 -X POST http://127.0.0.1:$p/api/nfse-sp/emitir-rps \
    -H 'Content-Type: application/json' \
    -d '{"imPrestador":"2130033","serieRps":"BC","numeroRps":"99099",
         "dataEmissao":"2026-09-26","valorServicos":"100.00",
         "aliquotaServicos":"0.0200","codigoServico":"ZZZ999",
         "cnpjTomador":"00000000000192","discriminacao":"teste"}'
  echo
done
```

O payload inválido é de propósito: prova que as duas chegam na prefeitura sem
criar nota nenhuma.
