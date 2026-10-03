# nfse-failover

Proxy que mantém a NFS-e de São Paulo no ar quando a implementação principal cai.

```
                    ┌──────────────────────────────┐
  ERP ── 4567 ────▶ │  nfse-failover (este)        │
                    └──────────────┬───────────────┘
                          pick the first healthy
                          ┌────────┴────────┐
                          ▼                 ▼
                 java  (nfse-sp-api)  ruby  (nfse-sp-bridge)
                    4568                 4569
                    primária             fallback
```

Quem chama não percebe a troca: as duas implementações falam o mesmo contrato em
`/api/nfse-sp`, e o proxy só repassa. Toda resposta traz o cabeçalho
`X-Backend` dizendo quem atendeu.

## Por que proxy, e não "matar o Ruby e subir o Java na mesma porta"

Trocar processo na porta é uma corrida: quem sobe depois pode perder o `bind`, e
durante a janela ninguém responde — que é justamente o momento em que mais faz
falta. Com o proxy não há disputa: cada implementação fica na sua porta, a pública
é uma só, e a troca é instantânea assim que o health check percebe.

## As duas implementações precisam falar o mesmo contrato

Os caminhos originais eram diferentes — `/emitir_rps` no Ruby contra
`/api/nfse-sp/emitir-rps` na Java. Com o proxy burro, quem chamasse o contrato da
Java e caísse no Ruby levaria 404. Por isso o bridge expõe as quatro rotas da Java
como aliases, traduzindo o payload no caminho.

O contrato comum é:

| rota | o que faz |
|---|---|
| `POST /api/nfse-sp/consulta-cnpj` | confere IM, regime e se emite NFS-e |
| `POST /api/nfse-sp/emitir-rps` | emite o RPS e devolve número, código de verificação, chave nacional e o XML assinado |
| `POST /api/nfse-sp/cancelar` | cancela por chave ou por IM + número |
| `GET  /api/nfse-sp/status` | health check; é o que o proxy consulta |

Duas rotas em específico precisaram de cuidado no bridge, e ambas por causa do
mesmo motivo — a gem converte o nome do elemento em snake_case e o `apelidar` só
trata o sufixo no topo:

- **`normalizar_keys`** — o JSON chega em camelCase (`"imPrestador"`) e o
  `symbolize_names: true` do Sinatra **preserva** essa grafia, virando
  `:imPrestador`. Sem converter para `im_prestador` a tradução inteira devolve
  `nil` e a gem monta um RPS sem `ChaveRPS` (erro 1001 da prefeitura).
- **`campo(hash, chave)`** — `retorno_legivel` devolve chave como **string**.
  Ler com símbolo devolvia `nil` e a resposta saía com número e código de
  verificação vazios, que é exatamente o que o ERP precisa para registrar e
  cancelar a nota. A nota era emitida, porém sem identificação de volta.

## Como rodar

```bash
ruby nfse-failover.rb
```

Variáveis de ambiente:

| variável | padrão | o que faz |
|---|---|---|
| `NFSE_FAILOVER_PORTA` | `4567` | porta pública |
| `NFSE_FAILOVER_BACKENDS` | `java=http://127.0.0.1:4568,ruby=http://127.0.0.1:4569` | lista `nome=url` **em ordem de preferência** |
| `NFSE_FAILOVER_INTERVALO` | `10` | segundos entre health checks |
| `NFSE_FAILOVER_TIMEOUT` | `3` | timeout do health check |

Com as duas de pé, a primeira da lista atende. Se a primária cair, a próxima
chegada já vai para a segunda — sem derrubar a pública em momento algum.

Se **nenhuma** estiver de pé, a resposta é `503` com o corpo JSON
`{"success": false, "error": "Nenhuma implementação de NFS-e disponível."}` e
`X-Backend: nenhum`. A ponta do ERP grava a intenção (`EMITINDO`) antes de
chamar, então uma indisponibilidade não perde RPS: ele fica registrado como
pendente e a emissão pode ser refeita com a mesma chave de RPS.

## Comprovado em produção

Ciclo feito contra a prefeitura de verdade, com certificado A1:

| passo | resultado |
|---|---|
| ambas de pé, `GET /status` na 4567 | `X-Backend: java` |
| API Java derrubada | `X-Backend: ruby` na requisição seguinte |
| `POST /api/nfse-sp/emitir-rps` pelo Ruby | NFS-e emitida, XML assinado, contrato completo |
| varrer cancelamento pelo Ruby | NFS-e 9 a 24 canceladas |

Uma nota emitida por uma implementação pode ser cancelada pela outra, e vice-versa
— o cancelamento aceita só IM + número, sem código de verificação, justamente
porque é o que permite recuperar NFS-e órfã.

## Aviso

O proxy **não tem autenticação**. Ele escuta em `0.0.0.0` e qualquer um que
alcance a porta consegue emitir e cancelar NFS-e. Isso vale para as duas
implementações também, e vale para a porta que for publicada para fora. Em
produção, ponha um `nginx` na frente com TLS e restrição por IP, ou troque a
`NFSE_FAILOVER_PORTA` para `127.0.0.1` e deixe o proxy só ao alcance local.
