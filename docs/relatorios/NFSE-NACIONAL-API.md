# NFS-e Nacional — API (AGILIBlue / Rondonópolis)

API nova em Java que emite NFS-e pelo WebService do **AGILIBlue**, o mesmo que
está em `https://nfse.rondonopolis.mt.gov.br/api/`.

- Módulo: `src/main/resources/microservices/nfse-nacional-api/`
- Porta: **4570** (as de SP são 4568 Java, 4569 Ruby bridge, 4567 proxy)
- Contrato: as mesmas 4 rotas de `/api/nfse-sp` que o ERP já consome
- Estado: **17/17 testes, exit 0**, jar empacotado. **Nenhuma chamada de verdade
  foi feita** — falta a `UnidadeGestora` (ver "O que falta").

---

## 1. O que a documentação da prefeitura diz, e o código faz

Tudo abaixo saiu de `src/main/resources/db/seed/issqn/`, que é o material do
dono. Nada veio de suposição.

### A URL

```
https://nfse.rondonopolis.mt.gov.br/api/GerarNfse
```

Base `/api` + nome da operação. A documentação também traz a base isolada como
`https://nfse.rondonopolis.mt.gov.br//api/`, com barra dupla. A classe
`NfseNacionalOperacoes` normaliza para a forma sem duplicidade, que é a do
exemplo de operação.

As oito operações, e o processamento de cada uma:

| operação | processamento |
|---|---|
| `GerarNfse` | **síncrono** |
| `EnviarLoteRps` | **assíncrono** (protocolo) |
| `CancelarNfse` | síncrono |
| `SubstituirNfse` | síncrono |
| `ConsultarLoteRps` | síncrono |
| `ConsultarNfseRps` | síncrono |
| `ConsultarNfseFaixa` | síncrono |
| `ConsultarRequerimentoCancelamento` | síncrono |

Sete das oito devolvem o resultado na própria chamada. Só `EnviarLoteRps` exige
consulta posterior por protocolo.

### A homologação é parâmetro, não URL

> "adicione à request o parâmetro booleano `homologacao` com valor igual a
> true. [...] o ÁGILIBlue NFS-e realizará todas as etapas de validação, mas ao
> final **não irá gravar** a NFS-e no sistema"

Três consequências que o código impõe:

1. **Não existe host de homologação.** Implementar "ambiente de homologação" como
   URL separada aponta para um endereço que não existe.
2. **O número que volta em homologação não existe.** O código devolve o sucesso
   com a nota marcada como *validada*, nunca como emitida — ver
   `NfseNacionalService.validadaEmHomologacao`.
3. Vale para as oito operações, não só a emissão.

`nfse.nacional.homologacao` está **default `true`**. Com `false` e a
`UnidadeGestora` errada, a nota vai para o município errado e não há como
desfazer.

### A autenticação é uma escolha, e as duas não podem ser mandadas

O XSD do dono, `nfse-v-100.xsd`, linha 570:

```xml
<xsd:complexType name="tcIdentificacaoPrestador">
  <xsd:sequence>
    <xsd:choice>
      <xsd:element name="ChaveDigital" type="tsChaveDigital" minOccurs="1" maxOccurs="1"/>
      <xsd:element ref="dsig:Signature" minOccurs="1" maxOccurs="1" />
    </xsd:choice>
    <xsd:element name="CpfCnpj" type="tcCpfCnpj" minOccurs="1" maxOccurs="1" />
```

Duas coisas:

- **O `xsd:choice` não tem `minOccurs="0"`, então é obrigatório.** Declaração sem
  `ChaveDigital` e sem assinatura é recusada na segunda tag. `NfseNacionalRegras`
  exige uma ou outra e avisa antes.
- **`dsig` é `http://www.w3.org/2000/09/xmldsig#`** — XMLDSig padrão, o mesmo que
  a API de São Paulo já produz com o A1. **É aqui que está o reaproveitamento de
  verdade:** o `NfseNacionalSigner` é o JSR-105 do JDK
  (`javax.xml.crypto.dsig`), sem BouncyCastle e sem biblioteca de terceiro.

O que muda entre São Paulo e Rondonópolis, na assinatura:

| | São Paulo | AGILIBlue |
|---|---|---|
| alvo | envelope SOAP inteiro | documento inteiro |
| `Reference` | `URI=""` | `URI=""` |
| onde a `Signature` cai | na raiz | dentro de `IdentificacaoPrestador` |
| segunda assinatura | cadeia de 86 posições, em RSA | não existe |

O `URI=""` com transform `ENVELOPED` é o mesmo nos dois. O que muda é **onde a
`Signature` é depositada**.

> **Por que `URI=""` e não `#Id`.** Um `Id` em `IdentificacaoPrestador` seria o
> jeito normal de fazer isso, e foi a primeira tentativa. O XSD do AGILIBlue não
> declara atributo nenhum naquele tipo, e `attributeFormDefault="unqualified"`, de
> modo que o atributo extra é recusado:
> `cvc-complex-type.3.2.2: Attribute 'Id' is not allowed to appear in element
> 'IdentificacaoPrestador'`. Sem `Id`, não há id para referenciar, e `URI=""` com
> `ENVELOPED` é a forma correta.

### A validação é em três etapas, e a ordem decide o diagnóstico

> "validará a estrutura conforme a ordem a seguir, sendo que a cada validação
> subsequente depende do sucesso da anterior: a estrutura de forma sintática; a
> estrutura do XML baseado em um XSD; a assinatura do prestador"

Por isso `NfseNacionalService` valida o XSD **antes** de assinar. Invertido, um
erro de tag chega como recusa de assinatura — e quem depura passa a tarde mexendo
no certificado, que está perfeito.

---

## 2. O que o material do dono tem de defeito

Três coisas encontradas e registradas em teste. Nenhuma é contornada em silêncio.

### 2.1 O XSD não carrega: falta o `xsd:import` do XMLDSig

`nfse-v-100.xsd` referencia `dsig:Signature` na linha 574 e declara
`xmlns:dsig` no raiz — mas **não tem `xsd:import`** para aquele namespace. Como
está, o arquivo não abre num validador JAXP:

```
src-resolve.4.2: Error resolving component 'dsig:Signature'
```

A correção seria uma linha no arquivo da prefeitura:

```xml
<xsl:import namespace="http://www.w3.org/2000/09/xmldsig#"
            schemaLocation="xmldsig-core-schema.xsd"/>
```

**O arquivo do dono não foi editado.** O `NfseNacionalValidadorXsd` copia os dois
schemas para um diretório temporário, insere o import na cópia e carrega de lá. O
arquivo em disco continua byte a byte igual, e o dia que a prefeitura mandar o
XSD corrigido o método vira no-op.

Duas armadilhas encontradas no caminho, ambas registradas no código:

- `prefix` no `xsd:import` é **XSD 1.1**. Em 1.0 o validador responde
  `s4s-att-not-allowed: Attribute 'prefix' cannot appear in element 'import'`.
- Descobrir se a tag de abertura é auto-fechada procurando a **última barra**
  antes do `>` dá errado: dentro do próprio XSD, o namespace da W3C é
  `http://www.w3.org/2001/XMLSchema`, e a última barra cai no meio de um valor de
  atributo. O import entra dentro da string e o validador responde que o `xmlns`
  "não pode conter o caractere `<`". O certo é olhar o caractere imediatamente
  antes do `>`.

### 2.2 Oito dos dez exemplos usam um valor que o XSD recusa

O tipo `tsRegimeEspecialTributacao` restringe a `-2|-3|-4|-5|-6`.

| exemplo | `RegimeEspecialTributacao/Codigo` |
|---|---|
| Alíquota especial | **-1** |
| ConstrucaoCivil | **-1** |
| Estimativa | **-1** |
| Imune | **-1** |
| Isento lei específica | **-1** |
| Isento | **-1** |
| Normal | **-1** |
| SociedadeProfissional | **-1** |
| Simei | -5 |
| SN ME EPP | -6 |

Oito mandam `-1`, com `Descricao` "Nenhum". Oito contra dois aponta o **XSD como
o errado**, não os exemplos — mas quem decide é a prefeitura.

O efeito prático: um ERP que copie o `-1` do exemplo tem a nota recusada na
validação de XSD, com a mensagem `Value '-1' is not facet-valid with respect to
pattern '-2|-3|-4|-5|-6' for type 'tsRegimeEspecialTributacao'`.

O teste `regimeEspecialNegativoAceitoPeloXsd` fixa os dois números (8 e 2). Se
mudarem, a pergunta para a prefeitura pode ser encerrada.

### 2.3 Os exemplos têm encoding misto, BOM e um `encoding` inválido

- **Encoding misto:** a maioria dos acentos está em UTF-8 e alguns em Latin-1
  (`Ç` como `C3 C7`, mas `Á` como `C1`), de colagem de texto de outra origem,
  num arquivo que se declara `utf-8`. Um parser conforme rejeita.
- **`Simei.xml` tem BOM UTF-8** (`EF BB BF`) e declara **`encoding="1.0"`**, que
  é um nome de encoding, não um.

Ambos dão `Content is not allowed in prolog`. O reparo existe só no teste
(`corrigir`); **a API emite em UTF-8 de verdade**, e o teste `saidaEmUtf8()`
verifica isso com acentos reais.

O `RegimeEspecialTributacao` do perfil `Normal` foi o único ponto em que a
estrutura gerada não passou no XSD, e a correção no teste foi usar `-2`. A
divergência em si está registrada em 2.2.

---

## 3. A reforma: o que está nas imagens dos PDFs

Antes eu escrevi que não conseguia ler as tabelas. **Instalei o tesseract e li.**
O que vem a seguir saiu das páginas renderizadas, e o que mudou o código.

### 3.1 As duas tags novas, e o wrapper

Página 13 do `atualizacao-e-ajustes-estruturais-...pdf`, que é uma imagem de
código:

```xml
<PisCofins>
  <CodigoSituacaoTributaria>-2</CodigoSituacaoTributaria>
  <CodigoTipoRetencao>-5</CodigoTipoRetencao>
</PisCofins>
```

**Não são duas tags soltas na declaração — estão dentro de `<PisCofins>`.** Eu
tinha escrito como tags soltas. O wrapper só entra se alguma das duas vier
preenchida, porque tag vazia é recusada e o XSD 1.00 do material não tem nenhuma
das três.

### 3.2 `CodigoSituacaoTributaria` — 68 valores, duas colunas

Seções 4 e 5, páginas 14 e 15. A tabela tem **duas colunas de código**, negativa
e positiva, com a mesma descrição:

| negativo | positivo | descrição |
|---|---|---|
| **-34** | **99** | Outras Operações |
| -33 | 98 | Outras Operações de Entrada |
| -32 | 75 | Operação de Aquisição por Substituição Tributária |
| -31 | 74 | Operação de Aquisição sem Incidência da Contribuição |
| -30 | 73 | Operação de Aquisição a Alíquota Zero |
| -29 | 72 | Operação de Aquisição com Suspensão |
| -28 | 71 | Operação de Aquisição com Isenção |
| -27 | 70 | Operação de Aquisição sem Direito a Crédito |
| -26 | 67 | Crédito Presumido — Outras Operações |
| -25 | 66 | Crédito Presumido — Receitas Tributadas e Não-Tributadas no Interno e Exportação |
| -24 | 65 | Crédito Presumido — Receitas Não-Tributadas no Interno e Exportação |
| -23 | 64 | Crédito Presumido — Receitas Tributadas no Interno e Exportação |
| -22 | 63 | Crédito Presumido — Tributadas e Não-Tributadas no Mercado Interno |
| -21 | 62 | Crédito Presumido — Exclusivamente a Receita de Exportação |
| -20 | 61 | Crédito Presumido — Exclusivamente a Receita Não-Tributada no Interno |
| -19 | 60 | Crédito Presumido — Exclusivamente a Receita Tributada no Interno |
| -18 | 56 | Op. com Direito a Crédito — Tributadas e Não-Tributadas, Interno e Exportação |
| -17 | 55 | Op. com Direito a Crédito — Não Tributadas, Interno e Exportação |
| -16 | 54 | Op. com Direito a Crédito — Tributadas, Interno e Exportação |
| -15 | 53 | Op. com Direito a Crédito — Tributadas e Não-Tributadas no Interno |
| -14 | 52 | Op. com Direito a Crédito — Exclusivamente a Receita de Exportação |
| -13 | 51 | Op. com Direito a Crédito — Exclusivamente a Receita Não-Tributada |
| -12 | 50 | Op. com Direito a Crédito — Exclusivamente a Receita Tributada |
| -11 | 49 | Outras Operações de Saída |
| -10 | 09 | Operação com Suspensão da Contribuição |
| -9 | 08 | Operação sem Incidência da Contribuição |
| -8 | 07 | Operação Isenta da Contribuição |
| -7 | 06 | Operação Tributável a Alíquota Zero |
| -6 | 05 | Operação Tributável por Substituição Tributária |
| -5 | 04 | Operação Tributável monofásica — Revenda a Alíquota Zero |
| -4 | 03 | Operação Tributável com Alíquota por Unidade de Medida |
| -3 | 02 | Operação Tributável com Alíquota Diferenciada |
| **-2** | **01** | **Operação Tributável com Alíquota Básica** |
| **-1** | **00** | **Nenhum** |

34 negativos e 34 positivos, e **as colunas não são simétricas em número** — o
`99` do negativo é `-34`, não `-99`. O par `-1`/`00` é "Nenhum", que é o valor
padrão.

Os dois que o ERP vai usar no dia a dia são `-2` (alíquota básica) e `-1`
(nenhum). O resto depende do que a prefeitura parametrizou no município.

### 3.3 `CodigoTipoRetencao` — 10 valores, de -1 a -10

Página 16:

| código | o que foi retido |
|---|---|
| **-1** | PIS/COFINS/CSLL **Não Retidos** |
| -2 | PIS/COFINS Retido |
| -3 | PIS/COFINS Não Retido |
| -4 | PIS/COFINS/CSLL Retidos |
| -5 | PIS/COFINS Retidos, CSLL Não Retido |
| -6 | PIS Retido, COFINS/CSLL Não Retido |
| -7 | COFINS Retido, PIS/CSLL Não Retido |
| -8 | PIS Não Retido, COFINS/CSLL Retidos |
| -9 | PIS/COFINS Não Retidos, CSLL Retido |
| -10 | COFINS Não Retido, PIS/CSLL Retidos |

`-1` é o padrão. O exemplo do manual na página 13 combina `-2` com `-5`.

### 3.4 O aviso que é endereçado a quem integra

O `adequacao-de-regras-de-tributacao-e-retencao-padrao-adn-25-de-maio-de-2026.pdf`
tem uma seção final que é dirigida a este projeto:

> **ATENÇÃO DESENVOLVEDORES E INTEGRADORES (WEBSERVICE):** Recomendamos a revisão
> imediata do **mapeamento (de/para)** de envio das tags de impostos e retenções no
> seu ERP para evitar rejeições ou cálculos incorretos na base do IBS/CBS.

E as duas regras:

1. **"Os valores informados nos campos de PIS e COFINS passam a ser utilizados
   exclusivamente para fins de redução da base de cálculo do IBS e CBS."**
2. **"Os valores correspondentes às retenções de PIS e COFINS não devem mais ser
   enviados separadamente. Eles devem ser somados à CSLL e o valor total
   (PIS + COFINS + CSLL) deve ser informado unicamente no campo CSLL."**

**O que eu tinha feito estava errado.** Mandava `ValorPis`, `ValorCofins` e
`ValorCsll` um a um. Isso **passa na validação de XSD** — as três tags existem,
aceitam número — e produz o líquido errado, com as retenções contadas duas vezes.
Não é erro de formato, então a recusa chega como divergência de apuração.

Agora está em `NfseNacionalMapeamentoTributacao`:

| o que o ERP tem | para onde vai |
|---|---|
| retenção PIS | soma em `ValorCsll` |
| retenção COFINS | soma em `ValorCsll` |
| retenção CSLL | soma em `ValorCsll` |
| redução de base do IBS/CBS por PIS | `ValorPis` |
| redução de base do IBS/CBS por COFINS | `ValorCofins` |
| **ISSQN** | **nos campos próprios, fora do mapeamento** |
| INSS, IRRF, outras | continuam separados; o aviso não os cita |

As tags `ValorPis` e `ValorCofins` não sumiram: **mudaram de significado.** Eram
retenção, agora são redução de base do IBS/CBS.

### 3.5 O ISSQN é municipal, e a pergunta do que ele faz no líquido

O agrupamento é **federal**. PIS, COFINS e CSLL são tributos federais; o ISSQN é
municipal e a NT 007/2026 não o toca. Ele fica em `ValorBaseCalculoISSQN`,
`AliquotaISSQN`, `ValorISSQNCalculado`, `ValorISSQNRecolher`, e a retenção em
`ISSQNRetido`.

E o `ValorLiquido` **não é recalculado por esta API**, porque o material não
diz qual é a conta:

| perfil | `ValorLiquido` | serviços − deduções |
|---|---|---|
| Normal | 1000.00 | 1000.00 |
| ConstrucaoCivil | 1000.00 | 1000.00, e o `ValorDeducaoConstCivil=100` **não** baixou |
| Imune | **900.00** | 1000.00 |
| Isento | **100.00** | 1000.00 |
| SN ME EPP | **970.00** | 1000.00 |

Três dos dez têm líquido sem fórmula nenhuma. E o dado que fecha o caso:
**`ValorISSQNRecolher` é 0 nos dez, e PIS, COFINS, INSS, IRRF, CSLL e
OutrasRetenções são 0 nos dez.** Nenhum dos exemplos exercita nenhuma retenção —
três têm `ISSQNRetido=1` e mesmo assim `ValorISSQNRecolher=0`.

Inventar a conta seria o pior erro possível: `ValorLiquido` está em
`tsDescricao` e aceita qualquer número, então uma conta errada produz nota errada
em todas as emissões **sem nenhuma recusa**. O ERP manda o líquido que ele já tem,
e o que a API verifica é a aritmética que o material confirma: base × alíquota =
`ValorISSQNCalculado`.

**Pergunta para a prefeitura, quando houver canal:** como o ISSQN e as retenções
entram no `ValorLiquido`? Nenhum exemplo responde, porque nenhum tem retenção.

### 3.6 A terceira regra do aviso, que é de relatório

> o grupo de informações que antes era impresso/exibido como **"Retenções de
> impostos"** foi renomeado para **"Tributação federal"**

Isso é do PDF da nota do AGILIBlue, não do WebService, e afeta o relatório da
nota no ERP — não o XML.

### 3.7 O que a leitura por imagem custou

O `tesseract` sozinho **não serve** para estas tabelas. Com `--psm 6` ele acerta a
segunda coluna de códigos e **perde a primeira** — os negativos viram `-` ou `-1`.
As três colunas viram uma linha só, e a coluna de código negativo desaparece.

O que funciona:

- renderizar a **400 DPI e em tons de cinza** (a 300 os dígitos saem ruins; em
  cinza o antialias do PDF some e o contraste melhora)
- pedir o **TSV** (`tessedit_create_tsv=1`), que traz a caixa de cada palavra — é
  o que permite separar colunas
- `--oem 1` (LSTM) em vez do motor legado

O que ainda não resolvi: reconstruir as colunas por coordenada, porque a coluna de
código tem x fixo na página e a tentativa por linha parte as frases. **O que
resolveu foi renderizar a página e ler a imagem**, e a tabela acima saiu de lá. O
script `scripts/ocr-tabela-pdf.py` fica no repo com o que foi tentado, porque a
tabela do ISSQN 2026 tem 88 páginas e vai precisar disso.

## 4. A homologação rodou: o que a prefeitura respondeu

`POST https://nfse.rondonopolis.mt.gov.br/api/GerarNfse?homologacao=true` com o
XML assinado pela API. **HTTP 202**, `GerarNfseResposta` com 10 recusas de
negócio. Nada foi gravado — é homologação.

### 4.1 O content-type, que era o bug que escondia tudo

| Content-Type | resposta |
|---|---|
`text/xml` (o que eu mandava) | **HTTP 500**, 53 bytes: `ALERTA: XML inválida ou não informada corretamente.`
`application/soap+xml` | HTTP 500: `ERRO: O sistema identificou uma inconsistência`
`application/octet-stream` | HTTP 500: idem
`multipart/form-data` | HTTP 500: idem
**`application/xml; charset=utf-8`** | **HTTP 202**, com a `GerarNfseResposta` completa |

O `ALERTA` não diz que o problema é o content-type. Com ele, a API inteira
parecia quebrada — e o XML estava certo, o XSD validava, a assinatura estava no
lugar. **Erro de transporte e resposta de negócio são a mesma aparência
enquanto o content-type está errado.**

Detalhe do transporte, que também é o do XSD: a operação inexistente devolve
`404` com mensagem de WCF (`No HTTP resource was found that matches the request
URI`), e `GET` devolve `405`. O endpoint existe e é `POST`.

### 4.2 As dez recusas, e o que cada uma exige

| código | o que a prefeitura diz | quem resolve |
|---|---|---|
**E21** | "A chave digital informada não é válida para o econômico prestador do serviço." | **A prefeitura.** Rondonópolis aceita `ChaveDigital`, **não** o `dsig:Signature` |
**E265** | "Não existe competência aberta para o prestador de serviço que contemple a data de emissão do RPS." | **A prefeitura.** A competência do mês tem que estar aberta |
**E16** | "Inscrição municipal do prestador do serviço não informada." | **A prefeitura.** Obrigatória, apesar do `minOccurs="0"` |
**E312** | "A tag `CodigoAtividadeEconomica` foi informada indevidamente." | **Código.** Rondonópolis parametriza `ItemLei116AtividadeEconomica` |
**E293** | "A tag da atividade econômica `ItemLei116AtividadeEconomica` não foi informada ou está sem conteúdo." | **Código.** Confirma o E312 |
**E358** | "É obrigatório preencher o código do NBS." | **Código** |
**E88** | "O valor da base de cálculo do ISSQN deve ser superior a R$ 0,00." | **Código.** Faltou `ValorBaseCalculoISSQN` no XML de teste |
**E303** | "Não existe cadastro de alíquota do ISSQN para a atividade econômica por item da lei 116/2003 informada na tag `ItemLei116AtividadeEconomica`." | **A prefeitura.** O item usado tem que estar parametrizado |
**E26** | "Modelo de RPS informado não encontrado na base de dados para o prestador do serviço." | **A prefeitura.** Série e tipo do RPS cadastrados |

### 4.3 O que isso provou sobre o material

**O `-1` é recusado pelo servidor, e o próprio material usa.**

Enviando o `GerarNfseEnvio - Normal.xml` **sem alterar nada**, com o
content-type certo:

> O elemento 'Codigo' é inválido - O valor '-1' é inválido dependendo do tipo de
> dados 'tsRegimeEspecialTributacao' - Falha na restrição Pattern.

São 8 dos 10 exemplos com `-1` (ver 2.2). A[XSD] e o servidor concordam, e os
exemplos não passam. A favor do XSD: ele é o que o servidor valida. A favor dos
exemplos: são 8 contra 2 e a descrição dos oito é "Nenhum". **Quem decide é a
prefeitura**, e a pergunta agora tem resposta de um lado só.

### 4.4 O `ItemLei116AtividadeEconomica` confirma o formato

O **E312** diz que `CodigoAtividadeEconomica` foi informada *indevidamente*, e o
**E293** diz que `ItemLei116AtividadeEconomica` não foi informada. Rondonópolis
usa a segunda. **O formato certo é `POS_REFORMA`**, que é o default da API.

### 4.5 A Inscrição Municipal: eu estava errado

O XSD declara `InscricaoMunicipal` com `minOccurs="0"`, e eu tirei a exigência da
validação local por causa disso. A prefeitura respondeu **E16**.

**`minOccurs="0"` no XSD não significa dispensável.** O schema permite omitir; o
servidor exige. Eu devia ter ligado a validação local de volta ao ver o `E16`,
e não ao ver o `minOccurs`. A validação local está como estava, com o `E16` na
mensagem.

### 4.6 O que falta, em ordem

**Vem da Prefeitura de Rondonópolis** (e só ela tem):

1. **`ChaveDigital`** do prestador — MD5 de 32 caracteres. Sem ela o servidor
   responde E21, e **a assinatura não é substituto** para este município
2. **`Inscrição Municipal`** do prestador
3. **Competência aberta** para o mês de emissão
4. **Alíquota cadastrada** para o item da LC 116 que o ERP vai usar
5. **Modelo de RPS cadastrado** — série e tipo

**Vem do ERP, e já está no código:**

6. `ItemLei116AtividadeEconomica` — o ERP precisa do item da LC 116 do prestador
7. `CodigoNBS` — o ERP precisa do NBS
8. `ValorBaseCalculoISSQN` — o ERP sempre mandou

## 5. O que foi reaproveitado, e como

O dono do projeto mandou **não linkar, e sim escrever em cima do que já
existe**. O que ficou:

| arquivo | origem | o que mudou |
|---|---|---|
| `RespostaNfsePadrao` | copiado da API de SP | **só o texto**, que dizia São Paulo. A estrutura dos 8 campos é o contrato que o ERP lê, e não muda |
| `NfseNacionalSigner` | copiado e reescrito | a cadeia de 86 posições foi removida (é de SP); entrou `assinarDeclaracao`, que assina o documento e deposita a `Signature` dentro de `IdentificacaoPrestador` |
| `NfseNacionalCertificadoService` | copiado | quase nada: carrega o A1 do `.pfx`, que é igual nos dois |
| `NfseNacionalValidadorXsd` | novo | o `SchemaFactory` de SP validava contra o XSD da prefeitura; este valida contra o do dono |

**O que não foi reaproveitado, e por quê:** o `nfse-client` de terceiro, que
estava em `microservices/nfse` e é o cliente oficial da nacional. Ele é Java 8 e
usa `javax.xml.bind` nos DTOs — que o Java 11 removeu do JDK e o Spring Boot 3
trocou por `jakarta.xml.bind`. E arrasta `jasperreports 6.21.3`, que declara o
`openpdf` com o `groupId` errado (`com.github.librepdf.openpdf` em vez de
`com.github.librepdf`), e a construção falha em `Could not collect
dependencies`. O raciocínio está no `pom.xml`.

---

## 6. Como rodar

```bash
cd src/main/resources/microservices/nfse-nacional-api

# testar
mvn test

# subir
mvn spring-boot:run
```

O que precisa estar em `application.yml` para uma chamada de verdade:

```yaml
nfse:
  nacional:
    unidade-gestora: "<CNPJ da Prefeitura de Rondonopolis>"   # <- falta
    cnpj-prestador: "14375732000170"
    inscricao-municipal-prestador: "..."
    homologacao: true          # nao gravar
    chave-digital: ""          # vazio = assina com o certificado
    certificado-caminho: "<caminho do .pfx A1>"
    certificado-senha: "..."
```

Com `homologacao: true` e a `UnidadeGestora` errada, a prefeitura **valida e não
grava** — dá para acertar o cadastro sem sujar a série. Com `false`, grava de
verdade.

O `GET /api/nfse-sp/status` avisa antes: `UnidadeGestora` ausente, duas
autenticações configuradas ao mesmo tempo, nenhuma configurada, e quando está em
produção.

---

## 7. Referência rápida das classes

| classe | o que faz |
|---|---|
| `NfseNacionalOperacoes` | as 8 operações, a base e o parâmetro de homologação |
| `NfseNacionalDeclaracaoBuilder` | monta o `GerarNfseEnvio`, na ordem do XSD |
| `NfseNacionalCancelamentoBuilder` | monta o `CancelarNfseEnvio` |
| `NfseNacionalSigner` | `dsig:Signature` no lugar do `ChaveDigital` |
| `NfseNacionalCertificadoService` | carrega o A1 do `.pfx` |
| `NfseNacionalValidadorXsd` | valida contra o XSD do dono (formato ANTES), com o import do dsig |
| `NfseNacionalClient` | POST de XML em mTLS quando há certificado |
| `NfseNacionalRespostaParser` | lê `GerarNfseResposta`; `Nfse` é emissão, `ListaMensagemRetorno` é recusa |
| `NfseNacionalRegras` | validação local, antes de gastar certificado |
| `NfseNacionalService` | a ordem: valida, monta, XSD, assina, envia, traduz |
| `RespostaNfsePadrao` | o contrato de 8 campos que o ERP lê |
