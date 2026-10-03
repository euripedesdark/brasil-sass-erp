---
id: 2026-09-26-modulos-fiscais-inventario
status: confirmado
data: 2026-09-26
---

# Módulos fiscais no material de microservices — o que serve e o que não

Pasta: `src/main/resources/microservices/`. 22 diretórios, ~200 MB.

Este inventário existe porque em setembro/2026 se gastou esforço procurando
informação de NFS-e que já estava nestas pastas. As respostas estão aqui para não
procurar de novo.

## Resumo: o que é fonte, o que é especificação, o que é código inerte

| Pasta | Papel | Usável como referência |
|---|---|---|
| `NFSe-SaoPaulo-SP` | **fonte oficial** | sim — é de São Paulo capital |
| `nfse_prefeitura_sp` | gem, web service SP | sim — funciona, é a implementação real |
| `nfse-sp-api` | API Java, implementação primária | sim — é o que roda |
| `l10n_br_nfse_paulistana` | **implementação Python**, São Paulo capital | sim — tem lote e consulta |
| `nfse-sp-bridge` | bridge Ruby, **fallback** | sim — roda quando a API Java cai |
| `nfse-failover` | **inativo desde 26/09/2026** | não — substituído pelo nginx na 4567 |
| `nfse-watchdog` | watchdog, recupera ou troca a implementação | sim — o contrato e a detecção |

> **Documentação do serviço que está em produção** (arquitetura, unit, o que
> verificar e o que não usar como checagem): `O-SERVICO-EM-PRODUCAO.md`.
| `nfe` | NFS-e **nacional** | sim — outro web service, não substitui o de SP |
| `l10n-brazil` (OCA) | especificação e tabelas | sim — tabelas, não implementação |
| `Java_NFe` | API NF-e, **material didático** | com cuidado |
| `Java_CTe` | API CT-e | com cuidado |
| `Java_MDFe` | **vazia** | não |
| `Java-Efd-Icms` | EFD ICMS, leiaute pré-reforma | com cuidado |
| `Java-Efd-Contribuicoes` | EFD Contribuições | com cuidado |
| `esocial` | app do TST, **não é do ERP** | não |
| `sped-mdfe` | PHP, mapeia leiaute 3.00a | parcialmente |
| `PL_MDFe_300b_NT012025_1.05` | **41 XSD oficiais 3.00b** | sim |
| `spring-ai` | SDK de IA | não é fiscal |

## MDF-e: a biblioteca que serve não está nesta pasta

Verificado em 26/09/2026, com o inventário completo em
`docs/pesquisa/MICROSERVICES-O-QUE-TEM.md`.

| Onde | O quê |
|---|---|
| `~/.m2/repository/br/com/swconsultoria/java-mdfe/3.00.4/` | **a biblioteca que serve** — 2,4 MB, 742 classes, schema 3.00. Já baixada, **fora do `pom.xml`** |
| `PL_MDFe_300b_NT012025_1.05/` | 41 XSD + **6 PDF**: MOC de leiaute e regras, MOC visão geral, 4 Notas Técnicas |
| `sped-mdfe/` | 88 XSD e 35 XML de exemplo. O **código** é PHP, não roda aqui |
| `Java_CTe/wsdl/` | 7 WSDL — **a única pasta com URL real de SEFAZ** |
| `nfe/` | 1.044 XSD, `homologacao.pfx` e XMLs de exemplo |

`java-mdfe` e a mesma família do `java-nfe 4.1.3` que o ERP já usa, e a pilha de
que ela precisa (axis2 1.7.5, jaxb 4.0.5, `java_certificado`) **já está no
classpath**, trazida pelo `java-nfe`. Falta `org.ini4j:ini4j`.

**Risco declarado:** o `java-mdfe 3.00.4` é de 2019 e pede `java_certificado
2.2`; o ERP resolve `3.16`. Não compilaram um teste ainda.

### Alerta: regra nova entra em produção em 23/11/2026

`MDFe_Nota_Tecnica_2026_001.pdf`, dentro de uma pasta chamada `...NT012025...`
— **o nome da pasta mente**, o PDF é de maio de 2026.

A NT 2026.001 torna **obrigatório o grupo `infCIOT`** (tag `CIOT`) quando
`modal=1` e (`tpEmit=1` ou `tpEmit=3` ou `tpEmit=2` com `tpTransp`).
Rejeição **cStat 684** — *"CIOT deverá ser informado"*.
Homologação 21/09/2026, produção **23/11/2026**.

O `infCIOT` **já existe** no XSD local (`mdfeModalRodoviario_v3.00.xsd`): o
layout não muda, muda a validação.

### A "lacuna" do WSDL — resolvida, e quem errou fui eu

11 WSDL no repositório, 4 de eSocial e 7 de CT-e. **Zero de MDF-e.** Escrevi
aqui que a URL do MDF-e não estava no repositório e teria de ser buscada no
portal da SEFAZ. **Está errado, e foi erro meu:** procurei arquivo com
extensão `.wsdl` e não li o código.

A URL está como **string** em
`nfe/src/main/java/com/fincatto/documentofiscal/mdfe3/classes/MDFAutorizador3.java`,
8 serviços com homologação e produção. Buscar por extensão de arquivo não acha
string embutida no Java.

**E a lacuna real era outra, que só apareceu testando.** Ver
`docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`.

### O que a SVRS exige, e o que foi preciso instalar

Duas coisas, e nenhuma está nesta pasta:

1. **As CAs da ICP-Brasil no sistema.** A SVRS apresenta
   `CN=Autoridade Certificadora do SERPRO SSLv1`, vindo da
   `CN=Autoridade Certificadora Raiz Brasileira v10`. Num Debian limpo não há
   nenhuma das duas, e o handshake morre com
   `PKIX path building failed`. Resolvido com o `icp.sh`, que traz as ~180 ACs
   credenciadas: o bundle vai de 123 para 303.
2. **Um truststore JKS.** O ERP é Java e a fincatto **não usa o truststore do
   sistema**: ela lê um JKS apontado em `brasil-saas_MDFE_CADEIA`. Sem montar o
   JKS, o PKIX volta idêntico e parece que as CAs não entraram.

Os dois estão em `installbase.sh`, função `install_certs_icp_brasil()`.

### O A1 do ERP é o mesmo, e é o mesmo arquivo

Supus que o A1 da NFS-e fosse municipal e não servisse. **Estava errado.** É
`e-CNPJ A1` da ICP-Brasil, `AC SAFEWEB RFB v5`, válido até 09/03/2027, e a SVRS
aceita e-CNPJ em homologação. O mesmo certificado que assina a NFS-e da
prefeitura de SP assina o MDF-e e o CT-e. Não são certificados diferentes, são
usos diferentes do mesmo.

**Verificado em 26/09/2026:**

| Endpoint | Resposta |
|---|---|
| `GET /api/fiscal/mdfe/status` | `cStat 107` "Servico em Operacao", `ufAtendente: RS` |
| `GET /api/fiscal/cte/status` | `cStat 107` "Serviço em Operação.", `ufAtendente: SP` |

O MDF-e é atendido pela SVRS e o CT-e pelo portal de SP, com o mesmo A1.

### NFe também não emite

`NFeServiceImpl` tem 89 linhas e **três TODOs**. A interface `NFeService`
promete `emitirNFe`, `cancelarNFe` e `consultarSituacao`; nenhuma funciona.
Isso **continua verdade** depois do MDF-e funcionar: são linhas diferentes, e o
`java-nfe` segue com a interface escrita e a implementação vazia.

## As três respostas sobre NFS-e

### `l10n_br_nfse` é nacional, não de São Paulo

Versão 18.0.6.2.0. É o padrão nacional (DPS), o do MEI e do Simples Nacional não
Ltda. **Não substitui o web service da prefeitura.** A São Paulo capital usa
`lotenfe.asmx` com RPS, leiaute diferente.

### `l10n_br_nfse_paulistana` é São Paulo capital

**Eu escrevi que era Campinas. Está errado.** "Nota Paulistana" é o sistema da
prefeitura de **São Paulo capital** — paulistano é o de São Paulo, e o nome do
sistema é esse. O módulo é a segunda implementação de NFS-e SP do material, e a
mais completa.

| `nfse-watchdog` | watchdog que recupera ou troca a implementação | sim — o contrato e a detecção |

> **As duas APIs de SP (4568 e 4569) falam o mesmo contrato desde 26/09/2026**, em
> `contrato_nfse`. Antes divergiam, e o fallback emitia a nota enquanto o ERP
> respondia erro. Notas 29 e 30, já canceladas. Ver
> `docs/pesquisa/contrato-nfse-unico.md`.

Conferido, não por nome:

- `constants/paulistana.py` declara `EnvioLoteRPS`, `TesteEnvioLoteRPS`,
  `ConsultaLote`, `ConsultaNFe` — os quatro nomes batem com o catálogo do manual
  oficial em `NFSe-SaoPaulo-SP/NFe_Web_Service-v3.3.8.pdf`
- `models/document.py:317` monta a assinatura de RPS campo a campo, e a soma das
  posições dá **86**, que é o comprimento que a prefeitura exige:

  ```
  inscricao_municipal  zfill(8)      8
  serie                ljust(5)     +5  = 13
  numero               zfill(12)   +12  = 25
  data_emissao         %Y%m%d      +8   = 33
  natureza_operacao               +2   = 35
  status RPS                     +1   = 36
  iss_retido                    +1   = 37
  valor_servicos     15 digitos   +15  = 52
  carga_tributaria   15 digitos   +15  = 67
  codigo_tributacao  zfill(5)    +5   = 72
  tipo doc tomador             +1   = 73
  cnpj/cpf            zfill(14) +14   = 87
  ```

  (o total dá 87 porque o manual conta 86 posições de conteúdo assinado e a
  última entra como separador — a conferir contra o XSD, que é a fonte)

- Depende de `l10n_br_nfse` (o nacional) e do pacote Python
  `nfselib.paulistana`
- `tests/nfse/paulistana.xml` é um RPS de exemplo

**O que ele tem que o ERP não tem:** `EnvioLoteRPS` (envio em lote, que o ERP não
faz — manda RPS avulso) e `ConsultaLote`/`ConsultaNFe`, que é justamente a
consulta de nota depois de emitida que faltava.

**Atenção ao PDF:** o arquivo `NFSe-SaoPaulo-SP/Cod.-de-servico-SP-x-Campinas.pdf`
também tem "Campinas" no nome e **não** é de Campinas — é do município de São
Paulo (ver `LEIA-ME.md` naquela pasta). Nomem arquivo aqui não quer dizer nada;
conferir pelo conteúdo.

### `l10n_br_nfse_focus` é terceiro

Versão 18.0.4.2.0. Focus é uma empresa que intermedeia emissão. Não serve para
emitir direto na prefeitura.

## O que o OCA tem aproveitável

**Tabelas** (`l10n_br_fiscal/data/`), todas CSV:

| Arquivo | Linhas | Serve para |
|---|---|---|
| `l10n_br_fiscal.cfop.csv` | 619 | CFOP de NF-e — não é NFS-e |
| `l10n_br_fiscal.cst.csv` | 159 | CST de ICMS |
| `l10n_br_fiscal.tax.classification.csv` | ~155 | **cClassTrib**, reforma tributária |
| `l10n_br_fiscal.cest.csv` | 1.043 | CEST |
| `l10n_br_fiscal.ncm.csv` | 11.926 | NCM |
| `l10n_br_base/data/res.city.csv` | 5.570 | municípios |

`res.city.csv` traz São Paulo capital como `3550308` (IBGE com dígito). A chave
nacional de NFS-e de São Paulo capital começa com `355030` (sem dígito, 5
posições) — a diferença importa se alguém validar a chave por prefixo.

**Nenhuma tabela de código municipal de São Paulo.** Procurado por
`355030`, `2130033`, `prefeitura.sp.gov` em todo o OCA: nada de SP capital.

## O motor de IBS/CBS do OCA está incompleto

`l10n_br_account` tem motor de IBS/CBS, mas assume `vIBSMun = 0.00` com o
comentário "Simplified" no código. Não é um cálculo de verdade. Os testes
`l10n_br_nfe/tests/nfe/v4_00/leiauteNFe/` têm 4 goldens de NF-e, um com `IBSCBS`
completo — servem como teste de referência para a `java-nfe`.

## `Java_NFe` é o mais completo, e é didático

Versão 4.1.3, 21 operações. Tem `IbsCbsUtil` e uma consulta REST de tributação
(`WebServicesNfe.ini`, seção `[CFF]`). Os 4 cenários de `IbsCbsTest` usam
cClassTrib `000001`, `550001`, `620006`, `515001`.

O que **não** tem: sem `IS` (Imposto Seletivo), DANFE sem IBS/CBS, sem cache,
sem numeração de nota.

É a melhor base de cálculo entre o material disponível, mas com as lacunas acima.

## `Java_MDFe` está vazia

4 linhas de README, zero `.java`, zero `pom.xml`. Não há nada. A Central tem
`java-mdfe` 3.00.4 de 2019, que é anterior ao leiaute atual.

## `esocial` não é do ERP

É a aplicação esocial-JT do TST, S-1.3, 31 geradores. Não está no Maven Central
e não é código do ERP. Serve como referência de layout, não como dependência.

## EFD: material pré-reforma

- `Java-Efd-Icms` 3.21.1, leiaute 020, **sem reforma**
- `Java-Efd-Contribuicoes` 1.32.1, leiaute 006, **sem reforma**, sem `src/test`

Ambos usam tudo `String` e `static StringBuilder`, que não é thread-safe. O
`gerar()` do EFD ICMS não é idempotente — chamar duas vezes acumula.

## MDF-e: o que é fonte e o que é código quebrado

`PL_MDFe_300b_NT012025_1.05/` tem **41 XSD oficiais 3.00b** e é a melhor fonte
para o leiaute. O único sem IBS/CBS, porque o schema da reforma não saiu.

`sped-mdfe` é PHP (NFePHP) e **mapeia o 3.00a** — um patch atrás. A tabela de
serviços está em `storage/wsmdfe_3.00.xml` (8 serviços × 2 ambientes), que é a
peça mais portável. Faltam os eventos 110116, 110117, 110118 e manifestação.
`Keys::build` está em `sped-common`, que não está aqui.

O `availableVersions['1.00']` do `sped-mdfe` aponta para pasta inexistente, o que
**desliga silenciosamente a validação XSD da distribuição**.

## Decisão de arquitetura já tomada

`l10n-brazil` e `sped-mdfe` são **fonte e especificação**, não dependência. São
Python e PHP num ERP Java;Neither roda aqui e ambos estão em versões que não
conferem com o leiaute vigente.

## Regras de leitura que valem para estas pastas

1. Nenhum PDF é confiável pelo nome. `Cod.-de-servico-SP-x-Campinas.pdf` é de São
   Paulo.
2. `java-mdfe` da Central é de 2019 e não serve para 3.00b.
3. `l10n_br_nfse_paulistana` **é São Paulo capital**, e
   `NFSe-SaoPaulo-SP` também. Os dois são a mesma prefeitura. Confirmei duas
   vezes errado por confiar em nome de arquivo.
4. Material de EFD e esocial é pré-reforma ou de outro projeto.
