# ANALISE DO FLUXO FISCAL

> 29/09/2026 · leitura **profunda** das APIs de `src/main/resources/microservices/`,
> não superficial. Pergunta que responde, explicitamente: **estou reutilizando
> algo existente ou construindo algo novo?**
> Documento par: `RELATORIO-ENDPOINTS-ORFAOS.md`.

---

## 1. A RESPOSTA, PRIMEIRO

> **Estou REUTILIZANDO. Não estou construindo do zero.**

A camada de assinatura XML do `nfse-sp-api` é **exatamente** a camada que
NF-e e NFC-e exigem, e ela já está escrita, testada contra a prefeitura e em
produção. Uma NFC-e feita do zero repetiria essa lógica e arriscaria estar
**errada** onde a existente já foi provada.

| Peça | Existe? | Onde |
|---|---|---|
| Assinatura XMLDSig com C14N exclusiva | **SIM, funcionando** | `NfseSpSigner.assinarXml` |
| Leitura de certificado A1 do disco | **SIM** | `NfseSpCertificadoService.carregar` |
| Montagem de XML para web service | **SIM** | `NfseSpXmlBuilder` |
| Cliente SOAP | **SIM** | `NfseSpClient` |
| Interpretação de resposta (sucesso/alerta/erro) | **SIM** | `NfseSpRespostaParser` |
| Contrato de resposta estável | **SIM** | `RespostaNfsePadrao` |
| Health check e watchdog de contrato | **SIM** | `nfse-watchdog` |
| **Conversa com a SEFAZ** | **NÃO** | só em NFS-e |
| **Modelo 65 (NFC-e)** | **NÃO** | inexistente |
| **Contingência offline** | **NÃO** | inexistente |

**O que falta é a conversa com a SEFAZ e o modelo 65. O resto — inclusive a
parte difícil, que é a assinatura — já está pronto.**

---

## 2. O QUE FOI LIDO, E O QUE CADA API FAZ

### 2.1 `nfse-sp-api` — a única API fiscal em produção

**15 arquivos Java.** Roda em `:4568`, `active`. A mais completa do conjunto.

| Classe | Papel |
|---|---|
| `NfseSpController` | 4 verbos: `/status`, `/emitir-rps`, `/cancelar`, `/consulta-cnpj` |
| `NfseSpService` | orquestra: monta, assina, envia, interpreta |
| `NfseSpXmlBuilder` | monta o XML SOAP (`envioRps`, `consultaCnpj`) |
| **`NfseSpSigner`** | **XMLDSig + assinatura da RPS** |
| `NfseSpCertificadoService` | carrega A1 (`.pfx`) com senha da estação |
| `NfseSpClient` | SOAP |
| `NfseSpRespostaParser` | extrai sucesso, alertas, erros, chave |
| `RespostaNfsePadrao` | contrato de resposta |
| `NfseSpException` + `NfseSpExceptionHandler` | tratamento de erro |
| `NfseSpProperties` | config por property |

### 2.2 `NfseSpSigner` — a peça que decide tudo

Este é o achado que muda a decisão. Lido:

```java
public String assinarXml(String xml, PrivateKey chave, X509Certificate certificado) {
    XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        newSignatureMethod(SignatureMethod.RSA_SHA1)
        newReference(
            newDigestMethod(DigestMethod.SHA1),
                Transform.ENVELOPED,
                Transform.ALGO_C14N_EXCLUSIVA)      // C14N exclusiva
    KeyInfo com o X509Data
    XMLSignature assinatura = factory.newXMLSignature(signedInfo, keyInfo);
```

**RSA-SHA1, digest SHA1, transform ENVELOPED e C14N exclusiva.** É literalmente
a especificação de assinatura de NF-e e NFC-e. A única diferença em relação à
NFC-e é a versão do XSD (4.00) e o modelo do documento (65).

`assinarTexto` assina a cadeia da RPS com `SHA1withRSA` e devolve Base64.

**O certificado:** `NfseSpCertificadoService.carregar()` lê o `.pfx` de disco,
com a senha **informada pela estação** — o comentário diz explicitamente que a
senha *"nunca vem do banco"*. É a decisão correta e jáTOMADA.

### 2.3 O contrato de resposta

`RespostaNfsePadrao`:

```java
sucesso, inscricao_municipal, numero_nfse, codigo_verificacao,
chave_nota_nacional, alertas[], erro, xml_assinado
```

Verificado ao vivo:

```
GET http://127.0.0.1:4568/api/nfse-sp/status
{"sucesso":true,...,"habilitada":true,"url":"https://nfe..."}
```

O `controller` embrulha isso em `contrato_nfse(...)`. **Contrato estável é o que
permite trocar de implementação sem quebrar o ERP** — e foi o que o incidente de
26/09 expôs.

### 2.4 `nfse-watchdog` — a lição de um incidente real

O cabeçalho do script é o melhor documento de todo o conjunto, e registra um
acidente que aconteceu:

> "o nginx já troca de upstream quando a conexão falha. O que ele não pega é a
> implementação que responde 200 e devolve o contrato errado — e é exatamente o
> que aconteceu em 26/09/2026: com a API Java fora, o Ruby emitiu a nota 29 com
> success=true, a prefeitura aceitou, e o ERP respondeu erro porque lia
> "sucesso" e o Ruby devolve "success" um nível abaixo, dentro de "error".
> O nginx viu sucesso. O ERP quebrou."

**Leitura: a nota foi emitida de verdade e o ERP não soube.** A questão é
contrato, não de disponibilidade. Por isso o watchdog checa **o contrato, não a
porta**: *"uma porta aberta não prova que a implementação serve"*.

Essa é a lição que uma API de NFC-e precisa incorporar desde o primeiro dia.

### 2.5 `nfse-sp-bridge` — o fallback

Ruby, `:4569`, responde 200. Só entra quando a API Java cai.

### 2.6 A 4567 e' o `nfse-failover.rb`, por decisao do dono — e ele nao esta no ar

**Correcao minha.** Escrevi antes que "o gateway da 4567 nao existe". Errado pela
metade: o proxy existe, foi desenhado, e a 4567 e' dele. O que nao esta no ar
neste servidor e' a **unit** dele.

O `.rb` esta no repositorio e documenta o desenho:

```
ERP -- 4567 --> nfse-failover (este)      pick the first healthy
                       +----------------+
                       v                v
        nfse-sp-api 4568 (java)   nfse-sp-bridge 4569 (ruby)
```

E o proprio codigo le as tres portas:

```ruby
PORTA = Integer(ENV.fetch('NFSE_FAILOVER_PORTA', '4567'))
UPSTREAM = 'java=http://127.0.0.1:4568,ruby=http://127.0.0.1:4569'
```

**Quem decide e' o dono, e a decisao mudou duas vezes:**

| Data | Commit | Decisao |
|---|---|---|
| 26/09 | — | proxy **nginx** na 4567, por causa do bug de `Transfer-Encoding: chunked` do Ruby (`docs/pesquisa/proxy-nginx-e-bug-do-fallback.md`) |
| 27/09 | `1ae94a78` | **"O Ruby volta a ser o proxy"** — *"DECISAO DO DONO: o proxy da 4567 e' o nfse-failover.rb. O nginx ficou so com a separacao dos papeis"* |
| 28/09 | `b974f00c` | o instalador passa a **gerar** a unit `brasil_saas-nfse-failover.service` |

A decisao vigente e' a do Ruby. O `installbase.sh` de hoje e' coerente com ela:
remove o conf da 4567 do nginx (linha 1727), para e apaga a unit antiga
(linhas 1754-1757) e **cria** a nova (linha 2165):

```
/etc/systemd/system/brasil_saas-nfse-failover.service
  WorkingDirectory=$MS_NFSE/nfse-failover
  ExecStart=$RUBY_NFSE nfse-failover.rb
  StandardOutput=append:/var/log/brasil-saas/nfse-failover.log
```

**O que medi neste servidor:**

| Porta | Estado | Quem e' |
|---|---|---|
| 4567 | **HTTP 000** | o proxy Ruby — **ausente** |
| 4568 | HTTP 200 | `nfse-sp-api` (java, pid 173990) |
| 4569 | HTTP 200 | `nfse-sp-bridge` (ruby, pid 1101) |

```
$ systemctl list-unit-files | grep -i nfse
  nfse-sp-api.service      enabled
  nfse-sp-bridge.service   enabled
  # brasil_saas-nfse-failover.service: NAO EXISTE
$ ls /var/log/brasil-saas/ | grep nfse-failover
  # nada
```

A unit nao esta instalada porque o bloco que a cria chegou no instalador em
**28/09**, e **esta maquina nao foi reinstalada** desde entao. Nao e' regressao:
e' instalacao incompleta.

**Consequencia:** o ERP aponta para a 4567 e nao ha ninguem escutando.

```java
// NfseEmissaoService.java:74
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}") String url,
```

Sem override em lugar nenhum: `application.yml` nao menciona, o `.env` (1951
bytes) nao menciona, e `/etc/brasil-saas/nfse-sp.env` so tem as portas das
implementacoes (`NFSE_SP_PORT=4568`, `NFSE_SP_BRIDGE_PORTA=4569`) mais a URL da
prefeitura. **A emissao de NFS-e pelo ERP esta quebrada em producao agora.** As
duas implementacoes estao no ar e respondem 200; falta so o roteador na frente.

**Correcao pendente, e pequena:** criar a unit
`brasil_saas-nfse-failover.service` a partir do bloco do `installbase.sh`
(linhas 2164-2196), sem reinstalar nada. O bloco e' idempotente e ja tem
verificacao. **Nao** mudar o default do codigo para a 4568: isso perderia o
failover, que e' exatamente o que o dono pediu ao voltar para o Ruby.


### 2.7 As demais — material, não serviço

| Pasta | O que é | Serve para NFC-e? |
|---|---|---|
| `Java_NFe` | API open-source Sw Consultoria, 367 arquivos, **com NFC-e** (`NFCeUtil`, `danfce.jasper`) | **sim, como referência de leiaute** |
| `nfe` | NF-e nacional, 2.178 arquivos | sim, referência |
| `nfse` / `nfse-nacional-api` | NFS-e nacional | não é varejo |
| `nfse_prefeitura_sp` | gem Ruby, web service real de SP | não é varejo |
| `NFSe-SaoPaulo-SP` | fonte oficial | não |
| `Java_CTe` / `Java_MDFe` | CT-e e MDF-e | não |
| `esocial` | app do TST, 5.073 arquivos | não é do ERP |
| `PL_MDFe_300b_NT012025_1.05` | 41 XSD oficiais do MDF-e 3.00b | os XSD **de MDF-e**; a NFC-e precisa dos **dela** |

---

## 3. O QUE REAPROVEITAR, ADAPTAR, E O QUE FALTA

### 3.1 Reaproveitar como está

| Peça | Motivo |
|---|---|
| `NfseSpSigner.assinarXml` | XMLDSig com C14N exclusiva é o que a SEFAZ exige. **Reescrever é risco, não economia** |
| `NfseSpCertificadoService` | Leitura de A1 com senha da estação, já decidida |
| `RespostaNfsePadrao` | Contrato estável, que é o que permite failover |
| `NfseSpExceptionHandler` | Tratamento de erro |
| A lição do watchdog | Checar contrato, não porta |

### 3.2 Adaptar

| Peça | Adaptação para NFC-e |
|---|---|
| `NfseSpXmlBuilder` | Novo método com o XML da NFC-e, namespace e modelo 65 |
| `NfseSpClient` | Aponta para o **WS de NFC-e da SEFAZ**, não o da prefeitura |
| `NfseSpRespostaParser` | Ler `cStat`/`xMotivo` e o **QR Code (CSC)**, que a NFC-e exige e a NFS-e não |
| `NfseSpController` | Quatro verbos viram `/status`, `/emitir`, `/cancelar`, `/consultar` |
| `nfse-watchdog` | Novo alvo, com checagem de contrato da NFC-e |

### 3.3 O que falta de verdade

| Falta | Observação |
|---|---|
| **Emissão de NFC-e contra a SEFAZ** | Não existe para nenhum estado. O ERP só conversa com a prefeitura de SP, via NFS-e |
| **Modelo 65** | o `Java_NFe` tem o XSD e o utilitário, mas nada monta o XML |
| **QR Code e CSC** | código de segurança do|John, por empresa, guardado cifrado |
| **Identificador de terminal e sequência NFC-e** | 2 dígitos, por instalação |
| **Contingência** | `epc`/FS-DA/FS-DAT, que a NFC-e exige. É a parte mais difícil e não tem nada |
| **Item fiscal** | `bc_fis_nfce_item` tem 7 colunas, nenhuma fiscal. `bc_fis_nfe_item` tem as 21 certas — **modelo de migration** |
| **Tributação no cálculo** | `RegraTributaria` e `SefazConsultaService` existem como estrutura |

---

## 4. A DECISÃO

> **Estou reutilizando a fundação de assinatura e o contrato de resposta.
> Não estou construindo do zero. A parte nova é a conversa com a SEFAZ, o
> modelo 65, o QR Code e a contingência.**

O caminho barato seria criar uma API de NFC-e do zero. O caminho correto é:

1. **Uma microsserviço novo de NFC-e**, que **copie a estrutura** do
   `nfse-sp-api` (13 classes, mesmas responsabilidades, mesmos nomes) e troque o
   que é específico: o XML, o cliente SOAP e o parser da resposta.
2. **Reaproveitar `NfseSpSigner` e `NfseSpCertificadoService` como estão.** São
   a assinatura e o certificado, que não têm nada de específico de prefeitura.
3. **Autenticar.** O `nfse-sp-api` não tem autenticação nenhuma — medido. Uma
   API que emite documento fiscal para a SEFAZ não repete isso.
4. **Gateway com failover**, aprendendo com o incidente de 26/09: a checagem é
   de contrato, não de porta.

**Estimativa honesta:** a assinatura, que é o difícil, está pronta. O trabalho
real é SEFAZ + contingência, que é integração — exatamente o motivo de o TEF
estar por último na sua lista.

---

## 5. O QUE IMPORTA AGORA, E NAO E' NFC-e

**A 4567 nao tem o proxy, e o ERP aponta para ela.** Ver §2.6.

| Medido | |
|---|---|
| `4567` | HTTP 000 — ninguem escuta |
| `4568` (`nfse-sp-api`) | HTTP 200 |
| `4569` (`nfse-sp-bridge`) | HTTP 200 |
| default no codigo | `http://127.0.0.1:4567/api/nfse-sp` |
| override em `application.yml` / `.env` | **nenhum** |

A 4567 e' do `nfse-failover.rb`, por decisao do dono no commit `1ae94a78`. A
unit que o instala foi acrescentada ao `installbase.sh` em `b974f00c` (28/09) e
esta maquina nao foi reinstalada depois. As duas implementacoes estao no ar e
saudaveis — o que falta e' so o roteador.

Duas saidas, e so uma e' certa:

| | |
|---|---|
| **Criar a unit `brasil_saas-nfse-failover.service`** | **e' esta.** E' o desenho que o dono escolheu, mantem o failover, e o bloco ja existe e e' idempotente (`installbase.sh` 2164-2196) |
| Apontar o default do codigo para a 4568 | resolve a chamada e **perde o failover**: se a API Java cair, a NFS-e cai junto, que e' o problema que o `nfse-watchdog` existe para evitar |

**Nao mexo nisso sem a sua ordem**, porque e' producao e e' servico do outro
lado da sua decisao de 27/09. E' uma linha de comando, nao uma mudanca de
arquitetura.

## 6. COMO VERIFICAR

```bash
M=~/BrasilCloudERP/src/main/resources/microservices
grep -n "C14N_EXCLUSIVA\|ENVELOPED" $M/nfse-sp-api/src/main/java/**/NfseSpSigner.java
grep -n "public " $M/nfse-sp-api/src/main/java/**/NfseSpController.java
for p in 4567 4568 4569; do
  echo -n "  $p: "; curl -s -m 5 -o /dev/null -w "%{http_code}\n" http://127.0.0.1:$p/api/nfse-sp/status
done
head -30 $M/nfse-watchdog/*        # a lição do incidente
grep -n "nfse.url" ~/BrasilCloudERP/src/main/java/**/NfseEmissaoService.java

# o proxy da 4567 e' o Ruby, e a unit e' esta:
systemctl list-unit-files | grep -i nfse
ls /etc/nginx/conf.d/brasil-saas-nfse.conf 2>&1   # nao deve existir: o dono trocou pelo Ruby
grep -n "NFSE_FAILOVER_PORTA\|java=http" $M/nfse-failover/nfse-failover.rb
sed -n "2164,2196p" ~/BrasilCloudERP/installbase.sh   # o bloco que cria a unit
```

