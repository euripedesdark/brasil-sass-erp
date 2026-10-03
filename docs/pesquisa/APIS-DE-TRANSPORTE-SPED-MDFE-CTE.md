# As três APIs de transporte: SPED, MDF-e e CT-e

Data: 26/09/2026

## O que funciona hoje, verificado

| API | Endpoint | Estado |
|---|---|---|
| **SPED EFD ICMS/IPI** | `POST /api/fiscal/sped/efd/gerar`, `GET /api/fiscal/sped/efd/exemplo` | **funciona, arquivo válido** |
| **MDF-e** | `GET /api/fiscal/mdfe/status`, `/recibo` | **cStat 107, SVRS responde** |
| **CT-e** | `GET /api/fiscal/cte/status` | **cStat 107, SVRS responde** |

As três se falam com a SEFAZ em homologação. As respostas reais, verificadas:

**MDF-e** — 1,49 s, incluindo o handshake TLS e o certificado do cliente:

```
sucesso: true    cStat: 107    xMotivo: "Servico em Operacao"
ambiente: HOMOLOGACAO       ufAtendente: RS
versaoAplicativo: RS20240709143558
tempoMedioResposta: 1
autorizador: SVRS (Virtual Ambiente Nacional)
estadoEmissor: SP
```

**CT-e**:

```
sucesso: true    cStat: 107    xMotivo: "Serviço em Operação."
ambiente: HOMOLOGACAO       ufAtendente: SP
versaoAplicativo: SP-CTe-2026-08-26-1
layout: CT-e 4.00
```

Repare na diferença: o MDF-e é atendido pela **SVRS** (`ufAtendente: RS`) e o
CT-e pelo **portal de SP** (`ufAtendente: SP`), com o mesmo A1. Os dois
responderam, mas são autorizadores diferentes.

`cStat 107` é *"Serviço em Operação"*, o único valor que significa "pode
emitir". É o health check, e o único ponto que se consulta sem assinar
documento.

---

## 1. As libs, e por que fincatto e não swconsultoria

### O conflito que matou a swconsultoria

`java-mdfe 3.00.4` (2019) chama `Certificado.getTipo()`. A
`java_certificado 3.16`, que o ERP **já usava** antes desta sessão (vem pelo
`java-nfe 4.1.3`, e `DynamicNFeConfig` e `SefazConfig` já importavam
`Certificado`), tem `getTipoCertificado()` e **não tem** `getTipo()`:

```
NoSuchMethodError: 'java.lang.String
  br.com.swconsultoria.certificado.Certificado.getTipo()'
```

Nem 3.16 nem 3.17 têm `getTipo()`. **Não há flag de Maven que resolva:** as
duas libs precisam de APIs diferentes da mesma classe, e só uma pode estar no
classpath. Por isso o `java-mdfe` saiu do `pom.xml`.

### A escolhida

`com.github.wmixvideo:nfe:5.1.2` — fincatto `documentofiscal`. Traz NFe,
**CT-e**, **MDF-e**, NFS-e, ECF e EFD na mesma base. Assina com o Apache
Santuario (`xmlsec`), **não depende de `java_certificado`**, então o conflito
não existe.

`5.1.2` é a última **release** no Central. A pasta local
`microservices/nfe` é `5.1.3-SNAPSHOT`, que não está publicado.

### Um bug do release que tive de corrigir

O `pom.xml` da fincatto 5.1.2 declara `httpclient5 5.3.1`, mas o código dela
chama `TlsSocketStrategy`, que só existe a partir da 5.4. Resultado, sem
correção:

```
NoClassDefFoundError: org/apache/hc/client5/http/ssl/TlsSocketStrategy
```

E só fixar o `httpclient5` não basta — o `httpcore5` tem de ir junto, senão:

```
DefaultHttpRequestWriterFactory.<init>(Http1Config) nao existe
```

Os dois estão pinados no `pom.xml`, com `exclusions` para a 5.3.1 não voltar
por baixo. São um par; fixar um sem o outro só troca o erro.

---

## 2. SPED EFD ICMS/IPI — funcionando

`java-efd-icms 3.21.1` e `java-efd-contribuicoes 1.32.1` no `pom.xml`.

**EFD não se envia para ninguém.** O arquivo é gerado, assinado e guardado; quem
busca depois é a SEFAZ ou a Receita. Não há web service, não há protocolo, não
há fila. Por isso é testável inteiro sem depender de nada externo.

O arquivo gerado bate **campo a campo** com o gabarito da biblioteca
(`microservices/Java-Efd-Icms/src/test/resources/efd.txt`):

```
|0000|020|0|01012026|31012026|NOME|99999999999999||GO|999999999|9999999|||A|1|
```

E os contadores conferem:

| Registro | Gerado | Regra |
|---|---|---|
| `0990` | 6 | linhas do bloco 0 |
| `9990` | 13 | linhas do bloco 9, **incluindo o próprio 9990** |
| `9999` | 19 | linhas do arquivo inteiro |

O `9900` declara quantas linhas de cada registro existem, e a SEFAZ recusa o
arquivo sem apontar a linha se a contagem divergir. Por isso o serviço **delega
a contagem para a lib** e não reconta nada.

### Um bug meu, corrigido

A primeira versão acrescentava o bloco 0 à mão com `registro.toString()` e
*depois* chamava a lib. Saíam **duas linhas de cada registro** — a certa, e
`br.com...Registro0000@df63e539`, que é o `toString()` padrão do objeto. E o
contador 9900 dobrava junto. Agora o método delega tudo.

### O que falta no SPED

Ler os dados fiscais do ERP e montar o EFD a partir deles. Hoje o cabeçalho vem
do corpo da requisição, que é o que permite validar o formato contra o
gabarito. E falta a tabela dos **registros**: o `bc_fis_sped_fiscal` atual tem 4
campos (`competencia`, `arquivo_url`, `status`, `gerado_at`) e é um *registro
de arquivo gerado*, não as linhas do EFD, que são milhares por arquivo.

O `java-efd-contribuicoes` está no `pom.xml` e resolve, mas **nenhum endpoint
foi escrito** para ele.

---

## 3. MDF-e — a forma

**Modelo 58.** Duas posições na chave, sempre `58`.

**Chave de acesso, 44 dígitos:**

```
cUF(2) AAMM(4) CNPJ(14) mod(2) serie(3) nMDFe(9) tpEmis(1) cMDFe(8) cDV(1)
```

`serie` tem **3** posições no MDF-e, ao contrário da NFe que usa 2. E `cMDFe` são
8 dígitos **aleatórios**, não contador: número sequencial na chave vaza volume
de emissão.

**Duas chamadas, não uma.** A resposta do `MDFeRecepcaoSinc` traz só
`infRec/nRec` — sem chave, sem protocolo. A autorização vem na segunda
chamada, a `MDFeRetRecepcao`, passando o recibo. Por isso o contrato tem
`recibo` e existe o endpoint `/recibo`. Escrever `getChNFe()` ali é NPE: o
método não existe no `InfRec` da swconsultoria, e o hábito de NFe e NFS-e leva
direto à armadilha.

**A Chave Natural** (UF + CNPJ + série + número + modelo + forma de emissão) é
**rejeitada se repetir**. É o mesmo risco que os três estados de `confirmado`
resolveram na NFS-e, e a solução é a mesma.

**Regra do CIOT — produção em 23/11/2026.** A NT 2026.001 (maio/2026) torna o
grupo `infCIOT` obrigatório quando `modal=1` e (`tpEmit=1` ou `tpEmit=3` ou
`tpEmit=2` com `tpTransp`). Rejeição **cStat 684**. Homologação foi
21/09/2026. O `infCIOT` **já existe** no XSD: muda a validação, não o layout.

**A SVRS autoriza o país inteiro.** O `MDFAutorizador3` tem um valor só no enum
(`RS`) e o fallback é silencioso — qualquer UF cai em `RS`. Isso é o
comportamento certo, não um bug: emitente de SP emite pelo SVRS. **Não existe
"URL de MDF-e de SP" para escolher.**

**Ambiente fixo na lib.** A `DFConfig` da 5.1.2 devolve `HOMOLOGACAO` e não tem
setter — o bytecode é um `getstatic` apontando para `HOMOLOGACAO`. A config
sobrescreve o método. Efeito colateral bom: a lib fica sempre em homologação
por padrão, e produção passa a ser decisão consciente.

---

## 4. O caminho até a SVRS, e onde para

Esta seção é a mais longa porque tem **quatro camadas**, e cada uma barreou
antes da seguinte. A ordem importa: cada camada foi resolvida porque a anterior
estava resolvida.

### Camada 1 — a CA da SVRS não estava no sistema — RESOLVIDA

O primeiro erro era:

```
(certificate_unknown) PKIX path building failed:
SunCertPathBuilderException: unable to find valid certification path
```

O que o servidor de homologação da SVRS apresenta:

```
subject  CN=*.svrs.rs.gov.br          (PROCERGS, serial 87124582000104)
issuer   CN=Autoridade Certificadora do SERPRO SSLv1
         OU=Autoridade Certificadora Raiz Brasileira v10
```

Duas fontes, nenhuma com a cadeia certa:

1. O `homologacao.cacerts` da fincatto tem 68 entradas e **todas de NFC-e** —
   alias começam com `hnfe.`, `hnfe.fazenda.mg.gov.br`.
2. O truststore do SO tinha **123 CAs e nenhuma da ICP-Brasil**. Procurei por
   "Raiz Brasileira", "ICP-Brasil" e "SERPRO": zero nos três.

E o portal da SVRS não publica a CA: `dfe-portal.svrs.rs.gov.br/mdfe/Documentos`
responde 200, mas não tem `.zip`, `.pfx`, `.cacerts`, `.cer`, `.pem` nem `.jks`.

**Resolvido com o `icp.sh`** (o script do usuário, salvo em `~/Documentos`),
que baixa as ~180 ACs credenciadas de `acraiz.icpbrasil.gov.br`. O bundle do
sistema foi de **123 para 303** e a cadeia passou a validar:

```
depth=2 CN=Autoridade Certificadora Raiz Brasileira v10
depth=1 CN=Autoridade Certificadora do SERPRO SSLv1
depth=0 CN=*.svrs.rs.gov.br
Verification: OK
      Verify return code: 0 (ok)
```

Automatizado no `installbase.sh`, função `install_certs_icp_brasil()`. Ver 4.1.

### Camada 2 — o ERP não usa o truststore do sistema — RESOLVIDA

Resolver a camada 1 **não bastou**. O ERP é Java, e a fincatto não lê o
truststore do SO: ela lê um JKS que o ERP aponta em `BRASIL_SAAS_MDFE_CADEIA`.
Sem o JKS, o PKIX volta com a mensagem igual, e parece que a instalação das CAs
não funcionou.

Por isso o bloco do `installbase.sh` faz **duas** coisas: instala as CAs no
sistema e monta o JKS. A segunda não é redundante.

### Camada 3 — o truststore ficou ilegível para o ERP — RESOLVIDA

Erro meu, do tipo que só aparece na implantação:

```
Falha ao consultar o status: nao foi possivel abrir
/etc/brasil_saas/certs/truststore-sefaz.jks (JKS): Permissão negada
```

Eu havia feito `chmod 600` no JKS, e `/etc/brasil_saas` a 700. O ERP **não roda
como root**, então não abria o próprio truststore. E pior: a pasta do truststore
está *dentro* de `/etc/brasil_saas`, então 700 na de cima impedia mesmo de
atravessar até o arquivo.

A correção separa o que é público do que é segredo:

| Arquivo | Modo | Conteúdo |
|---|---|---|
| `/etc/brasil_saas/` | 755 | — |
| `/etc/brasil_saas/certs/` | 755 | — |
| `truststore-sefaz.jks` | **644** | só CA **pública**: nem chave nem senha |
| `sefaz.env` | **644** | caminho e UF, **sem segredo** |
| `cert.env` | **600** | senha do A1 e do truststore |

O JKS em 644 é o correto: dentro dele não há nada secreto. E `sefaz.env` em 644
porque o ERP precisa ler o **endereço** do truststore, e obrigá-lo a rodar como
root para ler o próprio caminho seria absurdo. O segredo vai em `cert.env`,
separado.

### Camada 4 — a SVRS recusava o certificado — RESOLVIDA

Quando o TLS passou a completar, o servidor respondeu **403**:

```
403 - Forbidden: Access is denied.
You do not have permission to view this directory or page using the
credentials that you supplied.
```

O handshake mostra que a SVRS **pede certificado de cliente** (mutual TLS):
`TLSv1.2 (IN), TLS handshake, Request CERT (13)`. Sem certificado, 403 é o
esperado.

O certificado que a fincatto traz é **fixture de teste unitário**, não
credencial:

```
subject  CN=NFe, O=NFe, L=Florianopolis, ST=SC, C=BR
issuer   CN=NFe, O=NFe, L=Florianopolis, ST=SC, C=BR     <- auto-assinado
notAfter Jun  1 01:59:32 2019 GMT                          <- venceu em 2019
```

Auto-assinado e vencido há sete anos. Os outros do `microservices` também não
servem: o do sped-mdfe é auto-assinado, e o do TRT2 é AC de teste de outro
órgão, fora do caminho de confiança da SVRS.

**A solução foi o A1 que o ERP já usava.** Eu supus que não serviria — o nome
do arquivo sugere certificado da prefeitura de SP, e "certificado da
prefeitura" na minha cabeça era certificado municipal. **Estava errado.** O
certificado é:

```
subject  C=BR, O=ICP-Brasil, ST=SP, L=SAO PAULO,
         OU=Secretaria da Receita Federal do Brasil - RFB,
         OU=RFB e-CNPJ A1, OU=015792860
issuer   C=BR, O=ICP-Brasil, OU=RFB, CN=AC SAFEWEB RFB v5
validade notBefore Mar  9 17:56:37 2026   notAfter Mar  9 17:56:37 2027
uso         TLS Web Client Authentication, E-mail Protection
```

É **e-CNPJ A1 da ICP-Brasil**, com `TLS Web Client Authentication` — que é
exatamente o que mutual TLS exige. E a SVRS aceita e-CNPJ em homologação: o
mesmo A1 que assina a NFS-e da prefeitura de SP assina o MDF-e na SVRS. Não
são certificados diferentes, são usos diferentes do mesmo.

Registrado em `/etc/brasil_saas/cert.env` (600), ao lado do caminho do
truststore.

**Erro meu de raciocínio, e o padrão dele:** assumi que "certificado da
prefeitura" significava certificado municipal, e não abri para ler. Já tinha
aprendido a mesma coisa com o `.wsdl` do MDF-e e com o `nfe/`, e fiz de novo.
*Supor que um arquivo não serve é tão caro quanto supor que serve.*

### O que eu não fiz, de propósito

**Não desliguei a validação de TLS**, em nenhuma hipótese. Desligar é o jeito
rápido de fazer o teste passar, e é o jeito rápido de emitir MDF-e com um
certificado que não é seu, num ambiente que não é de produção. Se a SEFAZ
apresentar certificado fora da cadeia, o ERP **precisa** falhar: é assim que se
pega certificado vencido ou do ambiente errado. **Não há `TrustAllCerts` em
nenhuma linha deste código.**

### 4.1 O bloco do `installbase.sh`

`install_certs_icp_brasil()`, chamada junto com as demais no fim do script.
Idempotente: se o bundle já tiver 250+ certificados, pula o download.

```
/etc/ssl/certs/ca-certificates.crt: 123 -> 303 entradas
/etc/brasil_saas/certs/truststore-sefaz.jks: 303 CAs, 466 KB, 644
/etc/brasil_saas/sefaz.env: 644
/etc/brasil_saas/cert.env: 600
```

**Cinco bugs meus neste bloco, todos achados rodando, não lendo:**

1. **`grep "Raiz Brasileira"` no bundle** para checar se já está instalado. O
   `ca-certificates.crt` é PEM com o corpo em base64: o assunto do certificado
   **não aparece como texto legível**, então o `grep` nunca casa nem com as CAs
   instaladas. O bloco sempre baixava 20 MB de novo e ainda achava que era a
   primeira vez. Agora conta os certificados e compara com 250.
2. **`TMP` criado dentro do `else`.** A parte 2 fatia o bundle em `TMP`, e
   quando a parte 1 era pulada — justamente o caso de máquina já preparada — o
   `TMP` não existia e o `awk` falhava com *"cannot open ... for output"*.
3. **`awk` não cria diretório.** `print > arq` abre o arquivo de saída, não o
   diretório. Faltou `mkdir -p` na pasta de fatias.
4. **`JKS_TMP="$(mktemp)"`.** `mktemp` cria o arquivo, e arquivo de zero byte
   não é keystore: o `keytool` responde *"Keystore file exists, but is empty"*
   e recusa. As 303 importações falharam e o bloco terminou com *"so 0 CAs"* sem
   dizer por quê. O path tem de estar **livre**, não existir.
5. **`rm -rf "$TMP"` antes do `mv` do JKS.** O keystore estava dentro de `TMP`,
   então o `mv` falhava com *"Arquivo ou diretório inexistente"* — e o bloco
   ainda anunciava *"truststore do ERP: 303 CAs"*, que era verdade e
   irrelevante.

Os cinco são o mesmo padrão: **o script anunciava sucesso e o arquivo não
existia.** Anunciar contagem sem verificar o resultado é o que faz um script de
instalação passar no console e falhar em produção.

---

## 5. CT-e — o mesmo caminho do MDF-e

Documento de transporte terrestre, mesma autorizadora nacional, mesmo
certificado A1 com mutual TLS, mesmo fluxo de recibo e protocolo. A diferença
de layout está na lib (`cte400`, 330 arquivos).

Por isso **uma só config** para os dois: `MDFeConfig` e `CTeConfig` herdam do
mesmo `DFConfig`, e os 5 métodos abstratos são idênticos. São as mesmas 5
coisas. Duas classes seriam o mesmo código duas vezes, e a segunda ia divergir
quando a primeira mudasse. Não dá para estender as duas — são hierarquias
irmãs — então `ConfigCertificadoDocumentoFiscal` implementa os 5 e expõe
`comoMDFe()` e `comoCTe()`.

**Uma diferença de API que quebrou o build:** o `consultaStatus` do CT-e
**exige a UF**; o do MDF-e tem sobrecarga sem argumento. Sem a UF o compilador
reclama de *"actual and formal argument lists differ in length"*, e em runtime
o endpoint do CT-e responde 404 para a assinatura errada.

O contrato é o mesmo registro (`contrato_mdfe`, com os mesmos três estados de
`sucesso`). Não é cópia e cola por preguiça: é o mesmo formato, porque os dois
documentos compartilham autorizadora, certificado e fluxo.

---

## 6. O que a tabela precisa ter

`bc_fis_mdfe` existe com 0 linhas e 6 campos: `numero`, `serie`, `chave_acesso`,
`uf_inicio`, `uf_fim`, `xml`. Faltam, para um MDF-e real:

```
uf_carregamento          municipio_carregamento   municipio_descarregamento
ciot                     recibo                   protocolo
modal                    tp_emit                  tp_transp
```

E o `xml` em `text` no Postgres contraria a decisão já tomada no projeto:
documento vai no Mongo, o Postgres guarda a referência.

Nada disso foi feito — a tabela está como estava.

---

## 7. O que NÃO foi verificado

- **Nenhum MDF-e nem CT-e foi emitido.** O `status` responde `cStat 107`, o
  que prova que a cadeia TLS, o A1 e o envelope SOAP estão certos. **Não
  prova que o XML do documento esteja.** `envioRecepcaoSinc`, `encerramento`,
  `incluirCondutor`, `incluirDFe` e `pagamentoTransporte` existem na lib e
  **ainda não foram chamados nenhuma vez**.
- **A montagem do MDF-e não foi implementada.** Só o `status` está escrito. O
  `MdfeEmissaoService` tem `enviar` e `consultarRecibo` no papel; falta o
  objeto `MDFe` com emitente, veículo, motorista, LAC, município de
  carregamento e descarregamento, e o `CIOT`.
- **Não sei se o CNPJ está autorizado para MDF-e.** O `cStat 107` é do
  serviço, não do emitente. Se o CNPJ não estiver habilitado para MDF-e, a
  emissão é recusada, e isso **só aparece no primeiro envio**.
- **O EFD Contribuições não foi exercitado.** A lib está no `pom.xml` e
  resolve, mas nenhum endpoint foi escrito para ela.
- **As telas não foram vistas no navegador.** Não havia navegador conectado.
- **Produção nunca foi testada.** A `DFConfig` da 5.1.2 devolve `HOMOLOGACAO`
  fixo; a config sobrescreve, e sobrescrever não é o mesmo que funcionar em
  produção. Mudar `BRASIL_SAAS_MDFE_AMBIENTE=PRODUCAO` e rodar contra
  `mdfe.svrs.rs.gov.br` é o teste que falta.

## 8. Erros meus, para não repetir

### Da fase de codificação

1. **`MDFeConfigFincatto` vs `MdfeConfigFincatto`** — D maiúsculo no tipo de
   retorno, minúsculo na classe. O compilador acusou *"cannot find symbol"*
   numa classe do **mesmo pacote**, que é a mensagem mais confusa possível.
   Conferi com `javac` isolado, que compilava os dois, e o `-X` do Maven, que
   mostrava "Compiling 620 source files" — ou seja, o arquivo **estava** sendo
   compilado. A causa era a letra.
2. **`Certificado.getTipo()`** — escrevi de memória e não conferi. Só descobri
   porque o `/status` caiu com `NoSuchMethodError`.
3. **`InfRec.getChNFe()`** — não existe. O MDF-e síncrono devolve só o recibo.
   Escrevi por hábito de NFe.
4. **`setAmbiente()`** — não existe na 5.1.2. O ambiente é `getstatic` fixo em
   `HOMOLOGACAO` e precisa ser sobrescrito.
5. **`setTimeoutRequisicaoEmillis()`** — não existe, e o retorno é `int` com
   nome diferente.

Todos os cinco só apareceram compilando e rodando. **Ler a assinatura com
`javap` antes de escrever** teria evitado os cinco.

### Do `installbase.sh`

Os cinco bugs da seção 4.1, mais um sexto, este do ambiente e não do script:

6. **OneDrive gravou um documento vazio no git.** O arquivo
   `APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md` foi commitado com 12.651 bytes de
   18.134: o OneDrive ainda não tinha sincronizado quando o `git add` leu o
   arquivo, e o git versionou o que estava no disco naquele instante. Pior: o
   arquivo ficou **instável** — `ls` mostrava 18.134 bytes e `test -f` dizia
   "não existe", alternando, e as edições foram se perdendo. **Antes de
   commitar, conferir que o tamanho em disco bate com o esperado.** Um commit
   com arquivo pela metade é pior que não commitar, porque parece commitado.

### De raciocínio

7. **Supus que o A1 do ERP não serviria.** O nome do arquivo diz
   `EURIPEDES BATISTA DE PAIVA JUNIOR TECNOLOGIA DA I_00000000000191.pfx` e eu
   li "certificado da prefeitura de SP" como "certificado municipal", e
   concluí que uma AC municipal não passaria na SVRS. **Não abri para ler.** É
   `e-CNPJ A1` da ICP-Brasil, `AC SAFEWEB RFB v5`, e passou de primeira.

Este é o **terceiro** erro do mesmo tipo nesta sessão, e o que mais me custa: o
`.wsdl` do MDF-e que eu declarei inexistente, o `nfe/` que quase descartei, e
agora o A1. Nos três eu tirei a conclusão a partir de **nome** — do nome do
arquivo, do nome da pasta — em vez de **ler a coisa**.

O `.wsdl` custou uma frase falsa no documento. O `nfe/` quase custou a escolha
da biblioteca errada. O A1 custou uma conclusão errada sobre o seu próprio
certificado, e quase um pedido de certificado novo. **Nome não é conteúdo, e
pasta não é fonte.**

## Arquivos

```
pom.xml                                        as 3 libs + o par httpclient/httpcore pinado
installbase.sh                                 install_certs_icp_brasil()
src/main/java/br/com/brasil_saas/fiscal/
  sped/SpedEfdIcmsService.java                  gera o EFD, delega os contadores
  sped/SpedEfdController.java                   POST /efd/gerar, GET /efd/exemplo
  mdfe/ConfigCertificadoDocumentoFiscal.java    config compartilhada MDF-e e CT-e
  mdfe/MdfeEmissaoService.java                  status e consulta de recibo
  mdfe/MdfeController.java                      /status, /recibo
  mdfe/RespostaMdfePadrao.java                  o contrato, com os três estados
  cte/CteEmissaoService.java                    status do CT-e
  cte/CteController.java                        /status
scripts/montar_cadeia_sefaz.py                  monta o JKS a partir do bundle do SO
```
