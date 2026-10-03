# O failover descarta corpo em `Transfer-Encoding: chunked`

O proxy da porta 4567 (o failover entre a API Java na 4568 e o bridge Ruby na
4569) só lia o corpo da requisição quando vinha `Content-Length`. O ERP envia
`Transfer-Encoding: chunked`, então **o corpo era descartado** e a API recebia um
POST sem corpo.

Efeito: **nenhuma emissão de NFS-e funcionava na configuração padrão do ERP.** O
default de `brasil_saas.fiscal.nfse.url` é `http://127.0.0.1:4567/api/nfse-sp`,
o failover.

## Como o defeito se escondia

O sintoma era `Required request body is missing`, devolvido pela API. Parece
problema do ERP — e o ERP passou meses assim.

O que desviava o olhar:

- **curl funcionava.** `curl --data-binary @arquivo` manda `Content-Length`, e o
  proxy lia o corpo. Só quem manda chunked quebrava.
- **A API estava certa.** Ela respondia exatamente o que recebe: um POST sem
  corpo é um POST sem corpo.
- **O proxy respondia 200 nos testes de health check.** `/status` é GET, e GET
  não tem corpo. O health check nunca exercita o caminho quebrado.
- **A mensagem não apontava para o meio da linha.** A API Culpa o cliente, que é
  quem de fato não mandou corpo — mas quem não mandou foi o proxy, não o ERP.

Para reproduzir foi preciso mandar o mesmo pedido nos dois formatos:

```bash
# Content-Length: funciona
curl -s -X POST http://127.0.0.1:4567/api/nfse-sp/emitir-rps \
  -H 'Content-Type: application/json' --data-binary @rps.json

# chunked: corpo some
curl -s -X POST http://127.0.0.1:4567/api/nfse-sp/emitir-rps \
  -H 'Content-Type: application/json' -H 'Transfer-Encoding: chunked' \
  --data-binary @rps.json
```

O primeiro devolve a recusa da prefeitura. O segundo devolve
`"erro":"Falha inesperada: Required request body is missing: public
org.springframework.http.ResponseEntity<?> ...NfseSpController.emitirRps(...)"`

O mesmo pedido **direto** na 4568, em chunked, funciona. Isso isola o proxy como
causa.

## A linha

```ruby
# antes
corpo = headers['content-length'] ? origem.read(headers['content-length'].to_i) : nil
```

Sem `Content-Length`, `corpo` é `nil`. Os chunks ficam no socket, nobody lê, e o
backend recebe um POST vazio. O código segue sem erro nenhum: uma requisição sem
corpo é uma requisição válida.

## O conserto

`ler_corpo` decide pelo enquadramento em vez de assumir `Content-Length`:

```ruby
def ler_corpo(origem, headers)
  if headers['transfer-encoding'].to_s.downcase.include?('chunked')
    ler_chunked(origem)
  elsif headers['content-length']
    tamanho = headers['content-length'].to_i
    return nil if tamanho.zero?
    return '' if tamanho > CORPO_MAXIMO

    origem.read(tamanho)
  end
end
```

`Transfer-Encoding: chunked` tem prioridade quando presente, porque é o que
define o enquadramento na linha da malha — mesmo que `Content-Length` venha
junto. `ler_chunked` lê cada pedaco pelo tamanho em hex, e o total é limitado a
8 MB: o proxy lê o corpo inteiro na memória e não deve aceitar tamanho
irrestrito de ninguém.

O limite não é afano. NFS-e tem poucos KB; 8 MB é folgado para nota fiscal e
curto o bastante para não ser vetor de consumo de memória.

### Um detalhe que custou uma iteração

O parse do tamanho do pedaço foi escrito primeiro como
`linha.strip.split(';').first.to_s(16).to_i`. `String#to_s` não aceita argumento,
então isso estourava `ArgumentError` — engolido pelo `rescue StandardError` do
proxy, que respondia 502. O certo é `String#to_i(16)`.

O `rescue` do proxy mascarou o erro da mesma forma que mascarou o defeito
original: transformou um erro de programação em "falha ao falar com a
implementação de NFS-e", que não ajuda ninguém a achar a causa.

## O teste

`src/main/resources/microservices/nfse-failover/testar_failover.rb` sobe um
backend falso que devolve o corpo que recebeu, e o proxy na frente dele. Manda o
mesmo corpo nos dois formatos e compara.

O ponto do teste é ser **capaz de falhar**. Contra o código anterior:

```
  ok     Content-Length: corpo chegou inteiro (121 bytes)
  FALHA  chunked: esperava 121 bytes, chegou 0
  ok     GET sem corpo: {"recebido" => "", "tamanho" => 0}
```

Contra o código corrigido:

```
  ok     Content-Length: corpo chegou inteiro (121 bytes)
  ok     chunked: corpo chegou inteiro (121 bytes)
  ok     GET sem corpo: {"recebido" => "", "tamanho" => 0}
```

Um teste que só manda `Content-Length` passaria com o proxy quebrado. Por isso
os dois formatos são obrigatórios, e o chunked vai em socket cru — `Net::HTTP`
só envia chunked se o corpo for um IO sem tamanho, e aí não dá para escolher o
conteúdo com precisão.

Rodar:

```bash
cd src/main/resources/microservices/nfse-failover
ruby testar_failover.rb    # exit 0 passou, 1 falhou
```

## Por que não houve alerta

Nenhum dos três serviços tinha monitor de corpo de requisição. O health check
passava, o serviço respondia 200, e a falha só aparecia na emissão — que é
documento fiscal, ou seja, a operação que a pessoa mais precisa que funcione.

Vale registrar a lição: **health check que não exercita o caminho principal
deixa o proxy passar semanas quebrado**. Um health check de NFS-e que só faz
GET não diz nada sobre emissão.
