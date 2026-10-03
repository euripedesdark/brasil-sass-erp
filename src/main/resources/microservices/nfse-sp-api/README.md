# nfse-sp-api

API de emissão e cancelamento de **NFS-e da Prefeitura de São Paulo**, com
assinatura digital A1 (ICP-Brasil).

Standalone: **não usa banco de dados**. Roda em um processo só, sem Ruby, sem
Docker e sem gem.

```
POST /api/nfse-sp/consulta-cnpj   consulta IM e se emite NFS-e
POST /api/nfse-sp/emitir-rps      emite a nota
POST /api/nfse-sp/cancelar        cancela uma ou mais notas
GET  /api/nfse-sp/status          configuração (sem revelar a senha)
```

Tudo aqui foi **validado contra o WebService de produção** da prefeitura, com
notas emitidas e canceladas de verdade.

---

## O certificado A1 é usado em três lugares

Essa é a parte que mais surprising quem integra pela primeira vez:

1. **mTLS** — o certificado é apresentado no *handshake* TLS. Sem ele o WAF
   responde `403 Forbidden` e o sintoma parece ser rede.
2. **assinatura XMLDSig** da mensagem (envelopada, na raiz).
3. **assinatura do RPS** — SHA1 de uma cadeia de 86 posições, assinado em RSA,
   no campo `Assinatura`.

O certificado da ICP-Brasil costuma ser emitido com **RC2-40-CBC**, que o
OpenSSL 3 removeu. Em Java isso não é problema: o provider SUN ainda aceita.
Se você vier do Ruby/OpenSSL, é por isso que a sua integração vai precisar de
`OPENSSL_MODULES` e do provider `legacy` — aqui não precisa de nada disso.

---

## Regras que a prefeitura não documenta de forma óbvia

Todas descobertas testando contra o serviço. Nenhuma delas aparece com uma
mensagem de erro que diga o que fazer.

### O WSDL só abre com mTLS

```
curl https://nfews.prefeitura.sp.gov.br/lotenfe.asmx?WSDL     # 403
curl --cert cert.crt --key cert.key .../lotenfe.asmx?WSDL      # 200
```

Guarde o WSDL com o certificado à mão. É dele que saem as quatro regras abaixo.

### O elemento do corpo SOAP leva o sufixo `Request`

| operação | elemento | `SOAPAction` |
|---|---|---|
| `ConsultaCNPJ` | `ConsultaCNPJRequest` | `.../nfe/ws/consultaCNPJ` |
| `EnvioRPS` | `EnvioRPSRequest` | `.../nfe/ws/envioRPS` |
| `CancelamentoNFe` | `CancelamentoNFeRequest` | `.../nfe/ws/cancelamentoNFe` |
| `EnvioLoteRPS` | `EnvioLoteRPSRequest` | `.../nfe/ws/envioLoteRPS` |
| `TesteEnvioLoteRPS` | `TesteEnvioLoteRPSRequest` | `.../nfe/ws/testeenvio` |
| `ConsultaNFe` | `ConsultaNFeRequest` | `.../nfe/ws/consultaNFe` |

A `SOAPAction` **não** segue o nome da operação em dois casos
(`consultaCNPJ`, `testeenvio`). Errar o elemento dá
`1102 "Mensagem XML de Pedido do serviço sem conteúdo"`, que parece problema de
conteúdo e é de nome.

### SOAP 1.2 no envelope **e** cabeçalho `SOAPAction`

O WSDL publica os dois bindings, mas só a combinação 1.2 + cabeçalho funciona:

| combinação | resposta |
|---|---|
| SOAP 1.1 + `SOAPAction` | `Server did not recognize the value of HTTP Header SOAPAction` |
| SOAP 1.2 + `action=` no Content-Type | `415 Unsupported Media Type` |
| SOAP 1.2 sem cabeçalho | `Unable to handle request without a valid action parameter` |
| **SOAP 1.2 + cabeçalho** | funciona |

E o `Content-Type` é `application/soap+xml;charset=UTF-8` — **sem** o
parâmetro `action`, que é o padrão SOAP 1.2 mas aqui causa o 415.

### A assinatura fica dentro do `MensagemXML`

Não envolve o envelope. O XSD declara `Signature` como filho da raiz da
mensagem (item P3 de `PedidoEnvioRPS.xsd`). A ordem é:

1. assina a cadeia de 86 posições → campo `Assinatura`
2. monta o XML do RPS **já com** esse campo
3. assina a **mensagem** no padrão XMLDSig
4. só então embrulha no SOAP

### O `<RPS>` e o `<Detalhe>` ficam fora do namespace

```xml
<PedidoEnvioRPS xmlns="http://www.prefeitura.sp.gov.br/nfe">
  <Cabecalho xmlns="" Versao="1">...</Cabecalho>
  <RPS xmlns="">...</RPS>          <!-- sem namespace -->
</PedidoEnvioRPS>
```

Sem o `xmlns=""` a resposta é
`has invalid child element 'RPS' in namespace '...'` — que parece elemento
errado e é namespace.

No cancelamento vale a mesma regra, e `<transacao>` (minúsculo) é **filho** de
`<Cabecalho>`, não irmão.

### A resposta vem em `<RetornoXML>`

Com o `X` maiúsculo, e o XML real vem **escapado como texto** dentro dele.
Procurar `<Erro>` direto no SOAP não acha nada: é preciso desescapar e parsear
de novo.

---

## O leiaute depende do regime tributário — e a prefeitura decide

`xsd-version: 1` (clássico) ou `2` (IBS/CBS, LC 214/2025).

A escolha **não é sua**: a prefeitura compara com o cadastro do prestador e
recusa com

> **641** — *Contribuinte cadastrado como Simples Nacional na data informada.
> Deverá ser utilizado o leiaute 1.*

Empresa do **Simples Nacional é obrigada no leiaute 1**. Regime normal usa o 2.

Os campos também mudam:

- **leiaute 1**: exige `ValorServicos`, e `NumeroEncapsulamento` vem antes de
  `ValorTotalRecebido` (o XSD é `xs:sequence`)
- **leiaute 2**: não aceita `ValorServicos`; exige `IBSCBS` (com `cIndOp`,
  `cClassTrib`), `NCM`, `NBS`; e `ValorInicialCobrado` foi **descontinuado**
  (erro 640) — use só `ValorFinalCobrado`

### A assinatura muda de geometria com o leiaute

O erro **1206** devolve a string que a prefeitura calculou, o que torna o
diagnóstico direto:

> *Assinatura Digital do RPS incorreta - String verificada
> (02130033BC   000000000001202609251NN00000000000010000000000000000000101100086946749120)*

| posição | leiaute 1 | leiaute 2 |
|---|---|---|
| 1 — IM do prestador | **8** dígitos | 12 |
| 8 — valor | **Valor dos Serviços** | ValorFinalCobrado |

Sem intermediário a cadeia tem **86** posições (itens 1 a 12 do manual, item
4.3.2); com intermediário, 102. Acesar as posições 13-15 sem intermediário
produz uma assinatura que ela recusa.

---

## Códigos de serviço

O código municipal de São Paulo tem **4 dígitos** e **não** é o da LC 116
nacional:

| LC 116 | São Paulo | Serviço |
|---|---|---|
| 01.01 | 2660 | Análise e desenvolvimento de sistemas |
| 01.02 | 2668 | Programação |
| 01.03 | 2684 | Processamento, armazenamento ou hospedagem |
| 01.04 | 2692 | Elaboração de programas (software) |
| 01.05 | 2800 | Licenciamento de programas |
| 01.06 | 2881 | Assessoria e consultoria em informática |
| **01.07** | **2919** | **Suporte técnico em informática** |
| 01.08 | 2935 | Páginas eletrônicas |
| 01.09 | 2961-2963 | Conteúdo pela internet |

`0107` passa no XSD mas responde **306 inexistente**; `107` e `010701` são
recusados pelo XSD. Se o código existir na tabela mas não estiver vinculado ao
prestador, sai **alerta 307** — a nota é emitida, mas com aviso.

Mapeamento completo (314 pares) no arquivo `codigos-servico-sp.json`.

---

## Erros de negócio mais comuns

| Código | Significado |
|---|---|
| 223 | RPS já processado; a NF-e dele está cancelada. O número do RPS não retrocede |
| 306 | Código de serviço inexistente na tabela municipal |
| 307 | Código existe mas não está cadastrado para este prestador |
| 640 | `ValorInicialCobrado` descontinuado — use `ValorFinalCobrado` |
| 641 | Regime tributário não bate com o leiaute enviado |
| 1102 | Elemento do corpo errado (falta o sufixo `Request`) |
| 1206 | Assinatura do RPS incorreta (devolve a string verificada) |
| 1223 | Serviço tributado em SP ou exportação não informa `MunicipioPrestacao` |
| 1301 | NFS-e já cancelada |
| 1305 | Assinatura de cancelamento incorreta |

---

## Como rodar

```bash
mvn package

export NFSE_SP_ENABLED=true
export NFSE_SP_CNPJ=00000000000191
export NFSE_SP_XSD_VERSION=1          # 1 se for Simples Nacional
export NFSE_SP_CERT_PATH=/caminho/cert.pfx
export NFSE_SP_CERT_PASS=...          # a senha NUNCA vai para o banco

java -jar target/nfse-sp-api.jar
```

Conferir antes de emitir:

```bash
curl -X POST http://127.0.0.1:4567/api/nfse-sp/consulta-cnpj \
  -H 'Content-Type: application/json' -d '{"cnpj":"00000000000191"}'
```

```json
{"sucesso":true,"inscricao_municipal":"2130033","emite_nfse":true,"alertas":[]}
```

---

## Segurança

- A senha do `.pfx` **nunca** é gravada em lugar nenhum. Quem abre o
  certificado guarda a senha na própria estação.
- **Esta API não tem autenticação.** Ela assina nota fiscal: antes de expor em
  rede, coloque atrás de HTTPS com autenticação (mTLS ou token). O padrão do
  Spring Security não se aplica aqui de propósito — é uma API de serviço, não
  um módulo de ERP.
- O `GET /status` mostra se certificado e senha estão configurados, mas nunca
  revela a senha.

---

## Relação com o bridge em Ruby

A pasta `../nfse-sp-bridge` (Sinatra + gem `nfse_prefeitura_sp`) **não é código
morto**. Ela roda em paralelo e é o **fallback** desta API. Ver
`../nfse-failover/README.md` para o mecanismo de troca.

Por que ela continua no ar:

- é a que **validou** as regras contra a prefeitura primeiro, e o `brasil-saas.md`
  dela registra o que foi testado, com os erros literais que a prefeitura devolve
- serve de **oráculo**: o `validar/xml-bruto.rb` gera o mesmo XML desta API
  para comparação byte a byte. Foi essa comparação que pegou dois bugs aqui
  (a assinatura no envelope SOAP em vez de dentro do `MensagemXML`, e as
  posições 13-15 da cadeia de assinatura sendo acrescentadas sem intermediário)
- cobre a falha desta API. O certificado A1 em RC2-40-CBC obriga o Ruby a carregar
  o provider `legacy` do OpenSSL; o provider `SUN` do Java aceita sem nada disso,
  mas o caminho Ruby é o único que já se provou em produção três vezes

Para isso o bridge expõe as mesmas rotas desta API em `/api/nfse-sp` (além das
originais `/emitir_rps`, `/consulta_cnpj` e `/cancelar_nfse`). O contrato é o
mesmo campo a campo — inclusive o `xml_assinado` no retorno, que o ERP arquiva
no Mongo com prazo de 5 anos. Uma nota pode ser emitida por uma implementação e
cancelada pela outra.

> A gem Ruby também é a implementação de referência das **regras** de negócio
> (leiaute 1 e 2, códigos de serviço, mensagens de erro). Se um dia ela for
> removida, o `brasil-saas.md` do bridge precisa ter sido copiado para cá — ele
> documenta o que só se aprende emitindo nota de verdade.

---

## O que falta

- **Autenticação** — a API não tem nenhuma (ver *Segurança*).
- **Consulta de NFS-e** — o WebService oferece `ConsultaNFe`, `ConsultaLote` e
  `ConsultaInformacoesLote`, e esta API implementa só as três operações
  marcadas em `NfseSpOperacoes` (consulta de CNPJ, envio de RPS e
  cancelamento). As operações restantes já estão declaradas ali, com o
  elemento e o `SOAPAction` corretos, prontas para implementar.
- **Emissão em lote** — `EnvioLoteRPS` e `TesteEnvioLoteRPS` também declarados.

## Licença

A definir. Sem licença explícita, ninguém pode legalmente reutilizar — o que é
uma escolha legítima, mas convém deixar isso consciente.

