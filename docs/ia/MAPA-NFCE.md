> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

---
id: 2026-09-29-mapa-nfce
status: levantamento
data: 2026-09-29
---

# MAPA DA NFC-e: o que ja existe, o que se reaproveita, o que falta

> Pergunta que responde, nas palavras do dono: **estou reutilizando algo
> existente ou construindo algo novo?**
> Documentos pares: `RELATORIO-ENDPOINTS-ORFAOS.md`, `ANALISE-FLUXO-FISCAL.md`.
>
> Isto **nao e' implementacao**. E' o mapa que tem de vir antes dela.

---

## 1. A RESPOSTA

> **Reaproveitando. Em nenhuma parte estou construindo do zero.**

A unica peca que teria de ser escrita de cima e' a **conversa com a SEFAZ**,
que e' o mesmo trabalho da NF-e — e a NF-e ja tem a assinatura, a configuracao,
o certificado, a biblioteca e a chamada prontas. O que falta e' o **modelo 65**,
que e' NF-e com quatro campos trocados.

| Camada | Estado | Origem |
|---|---|---|
| Biblioteca com o modelo 65 | **pronto** | `java-nfe 4.1.3` no `pom.xml` |
| QR Code (online e contingencia) | **pronto** | `NFCeUtil` no jar |
| CSRT (hash do certificado + CSC) | **pronto** | `NFCeUtil.geraHashCSRT` |
| Layout do XML | **pronto** | `leiauteNFe_v4.00.xsd` (NFC-e usa o mesmo) |
| DANFE-e impresso | **pronto** | `danfce.jasper` / `.jrxml` |
| Exemplos de emissao | **prontos** | `EnvioNfceTeste`, `EnvioNfceContingenciaTeste` |
| Assinatura XMLDSig | **pronto** | `NfseSpSigner.assinarXml` (microservico) |
| Certificado A1 do disco | **pronto** | `NfseSpCertificadoService` |
| Arquivamento XML/PDF com prazo | **pronto** | `NfseArquivoService` |
| Tabela de retorno por operacao | **pronto** | `bc_fis_nfse_retorno` |
| Configuracao SEFAZ por UF | **pronto** | `DynamicNFeConfig`, `SefazProperties` |
| **Colunas fiscais no item** | **falta** | `bc_fis_nfce_item` tem 7, precisa 21 |
| **CSC por empresa, guardado** | **falta** | 0 ocorrencias no ERP |
| **Controller e service** | **falta** | nao existem |
| **Cliente SOAP da SEFAZ** | **falta** | so a prefeitura de SP e' chamada |

---

## 2. O QUE JA EXISTE

### 2.1 A modelagem da NFC-e: comecou, e parou no cabecalho

| | |
|---|---|
| `bc_fis_nfce` | **17 colunas**, 58 linhas seed |
| `bc_fis_nfce_item` | **7 colunas**, 58 linhas seed |
| `model/Nfce.java` | existe — `pessoaId, numero, serie, chaveAcesso, dataEmissao, status, protocolo, valorTotal, xml` |
| `model/NfceItem.java` | existe — `nfce, produtoId, quantidade, valorUnitario, valorTotal` |
| `repository/NfceRepository`, `NfceItemRepository` | existem |
| Controller | **nao existe** |
| Service | **nao existe** |

O cabecalho esta bom. As 17 colunas ja tem o que uma nota emitida precisa:

```
id, uuid, empresa_id, pessoa_id, numero, serie, chave_acesso, data_emissao,
status, protocolo, valor_total, xml, created_at, updated_at, created_by,
updated_by, deleted_at
```

`chave_acesso`, `protocolo` e `xml` sao exatamente o que a SEFAZ devolve. Falta
no cabecalho: `ambiente`, `tp_emissao`, `csc_id`, `qr_code`, `id_terminal`,
`nseq`, e o grupo de contingencia.

**O item nao serve**, e e' o unico bloqueio de migration:

```
bc_fis_nfce_item:  id, nfce_id, produto_id, quantidade, valor_unitario,
                   valor_total, created_at
```

Sete colunas. **Nenhuma fiscal.** Um item de NFC-e sem CFOP nao gera XML.

### 2.2 A peca que resolve a migration: `bc_fis_nfe_item`

**21 colunas, e sao exatamente as que faltam:**

```
id, nfe_id, produto_id, numero_item, ncm, cfop, cest, quantidade, unidade,
valor_unitario, valor_total, aliquota_icms, valor_icms, aliquota_ipi,
valor_ipi, codigo_produto, codigo_barras, uuid, created_at, updated_at,
deleted_at
```

`cfop`, `ncm`, `cest`, `unidade`, `aliquota_icms`, `codigo_barras`. **A tabela
de item fiscal pronta ja existe no banco.** A `bc_fis_nfce_item` e' uma copia
enxuta que nunca foi usada.

> **Decisao: ampliar `bc_fis_nfce_item` a partir de `bc_fis_nfe_item`. Nao criar
> tabela nova, nao recriar coluna por coluna sem referencia.**

---

## 3. O QUE SE REAPROVEITA DA NFS-e

O motivo de a NFS-e ser a melhor referencia nao e' o negocio (varejo != servico).
E' porque **a parte dificil — assinatura — ja foi escrita e provada**.

### 3.1 `NfseSpSigner.assinarXml` — a peca que decide tudo

```java
XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
    newSignatureMethod(SignatureMethod.RSA_SHA1)
    newReference(newDigestMethod(DigestMethod.SHA1),
        Transform.ENVELOPED,
        Transform.ALGO_C14N_EXCLUSIVA)     // C14N exclusiva
    KeyInfo com o X509Data
```

**RSA-SHA1, digest SHA1, transform ENVELOPED, C14N exclusiva.** E' a
especificacao de assinatura de NF-e e NFC-e, nao da NFS-e. A NFS-e de SP nao
exige XMLDSig — este codigo foi escrito porque o autor sabia o que vinha.

**Reaproveitar como esta. Reescrever e' risco, nao economia.**

### 3.2 `NfseSpCertificadoService` — A1 do disco

```java
// le .pfx, e o comentario e' explicito:
/** A senha nunca vem do banco: e informada pela estacao que esta emitindo. */
```

Decisao correta, ja tomada, e a mesma que a NFC-e precisa.

### 3.3 `NfseArquivoService` — e o achado que ninguem esperaria

Este e' o reaproveitamento mais barato de todos, e o proprio codigo ja pensa em
QR Code:

```
XML: 5 anos. Exigencia legal de guarda do documento fiscal eletronico.
PDF: 60 dias. Serve para reimprimir e conferir o QR Code.
```

| | |
|---|---|
| Onde guarda | **MongoDB**, colecao `documentos`; o Postgres guarda so o ponteiro |
| Prazo | `nfse.retencao-dias-xml` (1825) e `-pdf` (60), por property |
| Chave | `(empresa, tipoEntidade, entidadeId)` — separado por tipo, senao salvar o PDF apaga o XML |
| Limpeza | `@Scheduled`, com `limpeza-ativa:false` por padrao |

Para NFC-e, `TIPO_PDF` vira o **DANFE-e** e o QR Code e' impresso nele. O
servico ja foi desenhado para guardá-lo e para reimprimi-lo.

### 3.4 `bc_fis_nfse_retorno` — o padrao de retorno

18 colunas, e **tabela, nao fila de verdade**:

```
id, uuid, empresa_id, nfse_id, operacao, sucesso, http_status, duracao_ms,
mensagem, codigo, alertas, documento_id, created_at, updated_at, deleted_at,
created_by, updated_by
```

Toda tentativa de emissao vira uma linha: sucesso, status HTTP, quanto tempo
levou, o que a prefeitura respondeu. **E' o que permite responder "o que
aconteceu com a nota 47?"** — e a NFC-e precisa disso em dobro, porque no
varejo o cliente esta na frente do balcao.

### 3.5 A licao do `nfse-watchdog`

O cabecalho do script registra um acidente real de 26/09: com a API Java fora, o
Ruby emitiu a nota 29 com `success=true`, a prefeitura aceitou, e o ERP
respondeu erro — porque lia `"sucesso"` e o Ruby devolvia `"success"`. **A nota
foi emitida de verdade e o ERP nao soube.**

Daí a regra: *"uma porta aberta nao prova que a implementacao serve"*. O
watchdog checa **o contrato**, nao a porta.

### 3.6 `RespostaNfsePadrao` — o contrato estavel

```java
sucesso, inscricao_municipal, numero_nfse, codigo_verificacao,
chave_nota_nacional, alertas[], erro, xml_assinado
```

Contrato estavel e o que permite trocar de implementacao sem quebrar o ERP. E'
a mesma propriedade que uma API de NFC-e precisa ter — **e com autenticacao,
que a de NFS-e nao tem.**

---

## 4. O QUE SE REAPROVEITA DA NF-e

### 4.1 A biblioteca ja esta no classpath

`pom.xml`, linha 234-236:

```xml
<groupId>br.com.swconsultoria</groupId>
<artifactId>java-nfe</artifactId>
<version>${java-nfe.version}</version>   <!-- 4.1.3 -->
```

E o jar esta no `.m2` local, entao **o build offline funciona**:

```
~/.m2/repository/br/com/swconsultoria/java-nfe/4.1.3/java-nfe-4.1.3.jar
```

### 4.2 Os metodos que o TODO cita existem

O `NFeServiceImpl` diz, textualmente:

```java
// TODO: montar TEnviNFe a partir do pedido e chamar
//       br.com.swconsultoria.nfe.Nfe.montaNfe/envia
```

Verifiquei com `javap` no jar instalado. **O comentario esta certo:**

```java
public static TEnviNFe     montaNfe(ConfiguracoesNfe, TEnviNFe, boolean)
public static TRetEnviNFe  enviarNfe(ConfiguracoesNfe, TEnviNFe, DocumentoEnum)
public static TRetConsSitNFe  consultaXml(ConfiguracoesNfe, String, DocumentoEnum)
public static TRetConsStatServ statusServico(ConfiguracoesNfe, DocumentoEnum)
public static TRetEnvEventoCancelamento cancelarNfe(ConfiguracoesNfe, TEnvEventoCancelamento, boolean, DocumentoEnum)
public static TRetEnvEventoEpec enviarEpec(ConfiguracoesNfe, TEnvEventoEpec, boolean)
```

A configuracao ja e' montada por `DynamicNFeConfig.criar(ufCodigo, ambienteCodigo, ...)`
e o certificado ja vem de `CertificadoDigitalService`. **A chamada e' uma linha
de distacia do TODO.**

### 4.3 O modelo 65 e' nativo na biblioteca

```
DocumentoEnum:  NFE, NFCE
ServicosEnum:   EPEC, INUTILIZACAO, URL_CONSULTANFCE
```

E o layout do XML:

> **A NFC-e nao tem XSD proprio. Ela usa o `leiauteNFe_v4.00.xsd`, com
> `mod="65"` e `tpNF="1"`.** O leiaute esta no jar, validado, e e' o mesmo da
> NF-e.

Por isso a diferenca entre os dois e' pequena, e e' **campo, nao arquitetura**:

| | NF-e | NFC-e |
|---|---|---|
| modelo | 55 | **65** |
| destinatario | pessoa juridica ou fisica identificada | consumidor, **pode ser nao identificado** |
| `indPres` | 0, 1, 2, 3, 4, 5 | **sempre 1** (presencial) |
| `idTerminal` / `nSeqNFCe` | nao | **2 + 9 digitos, por instalacao** |
| QR Code | nao | **obrigatorio** |
| impressao | DANFE, A4 | **DANFE-e, 58 ou 80 mm** |
| contingencia | EPEC | **EPEC e offline** |

**Esta e' a razao tecnica de a NFC-e ser reaproveitamento e nao construcao:
o documento e' o mesmo, com quatro campos diferentes.**

### 4.4 QR Code e CSRT: prontos, e nas duas modalidades

`NFCeUtil` existe **no jar compilado** (nao so no fonte), com:

```java
getCodeQRCode(...)                  // v2, usa CSC
getCodeQRCodeV3(...)                // online, nao usa CSC no QR
getCodeQRCodeContingencia(...)
getCodeQRCodeContingenciaV3(...)
geraHashCSRT(certificadoBase64, csc)   // hash do A1 + CSC
```

E ha teste: `NFCeUtilTest.java`, mais os exemplos
`EnvioNfceTeste.java` (online) e `EnvioNfceContingenciaTeste.java`.

### 4.5 O DANFE-e esta desenhado

```
Java_NFe/src/main/resources/jasper/nfce/danfce.jasper
Java_NFe/src/main/resources/jasper/nfce/danfce.jrxml
```

**O ERP ja tem um relatorio Jasper rodando** (`RelatorioService` com 291 linhas
e `PdfGeneratorService` com 283, ambos sem tela). A impressao do cupom e'
adaptacao de layout, nao construcao de motor.

### 4.6 `NfeImportacaoService` — 378 linhas, o maior service fiscal pronto

Nao serve para NFC-e (e' **entrada**, nao saida), mas serve de referencia de como
se trata XML fiscal no ERP: 6 endpoints em `EntradaNotaController`, `V102` e
`V103` aplicadas, **zero sinal de stub**.

---

## 5. O QUE FALTA **EXCLUSIVAMENTE** PARA NFC-e

Aqui esta a fronteira. Tudo o que esta nesta secao e' **inevitavelmente novo**.

### 5.1 Migration: colunas fiscais no item

**O unico bloqueio estrutural. Uma migration.**

Origem: `bc_fis_nfe_item` (21 colunas). Alvo: `bc_fis_nfce_item` (7).

| Coluna | Para que |
|---|---|
| `numero_item` | o XML numera os itens |
| `ncm` | obrigatorio no item |
| `cfop` | **obrigatorio. Sem isso nao ha XML** |
| `cest` | quando aplicavel |
| `unidade` | UN/IN (UN, PC, CX, KG) |
| `aliquota_icms`, `valor_icms` | imposto |
| `codigo_produto`, `codigo_barras` | GTIN / codigo do vendedor |
| `origem` | 0 a 8 (0 = nacional) |
| `inf_adicoes` |Infos adicionais |

No cabecalho: `ambiente`, `tp_emissao`, `csc_id`, `qr_code`, `id_terminal`,
`nseq`, e o grupo de contingencia.

### 5.2 CSC por empresa, guardado cifrado

**0 ocorrencias de "CSC" em `src/main/java`.** O ERP nao tem onde guardar o Codigo
de Seguranca do Contribuinte.

| | |
|---|---|
| O que e' | par `(CSC, ID do CSC)` que a SEFAZ entrega por empresa, para gerar o QR Code v2 e o CSRT |
| Onde guardar | tabela nova, **cifrado**, por empresa, com masked value no log |
| Quem gera o hash | `NFCeUtil.geraHashCSRT(certificadoBase64, csc)` — a biblioteca faz, o ERP so fornece o par |
| Ordem na implantation | **depende da SEFAZ.** Sem CSC, so sai NFC-e online com QR Code v3 |

> Esta e' a unica peca que **nao da para reaproveitar** de lugar nenhum do
> sistema. E' ela que torna a NFC-e especifica do varejo.

### 5.3 Controller e service

Nao existem. Seguindo o padrao do `nfse-sp-api`: 13 classes, 4 verbos
(`/status`, `/emitir`, `/cancelar`, `/consultar`), **com autenticacao** — que a
API de NFS-e nao tem e uma API que emite documento fiscal precisa ter.

### 5.4 Conversa com a SEFAZ

Para nenhum estado. Hoje o ERP so conversa com a prefeitura de SP, via NFS-e,
e o caminho e' HTTP/SOAP de RPS, nao o WS de NF-e da SEFAZ.

### 5.5 Contingencia

**0 ocorrencias de "contingencia", "EPEC", "FSAT" no ERP.**

A biblioteca tem `enviarEpec` e `ServicosEnum.EPEC`, e ha exemplo pronto
(`EnvioNfceContingenciaTeste`). Falta no ERP: a decisao de quando entrar em
contingencia, a fila de notas pendentes, e a transmissao quando a internet
voltar. **E' a parte mais dificil e a ultima da fila** — e por isso o TEF fica
por ultimo na sua ordem tambem.

### 5.6 Terminal e sequencia

2 digitos de identificacao e 9 de sequencia, por instalacao, com o controle de
sequencia que a SEFAZ exige (numeração seqencial sem buraco, ou declaracao de
inutilizacao — que a biblioteca tambem tem: `ServicosEnum.INUTILIZACAO`).

### 5.7 Ligacao com a venda

A `PedidoVenda` e' o que o `NFeService` **ja recebe** na assinatura. O que falta
e' o mapeamento `PedidoVenda -> TEnviNFe` com `mod=65`: itens, soma dos
`vProd`, `vDesc`, `vNF`, e o **`vTroco`** (troco do dinheiro), que so o varejo
tem.

---

## 6. A DECISAO

> **Estou reutilizando a fundacao inteira. O que e' novo e' o modelo 65, o CSC,
> a contingencia e a conversa com a SEFAZ — e a contingencia e' a ultima
> coisa, nao a primeira.**

O caminho seria o inverso do que voce mandou evitar:

| | |
|---|---|
| Criar API de NFC-e do zero | reescrever assinatura XMLDSig, leitura de A1, contrato de resposta, arquivamento, retorno por operacao — **tudo isso ja existe e foi provado** |
| Reaproveitar e escrever so o que falta | migration do item, CSC, controller, service, e a conversa com a SEFAZ |

**Ordem que eu proporia, se voce aprovar:**

| Passo | Entrega | Depende de |
|---|---|---|
| 1 | migration das colunas fiscais em `bc_fis_nfce_item` | nada |
| 2 | tabela de CSC cifrado por empresa | nada |
| 3 | service + controller de NFC-e, **em homologacao**, sem chamar a SEFAZ | 1 e 2 |
| 4 | chamada a SEFAZ para **um** estado, com certificado de teste | 3 |
| 5 | impressao do DANFE-e (reaproveitando `PdfGeneratorService`) | 4 |
| 6 | contingencia | 4, e e' o mais caro |

**O passo 1 nao depende de nada e destrava todo o resto.** E' a unica coisa que
posso comecar sem decisao de integracao.

---

## 7. COMO VERIFICAR

```bash
cd ~/BrasilCloudERP
J=~/.m2/repository/br/com/swconsultoria/java-nfe/4.1.3/java-nfe-4.1.3.jar

# 1. a migration de referencia
PGPASSWORD=ALTERE_ME psql -h 127.0.0.1 -U postgres -d brasil-saas -c \
  "\d brasil_saas.bc_fis_nfe_item" -c "\d brasil_saas.bc_fis_nfce_item"

# 2. os metodos que o TODO do NFeServiceImpl cita, no jar que o ERP usa
javap -classpath $J br.com.swconsultoria.nfe.Nfe | grep -E "montaNfe|enviarNfe|consultaXml"
javap -classpath $J br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum | grep NFCE
javap -classpath $J br.com.swconsultoria.nfe.util.NFCeUtil

# 3. a assinatura que a SEFAZ exige, ja escrita
grep -n "C14N_EXCLUSIVA\|ENVELOPED" \
  src/main/resources/microservices/nfse-sp-api/src/main/java/**/NfseSpSigner.java

# 4. o que NAO existe (a fronteira)
grep -rliE "CSC|contingencia|qrCode" src/main/java/br/com/brasil_saas | wc -l   # 0

# 5. o material do DANFE-e
ls src/main/resources/microservices/Java_NFe/src/main/resources/jasper/nfce/
```

