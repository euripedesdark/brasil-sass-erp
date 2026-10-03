# O que tem em `microservices/` — 26 pastas, 27 MB de codigo e muito mais

Data: 26/09/2026

Este documento existe porque **nome de pasta nao serve e codigo morto nao e
informacao perdida**. Das 26 pastas, 3 servem para o MDF-e e 23 nao servem como
codigo — mas varias delas tem dentro delas o XSD, o PDF de regra, a tabela ou o
exemplo que e o que realmente resolve o problema. Varias estavam aqui ha meses
e ninguem sabia o que tinha nelas.

Metodo: nao foi pelo nome da pasta. Foi pela contagem de arquivos por tipo em
cada uma, e pela leitura do conteudo dos que interessam. Script em
`/tmp/opencode/inventario_microservices.py`.

---

## Resposta curta: a que serve para MDF-e

Duas candidatas. A segunda é melhor e eu só a vi depois de ter escrito a
primeira como resposta.

### 1. `nfe/` — fincatto `documentofiscal` — **é a melhor**

`nfe/` nao é "material da NFe". É a biblioteca **fincatto
`documentofiscal`**, que implementa NFe, CT-e, **MDF-e**, NFS-e, ECF e EFD na
mesma base. E tem o pacote `mdfe3` completo: **183 arquivos** do modulo.

O `WSFacade` do MDF-e tem 15 metodos publicos, e sao exatamente o MDF-e inteiro:

```java
envioRecepcaoLote / envioRecepcaoSinc / envioRecepcaoSincAssinado
consultaStatus / consultaMdfe / consultaRecibo / consultaNaoEncerrados
cancelaMdfe / cancelaMdfeAssinado
encerramento / encerramentoAssinado
incluirCondutor / incluirDFe / incluirPagamentoTransporte
```

Mais 10 classes de web service (`WSRecepcaoLote`, `WSEncerramento`,
`WSIncluirCondutor`, `WSCancelamento`, `WSPagamentoTransporte`,
`WSNotaConsulta`, `WSConsultaRecibo`, `WSConsultaNaoEncerrados`,
`WSIncluirDFe`, `WSFacade`), a classe `MDFAutorizador3` com as URLs, e
`1.044 XSD` no mesmo repositorio.

Por que e melhor que a `java-mdfe`:

| | fincatto `documentofiscal` | `java-mdfe 3.00.4` |
|---|---|---|
| Idade | ativa | 2019 |
| `java_certificado` exigido | nao (assina com o proprio `XMLSigner`) | 2.2, contra os 3.16 do ERP |
| Conflito de versao | nenhum | **sim, declarado** |
| MDF-e junto com NFe/CT-e | sim, mesma base | so MDF-e |
| URL da SEFAZ | no codigo | nao |

**Ressalva honesta:** `nfe/` esta com `5.1.3-SNAPSHOT` — e snapshot. Nao e
versao de release, e o `pom.xml` dele e `com.github.wmixvideo:nfe`, que e o
fork do autor. Precisa verificar se ha release estavel no Maven Central, ou se
compilando do fonte. Nao fui ao Maven Central checar.

### 2. `br.com.swconsultoria:java-mdfe:3.00.4` — esta no `.m2`

**No `.m2`, ja baixada**, 2,4 MB, 742 classes, schema 3.00. **Nao esta no
`pom.xml`.** E a mesma familia do `java-nfe 4.1.3` que o ERP ja usa.

A pilha de que ela precisa ja esta resolvida, porque o `java-nfe` traz
transitivamente:

```
br.com.swconsultoria:java-nfe:4.1.3
 +- br.com.swconsultoria:java_certificado:3.16
 +- com.sun.xml.bind:jaxb-impl / jaxb-xjc / jaxb-core:4.0.5
 +- org.apache.axis2:axis2-kernel / adb / jaxws:1.7.5
 +- org.apache.axis2:axis2-transport-http / local:1.7.5
```

Falta so `org.ini4j:ini4j` (a lib le a configuracao de `.ini`), que o
`java-mdfe` exige e ninguem no ERP tem.

**Risco de versao, e ele e real:** o `java-mdfe 3.00.4` e de 2019 e pede
`java_certificado 2.2`. O ERP resolve `3.16`. Maven sempre vence a mais nova,
entao o `java-mdfe` vai rodar contra uma API de certificado tres versoes
posterior. Isso precisa ser compilado e testado, nao presumido.

---

## Uma regra que entra em producao em 2 meses

Achei isto na pasta `PL_MDFe_300b_NT012025_1.05`, e e o achado mais urgente
deste documento.

**Nota Tecnica 2026.001 v1.00 (maio de 2026) — "Altera Regras de validacao do
MDFe".**

> Esta NT dispoe sobre regra de validacao do MDFe obrigando o **CIOT** para as
> prestacoes de servico de transporte rodoviario de cargas realizadas por conta
> de terceiros e mediante remuneracao conforme o Ajuste SINIEF n° 03 de 2026.

| Campo | Valor |
|---|---|
| cStat | **684** |
| Mensagem | `Rejeicao: CIOT devera ser informado` |
| Condicao | `modal=1` e (`tpEmit=1` ou `tpEmit=3` ou (`tpEmit=2` com `tpTransp`)) |
| Obrigatoriedade | grupo `infCIOT` |
| **Homologacao** | **21/09/2026** (dentro da semana) |
| **Producao** | **23/11/2026** |

Ou seja: em novembro, quem emitir MDF-e rodoviario como prestador de servico
sem CIOT toma rejeicao. O `infCIOT` **ja existe no XSD local**
(`mdfeModalRodoviario_v3.00.xsd`, campo `CIOT`), entao o layout nao precisa
mudar — e so a regra de Validar que precisa.

---

## A chave de acesso do MDFe

Do MOC Visao Geral 3.00b, secao 2.1.3. Sao 44 digitos, e a montagem e fixa:

```
cUF (2) + AAMM (4) + CNPJ/CPF (14) + mod (2) + serie (3) + nMDFe (9)
       + tpEmis (1) + cMDFe (8) + cDV (1)  = 44
```

- `mod` = **58**, sempre. E o "modelo 58" que o MOC cita na introducao, e o
  valor `D4` na tabela de dominio. Um MDF-e com 55 ou 61 na posicao do modelo
  e rejeitado.
- `serie` = 3 posicoes (no MDFe,ao contrario da NFe, que usa 2).
- `cMDFe` sao 8 digitos **aleatorios**, escolhidos pelo emitente. Nao e
  sequencia. O ERP tem de gerar numero aleatorio de 8 digitos e nao um contador.

### A Chave Natural — o ponto que mais importa

> O Sistema de Autorizacao de Uso do Ambiente Nacional Autorizador das SEFAZ
> valida a existencia de um MDFe previamente autorizado e **rejeita novos
> pedidos de autorizacao para MDFe com duplicidade da Chave Natural**.

Chave Natural = UF + CNPJ/CPF + serie + numero + modelo + forma de emissao.
**Nao repete.**

Isto e exatamente o risco que ja foi resolvido na NFS-e com os tres estados de
`confirmado`: quando a resposta da prefeitura se perde, reemitir na mesma
chave natural e o que duplica a nota. O MDF-e tem o mesmo problema, com a
mesma solucao. Vale a mesma regra de negocio, e a mesma tabela de retornos.

---

## Regras de validacao do MDFe (F01-F18+)

Do MOC Anexo I, secao 2.2. As que mais derrubam emissor:

| Regra | cStat | O que e |
|---|---|---|
| F01 | 252 | ambiente do MDFe diferente do web service |
| F02 | 247 | UF do emitente diferente da UF da chave |
| F03 | 227 | campo `ID` invalido — falta a literal `MDFe`, ou a chave nao bate com a concatenacao |
| F04 | 666 | ano da chave menor que 2012 |
| F05 | 253 | digito verificador da chave invalido |
| F06 | 579 | versao do modal nao suportada |
| F07 | 580 | falha no schema XML |
| F08 | 456 | municipio de carregamento diverge da UF |
| F09 | 405 | municipio de carregamento inexistente (IBGE) |
| F10 | 685 | municipio de carregamento duplicado no MDFe |
| F11 | 612 | municipio de descarregamento diverge da UF |
| F12 | 406 | municipio de descarregamento inexistente |
| F13 | 680 | municipio de descarregamento duplicado |
| F14 | 638 | `tpEmit=1`: grupo de documentos NFe nao pode ser preenchido |
| F15 | 639 | `tpEmit=2`: grupo de documentos CTe nao pode ser preenchido |
| F16 | 540 | `tpEmit=3`: grupo de documentos CTe nao pode ser preenchido |
| F17 | 541 | `tpEmit=3` com operacao interestadual ou exterior |
| F18 | 743 | informed CPF do proprietario exige `tpTransp` preenchido |

As validacoes gerais (grupo D e E) que valem para tudo:

- **D** — validacao do certificado de assinatura
- **E** — validacao da assinatura digital
- **E-1** — assinatura em regime especial NFF
- **E-2** — assinatura PAA

## Valores de dominio (MOC 3.6)

```
D4   58                                   (modelo, fixo)
D5   AC AL AM AP BA CE DF ES GO MA MG MS MT PA PB PE PI PR RJ ...   (UF)
D6   1, 2                                 (tipo de ambiente)
D7   1, 2, 3                              (tipo de emitente)
D8   1, 2, 3, 4, 5, 6, 7                  (modal)
D11  01 a 11                              (forma de emissao)
D12  01, 02
D14  01, 1B, 02, 2D, 2E, 04, 06, ...      (tipo de transportador)
D17  01, 02, 03, 99
D20  01 a 06
D21  00 a 05
```

`D8` e o modal: 1 rodoviario, 2 aereo, 3 ferroviario, 4 aquaviario. O MOC traz
o leiaute completo dos quatro (secoes 3.1 a 3.4), mais **expressoes regulares**
(secao 3.5), que sao as que a SEFAZ usa para reprovar.

---

## Inventario das 26 pastas

Legenda: **serve** = da para usar; **schema** = o XSD e a lei, vale mais que
qualquer biblioteca; **doc** = regra em PDF; **dado** = tabela ou exemplo.

| Pasta | Tam | Codigo | Serve? | O que tem de valor |
|---|---|---|---|---|
| **java-mdfe** (no `.m2`) | 2,4M | java | **SIM** | A lib de MDF-e completa, schema 3.00. Ver ressalva de versao acima |
| **PL_MDFe_300b_NT012025_1.05** | 7,7M | — | **schema + doc** | 41 XSD do MDFe 3.00 + **6 PDFs**: o MOC de leiaute e regras, o MOC visao geral, e 4 Notas Tecnicas (2022, 2025, 2026, e a de CNPJ alfanumerico) |
| **sped-mdfe** | 3,8M | php | nao | **PHP** (nfephp). Mas tem 88 XSD, 35 XML de exemplo e 1 `.pfx` |
| **Java_CTe** | 4,7M | java 220 | **schema + wsdl** | **7 WSDL** — a unica pasta com URL real de servico SEFAZ. CT-e e o documento mais proximo do MDF-e: mesmo formato (transporte, assinado, autorizado) |
| **Java_NFe** | 9,1M | java 367 | schema + doc | 126 XSD, 17 docs, XMLs de evento de cancelamento e inutilizacao, 1 `.pfx` |
| **nfe** | 32M | java 2178 | **SIM** | **fincatto `documentofiscal`**: NFe + CT-e + **MDF-e** + NFS-e + ECF + EFD na mesma base. 1.044 XSD, **183 arquivos** do modulo MDF-e, `MDFAutorizador3` com as URLs da SVRS, `homologacao.pfx` e XMLs de exemplo |
| **Java_Certificado** | 1,3M | java 12 | doc + cert | Docs de **A1 e A3**, e 2 `.pfx` marcados `NaoUsar_*` |
| **Java-Efd-Icms** | 2,9M | java 600 | nao | 600 classes de EFD ICMS. Nada de MDF-e |
| **Java-Efd-Contribuicoes** | 2,6M | java 457 | nao | 457 classes de EFD contribuicoes |
| **esocial** | 54M | java 5073 | wsdl | **4 WSDL** de eSocial. XSD 67. Referencia de como um modulo grande de imposto se organiza |
| **l10n-brazil** | 192M | py 773 | **dado** | As tabelas fiscais em CSV: cfop 619, cst 160, cest 1.043, ncm 11.926, tax.classification 163, tax.group 28. E o OCA inteiro |
| **nfse** | 2,1M | java 168 | schema | 10 XSD do padrao nacional de NFS-e (DPS v1.01, NFSe v1.01) |
| **NFSe-SaoPaulo-SP** | 5,2M | — | schema + doc | 18 XSD da prefeitura de SP, o manual `NFe_Web_Service-v3.3.8.pdf`, e as planilhas de atribuicao/alteracao de codigos. **Em producao** |
| **nfse-sp-api** | 24M | java 15 | **em producao** | A API Java que esta no ar na 4567 |
| **nfse-sp-bridge** | 104K | ruby 1 | desligado | O emissor Ruby. `contrato_nfse` escrito, **nunca executado** |
| **nfse_prefeitura_sp** | 136K | ruby 23 | nao | 23 scripts Ruby contra a prefeitura. A base do emissor |
| **nfse-watchdog** | 12K | sh 1 | desligado | O watchdog que derrubou a API Java 3 vezes. **nao religar como estava** |
| **nfse-failover** | 16K | ruby 1 | nao | `testar_failover.rb`. Falha no codigo antigo, passa no novo |
| **boleto-cnab-api** | 116K | rb, py | nao | API de remessa CNAB. `openapi.json` documentado |
| **BancosBrasileiros** | 17M | js, py, php | nao | Codigo de banco, `.pt-br` |
| **EchoAvatar** | 13M | py 92 | nao | Agente de voz (ElevenLabs/Pipecat). Sem relacao com fiscal |
| **spring-ai** | 180M | java 2278 | nao | O framework Spring AI. Sem relacao com fiscal |
| **Java_Pdf_Signature** | 7,0M | java 12 | nao | Assinatura de PDF. Serve se um dia precisar carimbar o DAMDFE |
| **Java_MDFe** | 12K | **nenhum** | nao | **So um README com link de Discord.** Zero codigo. E o que da a impressao de que MDF-e esta tratado |

---

## As tres pastas que parecem MDF-e e nao servem

Esto e o que mais engana olhando a pasta de cima:

- **`Java_MDFe`** — 12 KB, **três arquivos**: `.gitignore`, `LICENSE`,
  `docs/modulos/README.md`. O README tem duas linhas e um link de Discord. Nenhum codigo.
- **`sped-mdfe`** — 3,8 MB de PHP (`composer.json`, `phpunit.xml.dist`,
  `bootstrap.php`). O ERP e Java. Nao roda aqui, e nao vai rodar sem um
  interpretador PHP em producao, o que seria um segundo stack de runtime
  inteiro por causa de um manifesto.
- **`l10n_br_mdfe_spec.json`** — o nome parece spec do MDF-e e nao e. O
  arquivo esta em `l10n-brazil/.oca/oca-port/blacklist/`, tem uma unica chave
  (`pull_requests`) e nada de MDF-e. Era lista de bloqueio de PR do OCA.

Duas delas tem XSD e PDF dentro, e por isso continuam valendo. A terceira e
lixo com nome convincente.

---

## A lacuna que eu declarei e que **nao era lacuna**

Eu escrevi aqui que "não existe WSDL de MDF-e em pasta nenhuma" e que "a URL do
MDF-e não está no repositório". **As duas frases estão erradas.**

O que eu fiz foi procurar arquivo com extensão `.wsdl` e não achei. Não procurei
a URL dentro do código. Ela está lá:

`nfe/src/main/java/com/fincatto/documentofiscal/mdfe3/classes/MDFAutorizador3.java`

```java
RS {
    getMDFeRecepcao(ambiente) ->
        HOMOLOGACAO ? "https://mdfe-homologacao.svrs.rs.gov.br/ws/MDFerecepcao/MDFeRecepcao.asmx"
                    : "https://mdfe.svrs.rs.gov.br/ws/MDFerecepcao/MDFeRecepcao.asmx"
    getMDFeRecepcaoSinc(...)   MDFeRecepcaoSinc.asmx
    getMDFeRetornoRecepcao(...) MDFeRetRecepcao.asmx
    getMDFeRecepcaoEvento(...)  MDFeRecepcaoEvento.asmx
    getMDFeStatusServico(...)   MDFeStatusServico.asmx
    getMDFeConsulta(...)        MDFeConsulta.asmx
    getMDFeConsNaoEnc(...)      MDFeConsNaoEnc.asmx
    getMDFeDistribuicao(...)    MDFeDistribuicaoDFe.asmx
}
```

São 8 serviços, com URL de homologação e de produção, o que é a peça que eu
disse que só se acha no portal da SEFAZ. **A diferença entre a minha conclusão e
a verdade foi que eu procurei por nome de arquivo em vez de ler o conteúdo.**
Buscar por extensão não acha string embutida no código.

### A ressalva que continua valendo

O enum tem **um único valor, `RS`** (Rio Grande do Sul, o Virtual Ambiente
Nacional). E o fallback é silencioso:

```java
public static MDFAutorizador3 valueOfCodigoUF(final DFUnidadeFederativa uf) {
    for (final MDFAutorizador3 autorizador : MDFAutorizador3.values()) {
        if (Arrays.asList(autorizador.getUFs()).contains(uf)) return autorizador;
    }
    return RS;          // <-- qualquer UF que nao esteja no enum cai aqui
    //  throw new IllegalStateException(...)   <-- commented out
}
```

Para **SP** — que e a empresa do ERP — o enum **nao tem entrada** e o código cai
em `RS`. Isso funciona, porque o MDF-e e nacional e a SVRS autoriza para todo o
pais, mas é preciso saber: **a empresa emite pelo Virtual Ambiente Nacional
(SVRS), nao pelo portal de SP.** Nao ha uma segunda URL para escolher. E o
`throw` esta comentado de propósito pelos autores da lib, o que significa que
foi uma decisão consciente e não um esquecimento.

O cabeçalho do arquivo aponta a fonte oficial por estado:
`https://www.fazenda.sp.gov.br/mdfe/url_webservices/url_webservices.htm`

---

## O que MDF-e tem no ERP hoje

| Camada | Estado |
|---|---|
| Tabela `bc_fis_mdfe` | **existe, 0 linhas** |
| `Mdfe.java` | existe — `numero`, `serie`, `chaveAcesso`, `dataEmissao`, `status`, `ufInicio`, `ufFim`, `xml` |
| `MdfeRepository` | existe — um metodo so: `findByEmpresaIdAndChaveAcesso` |
| Controller | **nao existe** |
| Service | **nao existe** |
| Tela | **nao existe** |
| Certificado para SEFAZ | **nao existe** — o que ha e o da NFS-e, que e outro certificado |
| Lib no `pom.xml` | **nao esta** |

`status` tem default `'DIGITADA'`, o que indica que o desenho original era de
lancamento manual, sem transmissao. Nenhum dos dois e o que a SEFAZ exige.

### NFe tambem nao emite — e isso muda o que da para prometer

`NFeServiceImpl` tem **89 linhas e tres TODOs**, apontando para
`br.com.swconsultoria.nfe.Nfe`:

```java
// TODO: montar TEnviNFe a partir do pedido e chamar br.com.swconsultoria.nfe.Nfe.montaNfe/env
// TODO: montar evento de cancelamento e chamar br.com.swconsultoria.nfe.Nfe.cancelarNfe
// TODO: chamar br.com.swconsultoria.nfe.Nfe.consultaXml
```

A interface `NFeService` promete `emitirNFe`, `cancelarNFe` e
`consultarSituacao`. **Nenhuma das tres funciona.** Isso ja estava assim antes
desta sessao e e a razao de ter saido um `NFeService` com 89 linhas: a interface
foi escrita e a implementacao nao.

Consequencia para o MDF-e: **nao existe infraestrutura de SEFAZ nenhuma no
ERP.** A NFS-e funciona porque a prefeitura de SP e um SOAP simples, com
certificado e um XSD, e a `nfse-sp-api` faz isso. MDF-e e SEFAZ, que e Axis2,
autorizacao com protocolo, consulta por lote, e evento de encerramento e
encerramento de manifesto. Sao coisas diferentes.

---

## Ordem sugerida, se o MDF-e for para a frente

1. **Decidir a lib.** `nfe/` (fincatto `documentofiscal`) e a melhor das duas,
   e o unico jeito de saber e compilar: ele esta em `5.1.3-SNAPSHOT`, e ha
   versao de release no Maven Central ou nao. Isso e um `curl` e decide o resto.
   A `java-mdfe 3.00.4` do `.m2` e a segunda opcao, com o risco de
   `java_certificado` declarado.
2. **Certificado A1 de SEFAZ.** O que ha e o da NFS-e, que e outro certificado.
   `Java_Certificado/` tem a documentacao de A1 e A3 e 2 `.pfx` de teste
   (marcados `NaoUsar_*`).
3. **Emissao pelo Virtual Ambiente Nacional (SVRS).** Confirmado no codigo: a
   `MDFAutorizador3` so tem `RS` e cai em `RS` para qualquer UF. Nao ha URL de SP
   a escolher. `MDFAutorizador3.java:22-116`.
4. **Municipio e UF de carregamento e descarregamento** (F08-F13 rejeitam).
   Precisa do codigo IBGE. O ERP tem a tabela de municipios?
5. **A regra do CIOT** entrar no validador antes de novembro, mesmo que a
   emissao comece depois. cStat 684.
6. **Controle de duplicidade pela Chave Natural**, reusando o desenho dos tres
   estados de `confirmado` que ja foi feito para NFS-e. Nao repetir o
   esquecimento de chave natural que ja custou notas orfas.

## O que NAO foi verificado

*Atualizado em 26/09/2026, depois que o MDF-e passou a responder.*

- **`nfe/` foi testada em execução, e é a melhor das duas opções.** É a
  fincatto `documentofiscal` 5.1.2, e com ela o ERP conversa com a SEFAZ:
  `GET /api/fiscal/mdfe/status` e `GET /api/fiscal/cte/status` respondem
  **`cStat 107`**. Em 1,49 s, com o A1 do próprio ERP.
- **A `java-mdfe 3.00.4` foi descartada por incompatibilidade, não por
  suposição.** Ela chama `Certificado.getTipo()`, que não existe no
  `java_certificado 3.16` que o ERP já usava, e dá `NoSuchMethodError` em
  runtime. **Não há flag de Maven que resolva:** as duas libs precisam de APIs
  diferentes da mesma classe, e só uma pode estar no classpath. O risco que
  eu declarei aqui se confirmou.
- **As URLs da SEFAZ não só estão no repositório: funcionam.** O `cStat 107`
  vem do servidor real, com o handshake TLS completo e o certificado do cliente
  apresentado.
- **Nenhum MDF-e nem CT-e foi emitido.** O `status` prova a cadeia, o A1 e o
  envelope SOAP. **Não prova o XML do documento.** A montagem do MDF-e não foi
  implementada.
- **As outras 22 pastas continuam sem execução.** O inventário delas é por
  leitura de arquivo. Vale para `esocial`, `l10n-brazil`, `Java_NFe`,
  `Java_Efd-Icms`, `Java-Efd-Contribuicoes` e todas as demais.
- **Produção nunca foi testada.** A lib devolve `HOMOLOGACAO` fixo e a config
  sobrescreve; sobrescrever não é funcionar.

## Arquivos citados

```
microservices/PL_MDFe_300b_NT012025_1.05/   41 XSD + 6 PDF   <- o mais importante
microservices/Java_CTe/wsdl/                7 WSDL          <- o padrao de URL
microservices/nfe/                          1.044 XSD + XML de exemplo + .pfx
microservices/sped-mdfe/schemes/            88 XSD
microservices/Java_Certificado/             doc de A1 e A3
~/.m2/repository/br/com/swconsultoria/java-mdfe/3.00.4/   <- a lib
```
