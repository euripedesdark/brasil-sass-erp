---
id: 2026-09-26-proxy-nginx-e-o-bug-do-fallback
status: confirmado
data: 2026-09-26
---

# O proxy nginx, e o bug que o fallback expôs

Duas coisas nesta sessão, e a segunda é séria.

> **Atualizado em 26/09/2026:** o bug do item 2 foi **corrigido**. O Ruby passou a
> falar o mesmo contrato da API Java, e o ERP passou a ler só esse contrato. Ver
> `docs/pesquisa/contrato-nfse-unico.md`. As notas 29 e 30, descritas abaixo, foram canceladas
> na prefeitura.

## 1. O proxy Ruby foi trocado pelo nginx

O proxy em Ruby (`nfse-failover.rb`, porta 4567) lia o corpo da requisição direto
do socket, e só sabia ler quando vinha `Content-Length`:

```ruby
corpo = headers['content-length'] ? origem.read(headers['content-length'].to_i) : nil
```

O ERP envia `Transfer-Encoding: chunked`, porque o corpo é um `Map` serializado e
não um arquivo com tamanho conhecido. Sem `Content-Length`, `corpo` era `nil`, os
chunks ficavam no socket e a API recebia um POST sem corpo:

```
{"erro":"Falha inesperada: Required request body is missing: public
org.springframework.http.ResponseEntity<?> ...NfseSpController.emitirRps(...)"}
```

**Nenhuma emissão de NFS-e funcionava pelo ERP na configuração padrão**, porque
`brasil_saas.fiscal.nfse.url` tem como padrão a 4567, que é o proxy.

### Por que nginx e não conserto no Ruby

O bug do chunked não é um bug isolado: é a classe. Um proxy que lê HTTP na mão
erra tudo que não antecipou, e cada erro novo é o mesmo tipo. nginx entende o
protocolo.

Além disso, o script era a terceira causa do crash-loop de 8,6 horas (ver
`docs/pesquisa/CAUSA-do-crash-loop-NFSE.md`). Tirar o roteador levou os serviços
de **3 para 2**: a API Java e o bridge Ruby continuam separados, que é o certo,
porque são implementações de verdade, com certificado e SOAP próprios. O que
sumiu foi o roteador.

### Configuração

`/etc/nginx/conf.d/brasil_saas-nfse.conf`, escrita pelo `installbase.sh`:

```nginx
upstream brasil-saas_nfse {
    server 127.0.0.1:4568 max_fails=3 fail_timeout=30s;
    server 127.0.0.1:4569 max_fails=3 fail_timeout=30s;
    keepalive 8;
}
server {
    listen 4567;
    client_max_body_size 8m;
    proxy_connect_timeout 30s;
    proxy_send_timeout    180s;
    proxy_read_timeout    180s;
    location / {
        proxy_pass http://brasil-saas_nfse;
        proxy_http_version 1.1;
        proxy_set_header Host              $host;
        proxy_set_header Connection        "";
        proxy_next_upstream error timeout http_502 http_503 http_504;
        proxy_next_upstream_tries 2;
    }
}
```

Pontos que não são óbvios:

- **`proxy_read_timeout 180s`** porque a prefeitura pode passar de 30s em pico,
  e um timeout no meio da emissão deixa o usuário sem saber se a nota saiu
- **`http_502 http_503 http_504` no `next_upstream`** porque uma implementação
  quebrada devolve 500, e sem isso o nginx repassaria o 500 sem tentar a outra
- **`Connection ""`** para o `keepalive` do upstream funcionar
- `listen 4567` porque é a porta que o ERP espera. O default do Ubuntu na 80 é
  removido pelo script, e não serve para nada aqui

### O que foi testado, e como

| Teste | Resultado |
|---|---|
| chunked pela 4567 | recusa real da prefeitura, corpo lido |
| corpo de 195 KB | íntegro, sem truncamento |
| Java fora, chunked | cai no Ruby, e a resposta é a **do Ruby** |
| Java volta | volta a atender sozinha, 3 envios seguidos |
| emissão pelo ERP | nota 28 emitida, chave nacional gerada, XML no Mongo |

O teste que prova que o fallback é real e não configuracional: com a Java fora, a
resposta muda de **formato**, porque o Ruby devolve `erro` como objeto com
`codigo` e `descricao`, e o Java devolve como string. Se fosse cache ou
intermediação, o formato seria o mesmo.

## 2. O bug: o fallback emite a nota e o ERP acusa falha

**Corrigido em 26/09/2026.** O texto abaixo fica como registro do que aconteceu e
do porquê, porque a forma errada de consertar — aceitar dois formatos no leitor —
é tentadora e já foi tentada uma vez. Ver `docs/pesquisa/contrato-nfse-unico.md`.

### O que aconteceu

Com a API Java derrubada e só o Ruby de pé, pelo ERP:

```
POST /api/fiscal/nfse/emitir
→ HTTP 422
  "A prefeitura nao confirmou a emissao: {success=true, chave_nfse=2130033,
   numero_nfse=29, codigo_verificacao=VFGXSERH,
   chave_nota_nacional=35503081200000000000191000000000002926093175450323,
   alertas=[], xml_assinado=...}"
```

A prefeitura **aceitou** e devolveu `success=true` com o número 29 e o XML
assinado. A nota **existe na prefeitura**. O ERP respondeu erro.

### Por quê

As duas implementações não usam a mesma forma de resposta.

**Java**, `nfse-sp-api` — plano:

```json
{"sucesso":true,"numero_nfse":"26","codigo_verificacao":"CHGEIPFB", ...}
```

**Ruby**, `nfse-sp-bridge` — aninhado:

```json
{"error":{"success":true,"numero_nfse":"29","codigo_verificacao":"VFGXSERH", ...}}
```

`NfseEmissaoService` lê `resposta.get("sucesso")`. Com o Ruby, o mapa que chega
tem a chave `error`, e `sucesso` está um nível abaixo. A leitura devolve `null`,
o ERP entende como "a prefeitura não confirmou" e lança `BusinessException`.

O mesmo teste com as duas de pé mostrou que **as duas agora devolvem a forma
aninhada** — a diferença do contrato não é fixa por implementação, é o que o
proxy devolve naquele momento. Isso torna o diagnóstico mais frágil ainda: o
formato da resposta não é estável nem previsível por porta.

### Por que isso é o pior tipo de bug

O ERP diz que a nota não foi emitida. A pessoa acredita e **emite de novo**,
criando duplicidade na prefeitura. Duas notas iguais para o mesmo serviço, e o
sistema só descobre quando o cliente aponta.

A nota 29 existe. O ERP ficou com `FALHA_EMISSAO`. O XML assinado dela está na
resposta que o ERP descartou, e é a prova da nota por 5 anos.

O código já tem o oposto disso escrito, em `NfseEmissaoService`:

> Nenhuma nota foi emitida — pode tentar de novo com os mesmos dados.

Essa frase está errada quando a nota foi emitida. Ela transformou uma falha de
leitura de resposta em orientação para duplicar.

### O que conserta

~~Ler o sucesso nos dois formatos, tolerando `sucesso` plano e
`error.sucesso` aninhado, e **nunca** tratar resposta não reconhecida como "não
emitiu".~~

**Feito, e de outra forma.** O contrato é do emissor, não do leitor: as duas APIs
passaram a falar o mesmo formato, e o ERP lê só esse formato. A regra do "nunca
tratar resposta não reconhecida como não emitiu" também está lá, como terceiro
estado do `confirmado`.

Antes de fixar o contrato num lugar só, verificar as duas implementações.

## 3. Firewall

`netfilter-persistent` instalado e habilitado. 21 regras salvas em
`/etc/iptables/rules.v4`.

O que se descobriu no caminho: a política do `INPUT` nesta máquina já é
`ACCEPT`, então as portas do BRASIL-SAAS **já respondiam**. Abrir porta era
no-op; o que não existia era a **persistência** — sem salvar, as regras somem no
reboot e a máquina volta sem Postgres, sem Mongo e sem o proxy.

O `installbase.sh` agora detecta a política e só libera porta se ela não for
`ACCEPT`:

```bash
POLITICA=$(iptables -S INPUT | head -1 | awk '{print $3}')
if [ "$POLITICA" = "ACCEPT" ]; then
    echo "    INPUT em ACCEPT: as portas ja passam, nao ha o que liberar."
else
    ... libera 22 80 443 8080 5173 9292 4567 ...
fi
```

`$3` e não `$2` porque a saída é `-P INPUT ACCEPT`: campo 1 é `-P`, campo 2 é
`INPUT`, campo 3 é `ACCEPT`. Com `$2` a comparação lê `INPUT` e o script acha que
a política não é `ACCEPT`, e abre portas à toa — o que aconteceu na primeira
execução, criando 12 regras redundantes, incluindo as do `ufw` default em 80/443.
Foram removidas e o estado original foi salvo.

## 4. Estado dos serviços

| Serviço | Papel | Estado |
|---|---|---|
| `nginx` | proxy, 4567 | active, enabled |
| `nfse-sp-api` | API Java, 4568 | active, enabled |
| `nfse-sp-bridge` | bridge Ruby, 4569 | active, enabled |
| `nfse-failover` | **desativado** | inactive, disabled |
| `netfilter-persistent` | regras de firewall | active, enabled |

O `nfse-failover.rb` continua no repositório, inativo. Apagar ou guardar é
decisão de quem opera.

## 5. Notas emitidas nos testes

Todas com `codigoServico` inválido ou valores de teste, e canceladas:

| Nota | Data | Via | Situação |
|---|---|---|---|
| 25 | 26/09 | curl direto na 4568 | cancelada |
| 26 | 26/09 | ERP, 4567 | cancelada |
| 27 | 26/09 | ERP, 4567, **nginx** | cancelada |
| 28 | 26/09 | ERP, 4567, **nginx** | cancelada |
| 29 | 26/09 | ERP, 4567, **fallback Ruby** | **cancelada** — existia na prefeitura, ERP marcou FALHA_EMISSAO |
| 30 | 26/09 | ERP, 4567, **fallback Ruby** | **cancelada** — idem |
| 31 | 26/09 | ERP, 4567, **fallback com contrato único** | emitida e cancelada, com número e XML arquivados |

A 31 é a prova de que o conserto funciona: mesmo caminho da 29 e da 30, agora o
ERP grava os identificadores.

Nenhuma nota real de cliente foi emitida. Nenhum serviço com CNPJ real foi
alterado.
