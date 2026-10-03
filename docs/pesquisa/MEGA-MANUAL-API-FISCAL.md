# MEGA MANUAL — como montar a API de MDF-e (e o resto do fiscal)

Data de fechamento: 26/09/2026
Escopo: tudo que foi verificado nesta sessao, mais o que precisa ser feito.
Base: 23 pastas em `src/main/resources/microservices/`, o repositório Maven
local, o banco de dados, e o código do ERP.

> Este documento **nao mexe** em `microservices/`. A pasta fica como esta. O
> manual é para consultar e para reconstruir o que for preciso.

---

# PARTE 1 — Onde as coisas estão

## 1.1 As 23 pastas, com a origem de cada uma

Só 3 das 23 mantiveram o `.git` com remote. As outras 20 perderam o histórico,
e a origem foi recuperada do `<scm>` do `pom.xml`, do `composer.json` /
`package.json`, ou dos badges do `README`. Onde não houve fonte confiável, está
escrito — não chutei URL, porque mandar alguém buscar o projeto errado é pior
que não ter resposta.

| Pasta | Origem | Confiança | Commit | Serve para MDF-e |
|---|---|---|---|---|
| `nfe` | `github.com/wmixvideo/nfe` | pom `<scm>` | — | **SIM — a melhor opção** |
| `PL_MDFe_300b_NT012025_1.05` | SEFAZ-SP, Pacote de Liberação oficial | oficial | — | **schema + regra** |
| `Java_Certificado` | `github.com/Samuel-Oliveira/Java_Certificado` | pom `<scm>` | — | **docs de A1/A3** |
| `sped-mdfe` | `github.com/nfephp-org/sped-mdfe` | git remote | `0831983` | XSD e exemplos (PHP) |
| `NFSe-SaoPaulo-SP` | SEFAZ-SP, documentos oficiais | oficial | — | — |
| `Java_CTe` | `github.com/Samuel-Oliveira/Java_CTe` | pom `<scm>` | — | WSDL e padrão |
| `Java_NFe` | `github.com/Samuel-Oliveira/Java_NFe` | pom `<scm>` | — | XSD e exemplos |
| `Java_MDFe` | `github.com/Samuel-Oliveira/Java_MDFe` | pom da lib no `.m2` | — | **vazia** (só README) |
| `Java-Efd-Icms` | `github.com/Samuel-Oliveira/Java-Efd-Icms` | pom `<scm>` | — | não |
| `Java-Efd-Contribuicoes` | `github.com/Samuel-Oliveira/Java-Efd-Contribuicoes` | pom `<scm>` | — | não |
| `Java_Pdf_Signature` | `github.com/Samuel-Oliveira/Java_Pdf_Signature` | pom `<scm>` | — | talvez (DAMDFE) |
| `l10n-brazil` | `github.com/OCA/l10n-brazil` | git remote | `e16c2f17d5` | tabelas fiscais |
| `esocial` | `github.com/tst-labs/esocial` | README | — | WSDL, padrão |
| `nfse` | `github.com/EduardoKuhn89/nfse` | pom `<scm>` | — | XSD nacional |
| `nfse_prefeitura_sp` | `github.com/infosimples/nfse_prefeitura_sp` | **busca online** | — | base do emissor Ruby |
| `boleto-cnab-api` | `github.com/akretion/boleto_cnab_api` | `BRASIL_SAAS.md` | — | não |
| `BancosBrasileiros` | `github.com/guibranco/BancosBrasileiros` | git remote | `c200d4c` | não |
| `spring-ai` | `github.com/spring-projects/spring-ai` | pom `<scm>` | — | não |
| `EchoAvatar` | `github.com/PantoMatrix/PantoMatrix` | README (fraca) | — | não |
| `nfse-sp-api` | **código do BRASIL-SAAS** | commits do ERP | `e2070939` | em produção |
| `nfse-sp-bridge` | **código do BRASIL-SAAS** | commits do ERP | — | desligado |
| `nfse-watchdog` | **código do BRASIL-SAAS** | commits do ERP | — | desligado |
| `nfse-failover` | **código do BRASIL-SAAS** | commits do ERP | — | desligado |

**As cinco últimas são nossas** — estão no histórico do ERP
(`e2070939`, `746e1f15`, `20179364`). Não têm de onde baixar: se a pasta for
perdida, o código está no git do ERP.

### Onde buscar o que é oficial (não é repositório)

`PL_MDFe_300b_NT012025_1.05` e `NFSe-SaoPaulo-SP` são **Pacotes de Liberação
e documentos oficiais**. Não têm git — e não devem. A nomenclatura é da SEFAZ:
`PL_MDFe` + `300b` (versão do MOC) + `NT012025` (nota técnica) + `1.05` (revisão).

| O quê | Onde |
|---|---|
| MDF-e, schemas e pacotes | `https://dfe-portal.svrs.rs.gov.br/mdfe/Documentos` |
| MDF-e, página do SVRS | `https://dfe-portal.svrs.rs.gov.br/mdfe/` |
| MDF-e, SEFAZ-SP | `https://portal.fazenda.sp.gov.br/servicos/mdfe` |
| MOC do MDFe (Confaz) | `https://www.confaz.fazenda.gov.br/legislacao/arquivo-manuais/manual_mdfe_v3-00.pdf` |
| Pacotes de liberação (SPED) | `http://sped.rfb.gov.br/arquivo/download/1750` |
| NFS-e SP, web service | `https://nfews.prefeitura.sp.gov.br/lotenfe.asmx` |
| NFS-e SP, manual | `NFe_Web_Service-v3.3.8.pdf` na pasta |

## 1.2 A biblioteca que serve, e a que não

### Opção A — fincatto `documentofiscal` (a pasta `nfe/`)

`github.com/wmixvideo/nfe`, **2.178 classes Java**. Não é "material da NFe": é
uma biblioteca que implementa **NFe, CT-e, MDF-e, NFS-e, ECF e EFD** na mesma
base. O módulo MDF-e tem **183 arquivos** e está em
`com.fincatto.documentofiscal.mdfe3`.

`WSFacade` do MDF-e — os 15 métodos públicos, que são o MDF-e inteiro:

```java
envioRecepcaoLote        envioRecepcaoSinc        envioRecepcaoSincAssinado
consultaStatus           consultaMdfe             consultaRecibo
consultaNaoEncerrados    cancelaMdfe              cancelaMdfeAssinado
encerramento             encerramentoAssinado    incluirCondutor
incluirDFe               incluirPagamentoTransporte
```

Mais 10 classes de web service em `mdfe3/webservices/`, a classe
`MDFAutorizador3` com as URLs, e 1.044 XSD no mesmo repositório.

| | fincatto | `java-mdfe 3.00.4` |
|---|---|---|
| Idade | ativa | 2019 |
| Assinatura | próprio `XMLSigner` | `java_certificado 2.2` |
| Conflito com o ERP | nenhum | **3.16 do ERP** |
| MDF-e + NFe + CT-e juntos | sim | só MDF-e |
| URL da SEFAZ no código | sim | não |

**Ressalva:** está em `5.1.3-SNAPSHOT` — é snapshot, e o `pom.xml` declara
`com.github.wmixvideo:nfe`, que é o fork do autor. **Não fui ao Maven Central
verificar se existe release estável.** É um `curl` e decide o resto.

### Opção B — `java-mdfe` (já no `.m2`, fora do `pom.xml`)

```
~/.m2/repository/br/com/swconsultoria/java-mdfe/3.00.4/java-mdfe-3.00.4.jar
```

2,4 MB, 742 classes, schema 3.00. Baixada, **não referenciada no pom**. É a
mesma família do `java-nfe 4.1.3` que o ERP já usa, e a pilha dela já está
resolvida porque o `java-nfe` traz:

```
br.com.swconsultoria:java-nfe:4.1.3
 +- br.com.swconsultoria:java_certificado:3.16
 +- com.sun.xml.bind:jaxb-impl / jaxb-xjc / jaxb-core:4.0.5
 +- org.apache.axis2:axis2-kernel / adb / jaxws:1.7.5
 +- org.apache.axis2:axis2-transport-http / local:1.7.5
```

Falta só `org.ini4j:ini4j:0.5.4`, que ninguém no ERP tem.

**Risco declarado:** a `java-mdfe 3.00.4` é de 2019 e pede `java_certificado
2.2`. O ERP resolve `3.16` — Maven sempre vence a mais nova, então ela roda
contra uma API de certificado três versões posterior. Não presumir: compilar.

## 1.3 As URLs da SEFAZ — estão no código

`nfe/src/main/java/com/fincatto/documentofiscal/mdfe3/classes/MDFAutorizador3.java`
linhas 22–116. São 8 serviços, homologação e produção:

```java
RS {
  getMDFeRecepcao        MDFeRecepcao.asmx
  getMDFeRecepcaoSinc    MDFeRecepcaoSinc.asmx
  getMDFeRetornoRecepcao MDFeRetRecepcao.asmx
  getMDFeRecepcaoEvento  MDFeRecepcaoEvento.asmx
  getMDFeStatusServico   MDFeStatusServico.asmx
  getMDFeConsulta        MDFeConsulta.asmx
  getMDFeConsNaoEnc      MDFeConsNaoEnc.asmx
  getMDFeDistribuicao    MDFeDistribuicaoDFe.asmx
}
// homologação: https://mdfe-homologacao.svrs.rs.gov.br/ws/...
// produção:   https://mdfe.svrs.rs.gov.br/ws/...
```

**O enum só tem `RS`** e o fallback é silencioso:

```java
public static MDFAutorizador3 valueOfCodigoUF(final DFUnidadeFederativa uf) {
    for (final MDFAutorizador3 a : values())
        if (Arrays.asList(a.getUFs()).contains(uf)) return a;
    return RS;    // qualquer UF que não esteja no enum cai aqui
    // throw new IllegalStateException(...)  <- comentado de propósito
}
```

Para SP o enum não tem entrada e cai em `RS`. **Isso funciona e é o
comportamento certo:** o MDF-e é nacional e o SVRS autoriza para todo o país.
O que precisa ficar claro: **a empresa emite pelo Virtual Ambiente Nacional, não
pelo portal de SP.** Não há uma segunda URL para escolher. O `throw`
comentado foi decisão dos autores, não esquecimento.

Fonte oficial por estado, no cabeçalho do próprio arquivo:
`https://www.fazenda.sp.gov.br/mdfe/url_webservices/url_webservices.htm`

> **Correção registrada.** Numa versão anterior deste documento eu escrevi que
> "não existe WSDL de MDF-e e a URL não está no repositório". Estava errado:
> procurei arquivo com extensão `.wsdl` e não li o código. A URL está no
> `.java`, como string. Buscar por extensão não acha string embutida.

---

# PARTE 2 — O MDF-e em si

## 2.1 Modelo 58

O MDF-e é o **modelo 58** no processo NF-e. Está no texto do MOC
("definição do leiaute e regras de validação do MDFe, modelo 58") e no valor de
domínio `D4 = 58`. É 2 dígitos na posição 4 e 5 da chave de acesso. Um MDF-e
com 55 ou 61 ali é rejeitado.

## 2.2 Chave de acesso — 44 dígitos

Do MOC Visão Geral 3.00b, seção 2.1.3:

```
cUF (2) + AAMM (4) + CNPJ/CPF (14) + mod (2) + serie (3) + nMDFe (9)
       + tpEmis (1) + cMDFe (8) + cDV (1)  =  44
```

| Campo | Tam | Nota |
|---|---|---|
| `cUF` | 2 | código da UF do emitente |
| `AAMM` | 4 | ano e mês |
| CNPJ/CPF | 14 | do emitente |
| `mod` | 2 | **sempre 58** |
| `serie` | 3 | **3 posições** — no MDF-e, ao contrário da NFe que usa 2 |
| `nMDFe` | 9 | número do documento |
| `tpEmis` | 1 | forma de emissão |
| `cMDFe` | 8 | **aleatório**, escolhido pelo emitente |
| `cDV` | 1 | dígito verificador (módulo 11) |

**`cMDFe` é aleatório, não é contador.** 8 dígitos. O ERP tem de gerar número
aleatório aí. Se for sequencial, a chave vira previsível e a consulta por chave
passa a dar informação de volume de emissão.

## 2.3 Chave Natural — a que impede duplicidade

> O Sistema de Autorização de Uso do Ambiente Nacional Autorizador das SEFAZ
> valida a existência de um MDF-e previamente autorizado e **rejeita novos
> pedidos de autorização para MDFe com duplicidade da Chave Natural**.

Chave Natural = UF + CNPJ/CPF + série + número + modelo + forma de emissão.
**Não repete.**

Isto é **exatamente** o risco que já foi resolvido na NFS-e com os três estados
de `confirmado`. Quando a resposta da SEFAZ se perde, reemitir na mesma chave
natural duplica o documento. A solução que já existe no ERP serve igual:

- `confirmado = true` → emitido, com protocolo
- `confirmado = false` → a SEFAZ recusou, reemitir é seguro
- `confirmado = null` → não deu para saber → **conferir na SEFAZ antes de reemitir**

A mesma regra, a mesma tabela de retornos (`bc_fis_nfse_retorno`).

## 2.4 Regras de validação (MOC Anexo I, seção 2.2)

As que mais derrubam emissor:

| Regra | cStat | O quê |
|---|---|---|
| F01 | 252 | ambiente do MDFe diferente do web service |
| F02 | 247 | UF do emitente diferente da UF da chave |
| F03 | 227 | campo `ID` inválido — falta a literal `MDFe`, ou a chave não bate com a concatenação |
| F04 | 666 | ano da chave menor que 2012 |
| F05 | 253 | dígito verificador da chave inválido |
| F06 | 579 | versão do modal não suportada |
| F07 | 580 | falha no schema XML |
| F08 | 456 | município de carregamento diverge da UF |
| F09 | 405 | município de carregamento inexistente na tabela IBGE |
| F10 | 685 | município de carregamento duplicado no MDF-e |
| F11 | 612 | município de descarregamento diverge da UF |
| F12 | 406 | município de descarregamento inexistente |
| F13 | 680 | município de descarregamento duplicado |
| F14 | 638 | `tpEmit=1`: grupo de documentos NFe não pode ser preenchido |
| F15 | 639 | `tpEmit=2`: grupo de documentos CTe não pode ser preenchido |
| F16 | 540 | `tpEmit=3`: grupo de documentos CTe não pode ser preenchido |
| F17 | 541 | `tpEmit=3` com operação interestadual ou exterior |
| F18 | 743 | CPF do proprietário informado exige `tpTransp` preenchido |
| **NT 2026.001** | **684** | **CIOT obrigatório** — ver 2.5 |

Validações gerais que valem para tudo (grupo D e E do MOC):

- **D** — validação do certificado de assinatura
- **E** — validação da assinatura digital
- **E-1** — assinatura em regime especial NFF
- **E-2** — assinatura PAA

## 2.5 A regra que entra em produção em 23/11/2026

**Nota Técnica 2026.001 v1.00, maio de 2026 — "Altera Regras de validação do
MDFe".**

> Esta NT dispõe sobre regra de validação do MDFe obrigando o **CIOT** para as
> prestações de serviço de transporte rodoviário de cargas realizadas por conta
> de terceiros e mediante remuneração conforme o Ajuste SINIEF nº 03 de 2026.

| Campo | Valor |
|---|---|
| cStat | **684** |
| Mensagem | `Rejeição: CIOT deverá ser informado` |
| Condição | `modal=1` e (`tpEmit=1` ou `tpEmit=3` ou `tpEmit=2` com `tpTransp`) |
| Obrigatoriedade | grupo `infCIOT` |
| Homologação | 21/09/2026 |
| **Produção** | **23/11/2026** |

O `infCIOT` **já existe** no XSD local:

```
PL_MDFe_300b_NT012025_1.05/mdfeModalRodoviario_v3.00.xsd
  name="infCIOT"  ->  name="CIOT"
```

O layout não muda. **Muda a validação.** Uma linha de código na Library que
valida, e o emissor para de ser rejeitado em novembro.

> O nome da pasta mente: `PL_MDFe_300b_NT012025_1.05` contém
> `MDFe_Nota_Tecnica_2026_001.pdf`, de **maio de 2026**.

## 2.6 Valores de domínio (MOC 3.6)

```
D4   58                                          modelo, fixo
D5   AC AL AM AP BA CE DF ES GO MA MG MS MT PA PB PE PI PR RJ ...   UF
D6   1, 2                                        tipo de ambiente
D7   1, 2, 3                                     tipo de emitente
D8   1, 2, 3, 4, 5, 6, 7                         modal
D11  01 a 11                                     forma de emissão
D12  01, 02
D14  01, 1B, 02, 2D, 2E, 04, 06, 07, 08, 8B...   tipo de transportador
D17  01, 02, 03, 99
D20  01 a 06
D21  00 a 05
```

`D8` é o modal: **1 rodoviário, 2 aéreo, 3 ferroviário, 4 aquaviário**. O MOC
traz o leiaute completo dos quatro (seções 3.1 a 3.4), mais **expressões
regulares** (3.5) — que são as que a SEFAZ usa para reprovar.

## 2.7 Onde está cada coisa, por arquivo

| Preciso de | Está em |
|---|---|
| Regras de validação, cStat, mensagens | `PL_MDFe_.../MOC_MDFe_Anexo I_Leiaute e Regras Validação_v3.00b.pdf` (45p) |
| Chave de acesso, fluxo, visão geral | `PL_MDFe_.../MOC_MDFe_VisaoGeral_v3.00b.pdf` (82p) |
| **Documento impresso, DAMDFE** | `PL_MDFe_.../MOC_MDFe_Anexo II - DAMDFE_v3.00b.docx` (158 parágrafos) |
| Layout do XML | `PL_MDFe_.../*.xsd` (41 arquivos) |
| Regra do CIOT | `PL_MDFe_.../MDFe_Nota_Tecnica_2026_001.pdf` (4p, maio/2026) |
| CNPJ alfanumérico (2027) | `PL_MDFe_.../DFe NTCJ 2025.001_CNPJ Alfa_v1.00.pdf` (15p) |
| PAA | `PL_MDFe_.../MDFe_Nota_Tecnica_2022_02_PAA_v1.02.pdf` (9p) |
| **Portal da SVRS salvo** | `PL_MDFe_.../Portal do Manifesto Eletrônico de Documentos Fiscais.html` |
| URLs da SEFAZ | `nfe/.../mdfe3/classes/MDFAutorizador3.java` |
| Como chamar a SEFAZ | `nfe/.../mdfe3/webservices/WSFacade.java` |
| Como assinar | `nfe/.../*/XMLSigner` |
| Certificado A1/A3 | `Java_Certificado/` (docs) |
| Padrão de WSDL | `Java_CTe/wsdl/` (7 WSDL) |
| Tabelas fiscais | `l10n-brazil/l10n_br_fiscal/data/*.csv` |

## 2.8 Os 49 arquivos do pacote MDFe, um a um

Inventário completo, lido arquivo por arquivo. Os 41 XSD estão especificados
pelo **elemento raiz que declaram**, que é o que diz o papel de cada um — o nome
do arquivo sozinho não diz.

### Os 41 XSD

| Arquivo | Raiz | Tags | Para que serve |
|---|---|---|---|
| `mdfe_v3.00.xsd` | `MDFe` | 1 | o manifesto |
| `procMDFe_v3.00.xsd` | `mdfeProc` | 3 | MDFe + protocolo da SEFAZ |
| `mdfeTiposBasico_v3.00.xsd` | — | **252** | **a base: todos os tipos** |
| `mdfeModalRodoviario_v3.00.xsd` | `rodo` | **89** | modal 1 — rodoviário |
| `tiposGeralMDFe_v3.00.xsd` | — | 2 | tipos gerais |
| `xmldsig-core-schema_v1.01.xsd` | `Signature` | 14 | assinatura XML |
| `eventoMDFeTiposBasico_v3.00.xsd` | — | 31 | base dos eventos |
| `eventoMDFe_v3.00.xsd` | `eventoMDFe` | 1 | evento |
| `evCancMDFe_v3.00.xsd` | `evCancMDFe` | 4 | **cancelamento** |
| `evEncMDFe_v3.00.xsd` | `evEncMDFe` | 7 | **encerramento** |
| `evConfirmaServMDFe_v3.00.xsd` | `evConfirmaServMDFe` | 3 | confirmação de serviço |
| `evIncCondutorMDFe_v3.00.xsd` | `evIncCondutorMDFe` | 5 | incluir condutor |
| `evInclusaoDFeMDFe_v3.00.xsd` | `evIncDFeMDFe` | 9 | incluir DF-e no MDF-e |
| `evPagtoOperMDFe_v3.00.xsd` | `evPagtoOperMDFe` | 29 | **pagamento da operação** |
| `evAlteracaoPagtoServMDFe_v3.00.xsd` | `evAlteracaoPagtoServMDFe` | 26 | alteração de pagamento |
| `mdfeModalAereo_v3.00.xsd` | `aereo` | 7 | modal 2 — aéreo |
| `mdfeModalFerroviario_v3.00.xsd` | `ferrov` | 15 | modal 3 — ferroviário |
| `mdfeModalAquaviario_v3.00.xsd` | `aquav` | 26 | modal 4 — aquaviário |
| `enviMDFe_v3.00.xsd` | `enviMDFe` | 1 | envio |
| `retEnviMDFe_v3.00.xsd` | `retEnviMDFe` | 1 | retorno do envio |
| `retMDFe_v3.00.xsd` | `retMDFe` | 1 | retorno do manifesto |
| `consSitMDFe_v3.00.xsd` | `consMDFe` | 1 | **consultar situação** |
| `consSitMDFeTiposBasico_v3.00.xsd` | — | 13 | tipos da consulta |
| `retConsSitMDFe_v3.00.xsd` | `retConsSitMDFe` | 1 | retorno da consulta |
| `consStatServMDFe_v3.00.xsd` | `consStatServMDFe` | 1 | **status do serviço** |
| `consStatServTiposBasico_v3.00.xsd` | — | 11 | tipos do status |
| `retConsStatServMDFe_v3.00.xsd` | `retConsStatServMDFe` | 1 | retorno do status |
| `consReciMDFe_v3.00.xsd` | `consReciMDFe` | 1 | **consultar recibo** |
| `consReciMDFeTiposBasico_v3.00.xsd` | — | 21 | tipos do recibo |
| `retConsReciMDFe_v3.00.xsd` | `retConsReciMDFe` | 1 | retorno do recibo |
| `consMDFeNaoEnc_v3.00.xsd` | `consMDFeNaoEnc` | 1 | **não encerrados** |
| `consMDFeNaoEncTiposBasico_v3.00.xsd` | — | 12 | tipos |
| `retConsMDFeNaoEnc_v3.00.xsd` | `retConsMDFeNaoEnc` | 1 | retorno |
| `mdfeConsultaDFe_v3.00.xsd` | `mdfeConsultaDFe` | 1 | consultar DF-e |
| `mdfeConsultaDFeTiposBasico_v3.00.xsd` | — | 10 | tipos |
| `retMDFeConsultaDFe_v3.00.xsd` | `retMDFeConsultaDFe` | 1 | retorno |
| `distMDFe_v3.00.xsd` | `distMDFe` | 1 | **distribuição** |
| `leiauteDistMDFe_v3.00.xsd` | — | 13 | leiaute da distribuição |
| `retDistMDFe_v3.00.xsd` | `retDistMDFe` | 1 | retorno |
| `procEventoMDFe_v3.00.xsd` | `procEventoMDFe` | 1 | evento processado |

### Os 6 documentos e a página

| Arquivo | Páginas | O quê |
|---|---|---|
| `MOC_MDFe_VisaoGeral_v3.00b.pdf` | 82 | chave de acesso, fluxo, visão geral |
| `MOC_MDFe_Anexo I_Leiaute e Regras Validação_v3.00b.pdf` | 45 | **regras F01-F18, cStat** |
| **`MOC_MDFe_Anexo II - DAMDFE_v3.00b.docx`** | 158 parágrafos | **Anexo II — o DAMDFE, o documento impresso.** Sumário: leiaute de impressão, código de barras CODE-128C e cálculo do DV, consulta por QR Code, contingência, papel e QR Code, e os **8 modelos de impressão** (rodoviário, aéreo, aquaviário, ferroviário — normal e contingência) |
| `MDFe_Nota_Tecnica_2026_001.pdf` | 4 | **regra do CIOT, cStat 684** |
| `MDFe_Nota_Tecnica_2025_001_1.03.pdf` | 7 | NT 2025.001 |
| `DFe NTCJ 2025.001_CNPJ Alfa_v1.00.pdf` | 15 | CNPJ alfanumérico |
| `MDFe_Nota_Tecnica_2022_02_PAA_v1.02.pdf` | 9 | PAA |
| `Portal do Manifesto Eletrônico de Documentos Fiscais.html` | — | **a página do portal da SVRS salva localmente** |

### O que o DOCX do DAMDFE acrescenta, e por que importa

O Anexo I diz o que o XML tem. O Anexo II diz **o que o motorista recebe
impresso** — e o MDF-e tem essa obrigação, que a NFS-e e a NFe não têm:

- leiaute de impressão do DAMDFE
- código de barras **CODE-128C** e o cálculo do dígito verificador dele
- consulta por **QR Code**, com as dimensões mínimas
- regras para MDF-e em **contingência**
- requisitos de papel
- **8 modelos**: rodoviário, aéreo, aquaviário, ferroviário — cada um em
  emissão normal e em contingência

Se o ERP for emitir MDF-e para(driver que realmente transporta), isso é
obrigação, não detalhe. `Java_Pdf_Signature/java-pdf-signature-1.1.jar` (7 MB)
assina PDF e é o caminho para gerar o DAMDFE.

---

# PARTE 3 — O estado do ERP

## 3.1 O que existe de MDF-e

| Camada | Estado |
|---|---|
| Tabela `bc_fis_mdfe` | **existe, 0 linhas** |
| `Mdfe.java` | existe — `numero`, `serie`, `chaveAcesso`, `dataEmissao`, `status`, `ufInicio`, `ufFim`, `xml` |
| `MdfeRepository` | existe — **um método**: `findByEmpresaIdAndChaveAcesso` |
| Controller | **não existe** |
| Service | **não existe** |
| Tela | **não existe** |
| Certificado de SEFAZ | **não existe** (o que há é o da NFS-e, que é outro) |
| Lib no `pom.xml` | **não está** |

Colunas de `bc_fis_mdfe`: `id`, `uuid`, `empresa_id`, `numero`, `serie`,
`chave_acesso` (50), `data_emissao`, `status` (20, default `DIGITADA`),
`uf_inicio` (char 2), `uf_fim` (char 2), `xml` (text), mais `created_at`,
`updated_at`, `created_by`, `updated_by`, `deleted_at`.

**Faltam colunas** para um MDF-e de verdade: `uf_carregamento`,
`municipio_carregamento`, `municipio_descarregamento`, `ciot`, `recibo`,
`protocolo`, `nsu`, `modal`, `tp_emit`, `tp_transp`. E `xml` em `text` no
Postgres contraria a decisão já tomada no projeto — documento vai no Mongo,
o Postgres guarda a referência.

O default `'DIGITADA'` indica que o desenho original era de lançamento manual,
sem transmissão. **Nenhum dos dois é o que a SEFAZ exige.**

## 3.2 NFe também não emite — e isso muda a promessa

`src/main/java/br/com/brasil_saas/fiscal/service/impl/NFeServiceImpl.java`:
**89 linhas, três TODOs**, apontando para `br.com.swconsultoria.nfe.Nfe`:

```java
// TODO: montar TEnviNFe a partir do pedido e chamar ...Nfe.montaNfe/env
// TODO: montar evento de cancelamento e chamar ...Nfe.cancelarNfe
// TODO: chamar ...Nfe.consultaXml
```

A interface `NFeService` promete `emitirNFe`, `cancelarNFe` e
`consultarSituacao`. **Nenhuma das três funciona.** Já estava assim antes desta
sessão.

**Consequência:** não existe infraestrutura de SEFAZ no ERP. A NFS-e funciona
porque a prefeitura de SP é um SOAP simples, com certificado e um XSD, e a
`nfse-sp-api` faz isso. MDF-e é SEFAZ: Axis2, autorização com protocolo,
consulta por lote, evento de encerramento, encerramento de manifesto, recibos.
São outras coisas.

## 3.3 O que a NFS-e ensina, e que se aplica

Duas lições da NFS-e que mudam como o MDF-e deve ser feito:

1. **Contrato único, dos dois lados.** `contrato_nfse` é o mesmo nome e a mesma
   assinatura no Java e no Ruby. O emissor se ajusta, não o consumidor.
2. **O motivo da recusa é persistido.** `bc_fis_nfse_retorno`, uma linha por
   chamada, com `sucesso`, `codigo` extraído, e o JSON bruto no Mongo. Sem
   isso, a tela de erro é uma parede e ninguém descobre o motivo.
3. **Validação local, nunca contra a prefeitura.** Se a prefeitura cair,
   não pode derrubar a API local.
4. **Health check não pode depender de terceiro.** O watchdog usava
   `consulta-cnpj` (1,27 s, ia à prefeitura) e derrubou a API Java 3 vezes
   enquanto o ERP emitia por ela.

---

# PARTE 4 — Como montar, na ordem

## Passo 0 — Decide a biblioteca (bloqueia todo o resto)

**Um `curl` decide.** Verificar se a fincatto tem release estável no Maven
Central, e qual versão:

```
https://search.maven.org/solrsearch/select?q=g:com.github.wmixvideo&rows=20&wt=json
https://central.sonatype.com/artifact/com.github.wmixvideo/nfe
```

Se houver release → usa a fincatto, que é a melhor das duas e traz NFe e CT-e
junto. Se só houver snapshot → compila do fonte (a pasta `nfe/` está inteira
aqui) ou vai de `java-mdfe 3.00.4` aceitando o risco do `java_certificado`.

## Passo 1 — Certificado A1 de SEFAZ

O que existe é o certificado da **NFS-e**, que é outro certificado, de outro
emissor. MDF-e usa A1 com CNPJ do transportador. A documentação de A1 e A3 está
em `Java_Certificado/` (12 classes + docs + 2 `.pfx` de teste marcados
`NaoUsar_*`).

A senha **não vai em arquivo versionado**. No padrão do projeto, fica em
`/etc/brasil_saas/*.env` com `600`.

## Passo 2 — Tabela e colunas

Ampliar `bc_fis_mdfe` por migration **nova** (V102+), nunca editando migration
aplicada — reescrever arquivo aplicado quebra o checksum do Flyway. Guardar
documento no Mongo, com a referência no Postgres, como já foi decidido.

## Passo 3 — Contrato único

`contrato_mdfe` com o mesmo nome e assinatura que o contrato da NFS-e, mesmo
formato de resposta, mesmo motivo de recusa persistido, mesmos três estados de
`confirmado`.

## Passo 4 — Emissor

Reaproveitar o desenho da `nfse-sp-api`: **uma API só, sem fallback
automático**, validação local, contrato único, motivo de recusa gravado antes de
lançar exceção. O fallback fica para depois — e quando voltar, tem que passar no
`testar_failover.rb` que já existe.

## Passo 5 — Em homologação primeiro

Ambiente `HOMOLOGACAO` (`tpEmis = 2`), SVRS `mdfe-homologacao.svrs.rs.gov.br`.
`consultaStatus` antes de qualquer envio: se a SEFAZ estiver fora do ar, não
tenta emitir. Emitir em homologação **não** cria documento válido.

## Passo 6 — A regra do CIOT, antes de novembro

`cStat 684`. Entra no validador **antes** de 23/11/2026, mesmo que a emissão
só comece depois. Uma linha que rejeita localmente custa menos que uma nota
recusada em produção.

## Passo 7 — Tela

Hoje não existe controller, service nem tela. Reaproveitar o padrão de
`Nfse.jsx` + a `bc_fis_nfse_retorno`, e ligar a **aba de retorno da prefeitura**
que ainda não existe (os dados estão lá, o endpoint existe, falta a aba).

## Passo 8 — Guardar a origem dos documentos oficiais

`PL_MDFe_...` e `NFSe-SaoPaulo-SP` não têm repositório. Guardar a **origem** de
cada arquivo (SEFAZ-SP, SVRS, Confaz) no `LEIA-ME.md` da pasta, junto com a
data. É a única forma de saber de onde veio, quando um dia precisar da revisão
nova.

---

# PARTE 5 — Riscos, e o que NÃO foi verificado

## 5.1 Riscos declarados

| Risco | Gravidade | Como fechar |
|---|---|---|
| fincatto é `SNAPSHOT`, não release | **alta** | `curl` no Maven Central (Passo 0) |
| `java-mdfe` 3.00.4 × `java_certificado 3.16` | **alta** | compilar e assinar um MDF-e de teste |
| Sem certificado de SEFAZ | **alta** | Passo 1 |
| Rejeição cStat 684 em 23/11/2026 | **alta, com data** | Passo 6, antes de novembro |
| Tabela sem as colunas de MDF-e | média | Passo 2 |
| NFe não emite (3 TODOs) | média | fora do escopo do MDF-e, mas alguém vai perguntar |
| `ini4j` ausente se for `java-mdfe` | baixa | adicionar ao pom |
| 20 pastas sem `.git` | média | este documento guarda a origem |

## 5.2 O que NÃO foi verificado — leia antes de confiar

Isso é importante e está aqui para não ser perdido:

- **MDF-e e CT-e foram testados contra a SEFAZ em homologação, e responderam
  `cStat 107`.** Isto atualiza este documento, que antes dizia que nada tinha
  sido executado. Detalhe em `docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`.
  - `GET /api/fiscal/mdfe/status` → 107, `ufAtendente: RS`, SVRS
  - `GET /api/fiscal/cte/status` → 107, `ufAtendente: SP`, portal de SP
- **A biblioteca escolhida é a fincatto, não a swconsultoria.** Verificado
  também o porquê: `java-mdfe 3.00.4` chama `Certificado.getTipo()`, que não
  existe no `java_certificado 3.16` que o ERP já usava, e dá
  `NoSuchMethodError` em runtime.
- **O `cStat 107` prova a cadeia TLS, o A1 e o envelope SOAP. Não prova o XML
  do documento.** Nenhum MDF-e nem CT-e foi emitido, e a montagem do MDF-e não
  foi implementada. Se o CNPJ não estiver habilitado para MDF-e na SVRS, isso
  só aparece no primeiro envio.
- **O A1 do ERP é o que serve.** `e-CNPJ A1` da ICP-Brasil, `AC SAFEWEB RFB
  v5`, válido até 09/03/2027. O mesmo que assina a NFS-e da prefeitura de SP.
  Registrado em `/etc/brasil_saas/cert.env` (600).
- **As CAs da ICP-Brasil estão instaladas** e o truststore JKS que o ERP usa
  está montado, por `installbase.sh`. Automatizado e idempotente.
- **Produção nunca foi testada.** A lib devolve `HOMOLOGACAO` fixo; a config
  sobrescreve, e sobrescrever não é funcionar.
- **Nenhuma tela foi vista no navegador.** Não havia navegador conectado. O que
  foi verificado: build passa, endpoints devolvem o que as telas leem. O que a
  tela *parece* e o que a pessoa *vê* não são a mesma coisa.
- **O EFD Contribuições não foi exercitado.** A lib resolve no `pom.xml`, sem
  endpoint.
- **A regra do CIOT não foi implementada** — foi lida e documentada, e a
  produção é 23/11/2026.

## 5.3 Quatro erros meus, registrados

Para não repetir:

1. **"Não existe WSDL de MDF-e e a URL não está no repo."** Falso. Procurei por
   extensão de arquivo e não li o código. A URL está no `MDFAutorizador3.java`.
   *Buscar por extensão não acha string embutida.*
2. **`nfe/` quase foi descartada como "material da NFe".** É a fincatto
   `documentofiscal`, com MDF-e completo. *Nome de pasta não descreve o
   conteúdo.*
3. **O inventário contou pastas e subpastas** quando o que resolveria era
   `grep` por conteúdo. As 183 menções a MDF-e em `nfe/` só apareceram quando
   busquei pelo texto. *Conteúdo, não nome.*
4. **O primeiro inventário tinha uma lista fixa de ~20 extensões.** Tudo que não
   estava na lista caía numa categoria "outro" que eu nunca imprimi. **Perdi
   assim 43 tipos de arquivo**, entre eles:
   - `MOC_MDFe_Anexo II - DAMDFE_v3.00b.docx` — 1,1 MB, o manual do documento
     impresso, com os 8 modelos de impressão e o código de barras
   - `Portal do Manifesto Eletrônico de Documentos Fiscais.html` — a página do
     portal da SVRS salva localmente
   - **3 `.jar` pré-compilados**: `java-efd-contribuicoes-1.32.1.jar` (489 KB),
     `java-pdf-signature-1.1.jar` (7,2 MB), `agent.jar`
   - **15 `.xjb`** em `Java_NFe/scripts/bindings/` — os bindings JAXB gerados
     dos XSD, que mostram exatamente como a lib mapeia o XML para classe
   - **12 `.class`** compilados versionados dentro de `src/main/java`
   - 1 `.rar` (`Exemplo-Java-Efd-Icms.rar`), 1 `.sln` e 1 `.csproj` (projeto
     C#), 1 `.go`, 1 `.rs` (Rust), 1 `.onnx` e 1 `.npz` (modelo de IA), 1
     `.htpasswd`, 128 `.adoc`, 51 `.rst`, 100 `.po`/`.pot` (traduções)

   *Não ter lista de extensões fechado. O script que refeiz classifica pelo que
   o arquivo é, lendo o conteúdo, e cobre os 43 tipos.*

E os erros recorrentes já conhecidos nesta sessão: caractere CJK escapando em
comentário (3 vezes), `awk` com campo errado, `String#to_s(16)`, palavra-chave
do Ruby escrita em Java, linha órfã ao substituir método, e escrever unit sem
aspas num caminho com espaço — que reproduziu a causa do crash-loop de 8,6 h.

## 5.4 O catálogo completo dos 16.298 arquivos

Existe agora um catálogo de **todos** os arquivos das 23 pastas, lidos pelo
conteúdo, com um JSON por pasta. Não está no git (seriam dezenas de MB); o
script que o gera está em `/tmp/opencode/catalogo_completo.py`.

O que ele extrai por tipo:

| Tipo | O que extrai |
|---|---|
| `.java` | pacote, classe, métodos públicos, anotações |
| `.xsd` | namespace, elemento raiz, `complexType`, nº de tags |
| `.wsdl` | serviços, `portType`, **URLs (`soap:address`)** |
| `.pdf` | páginas, título, início do texto |
| `.docx` | texto extraído do `word/document.xml` |
| `.html` | título, URLs |
| `.json` | chaves de primeiro nível |
| `.csv` | colunas e número de linhas |
| `.yml` | chaves de topo |
| `.xml` | tag raiz e atributos |
| `.md` | títulos |
| `.pfx` | **subject do certificado**, via `openssl pkcs12` |
| outros | tamanho e nome |

**Os 11 WSDL, com as URLs, como o catálogo extraiu:**

| WSDL | Serviço |
|---|---|
| `Java_CTe/wsdl/CTeRecepcaoSincV4.wsdl` | recepção CT-e |
| `Java_CTe/wsdl/CTeRecepcaoEventoV4.wsdl` | evento CT-e |
| `Java_CTe/wsdl/CTeConsultaV4.wsdl` | consulta CT-e |
| `Java_CTe/wsdl/CTeStatusServicoV4.wsdl` | status CT-e |
| `Java_CTe/wsdl/CTeRecepcaoGTVeV4.wsdl` | GTV-e |
| `Java_CTe/wsdl/CTeRecepcaoOSV4.wsdl` | OS |
| `Java_CTe/wsdl/CTeRecepcaoSimplificadoV4.wsdl` | simplificado |
| `esocial/.../WsEnviarLoteEventos-v1_1_0.wsdl` | eSocial |
| `esocial/.../WsConsultarLoteEventos-v1_1_0.wsdl` | eSocial |
| `esocial/.../WsConsultarIdentificadoresEventos-v1_0_0.wsdl` | eSocial |
| `esocial/.../WsSolicitarDownloadEventos-v1_0_0.wsdl` | eSocial |

**Nenhum de MDF-e.** Confirmado de novo: o MDF-e não tem WSDL, e a URL dele
está como string no `MDFAutorizador3.java`. Não há WSDL nem no pacote oficial
da SEFAZ.

## 5.5 A pasta `microservices/` — o tamanho

**554 MB, 14.135 arquivos versionados, `.git` com 1,2 GB.** Mas:

- O `pom.xml` **já exclui** `microservices/**` do empacotamento:
  ```xml
  <resource>
      <directory>src/main/resources</directory>
      <excludes><exclude>microservices/**</exclude></excludes>
  </resource>
  ```
  Não vai para o JAR nem para o `target/classes`. `git status` = 0,13 s.
- As 5 maiores: `l10n-brazil` 192M, `spring-ai` 180M, `esocial` 54M,
  `nfe` 32M, `nfse-sp-api` 24M. **426 MB são `l10n-brazil` + `spring-ai`, e
  o `spring-ai` não tem nada a ver com o ERP.**

A pasta **fica como está** — foi o que o usuário pediu. Este manual existe
para que a origem de cada pasta não se perca, porque 20 delas já perderam.

---

# PARTE 6 — Referência rápida

## Comandos de verificação

```bash
# o ERP está no ar
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/auth/me -H "Authorization: Bearer x"
# (500 com token falso é o comportamento normal; 403 com token válido)

# a API de NFS-e está de pé (caminho completo, sem /status)
curl -s http://localhost:4567/api/nfse-sp/status

# as migrations que o Flyway aplicou (a senha vem do ambiente, nunca do comando)
PGPASSWORD="$BRASIL_SAAS_PG_PASSWORD" psql -h localhost -U postgres -d brasil_saas -tAc \
  "select version||' '||description||' '||success from brasil_saas.flyway_schema_history order by installed_rank desc limit 6;"

# checksum de migration, do jeito que o Flyway calcula
python3 /tmp/opencode/checksum_flyway.py <checksum> <arquivo.sql>

# inventário e origem das pastas
python3 /tmp/opencode/inventario_microservices.py
python3 /tmp/opencode/origem_microservices.py
```

## Login

```
POST /api/auth/login
{"username": "<usuario>", "password": "<senha>"}
```

A resposta vem em `data.accessToken` — **não** em `token`. E os campos do
corpo são `username`/`password`, não `email`/`senha`.

> **Nenhuma senha neste documento.** Credencial em arquivo versionado é
> credencial vazada: o `.md` vai para o git e para o Mongo, e quem leu o log
> do build leu junto. O script `scripts/salvar_docs_mongo.py` recusa a gravar
> qualquer documento que contenha termo sensível — foi ele que pegou uma senha
> que eu mesmo tinha escrito aqui.

## Ordem de uma emissão, ponta a ponta

```
ERP  →  POST /api/fiscal/mdfe/emitir
        ↓ valida local (regras F01-F18, cStat 684, chave de acesso, DV)
        ↓ grava intenção com confirmado = null
API  →  assina o XML com o certificado A1
        ↓ POST MDFeRecepcao (SVRS)
        ↓ grava o retorno em bc_fis_mdfe_retorno, com o motivo
        ↓ confirmado = true com protocolo, ou false com cStat
ERP  →  200 com protocolo, ou erro com o cStat e a mensagem
        ↓ se confirmado = null, consultar antes de reemitir
```

## Estado do NFS-e em produção (26/09/2026)

```
nfse-sp-api       active/enabled     4567, única API
nfse-sp-bridge    inactive/disabled   Ruby, desligado
nfse-watchdog     inactive/disabled   desligado, não religar como estava
nginx             inactive/disabled
```

Contrato único `contrato_nfse` nas duas implementações. Nota 32 emitida com
código municipal real (2668) e cancelada pelo mesmo caminho — prova do caminho
feliz, não só da recusa.

---

## Documentos relacionados

```
docs/pesquisa/MICROSERVICES-O-QUE-TEM.md       inventário das 23 pastas
docs/pesquisa/TELAS-VAZIAS-DO-FISCAL.md         CFOP e CEST que mostravam vazio
docs/pesquisa/contrato-nfse-unico.md            o contrato
docs/pesquisa/o-que-sobrevive-a-tela-de-erro.md a tabela de retornos
docs/pesquisa/CAUSA-do-crash-loop-NFSE.md       o crash-loop
docs/pesquisa/diagrama-fallback-nfse.md         o desenho do fallback
src/main/resources/microservices/INVENTARIO.md  índice do projeto
src/main/resources/microservices/O-SERVICO-EM-PRODUCAO.md
```

**Nada disto está commitado.**
