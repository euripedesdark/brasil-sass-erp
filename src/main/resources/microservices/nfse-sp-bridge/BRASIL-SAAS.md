# NFS-e São Paulo — o que foi validado de verdade

> **Este bridge é a implementação de referência.** A API em Java que o
> substitui está em [`../nfse-sp-api`](../nfse-sp-api), com o mesmo escopo e as
> mesmas regras. O Ruby continua no ar de propósito: foi ele que validou as
> regras contra a prefeitura primeiro, e serve de oráculo para comparar o XML
> gerado — foi essa comparação que pegou os bugs de assinatura e de namespace
> da porta Java.
>
> **O que só existe aqui:** o registro do que foi *testado*, com as mensagens
> literais que a prefeitura devolveu. Se este arquivo sair junto com o
> bridge, perde-se a parte que não se deduz do código.

Este arquivo registra o que foi **testado contra o WebService de produção** da
prefeitura de São Paulo, com o certificado A1 da SrvCloud, em 25/09/2026.

Não é documentação copiada do manual: é o resultado de emitir e cancelar uma
NFS-e real. Os erros citados são as respostas literais que a prefeitura
devolveu.

---

## Resumo do que funciona

| Endpoint | Status | Observação |
|---|---|---|
| `POST /consulta_cnpj` | **200** | IM `2130033`, `emite_nfe: true` |
| `POST /emitir_rps` | **200** | NFS-e nº 9 e nº 10 emitidas de verdade |
| `POST /cancelar_nfse` | **200** | Ambas canceladas, reconfirmado pelo alerta 1301 |

Nenhuma das duas notas de teste está em aberto: a prefeitura respondeu
`"NFS-e já cancelada em 25/09/2026."` ao tentar cancelar de novo.

---

## As quatro decisões que destravaram a integração

### 1. O certificado A1 exige o provider legacy do OpenSSL

O `.pfx` foi emitido com **RC2-40-CBC**, que o OpenSSL 3 removeu do provider
default. Sem isso o serviço inteiro não emitia nada:

```
PKCS12_parse: unsupported (Global default library context,
Algorithm (RC2-40-CBC : 0), Properties ())
```

**Java não é afetado** (o SUN Provider ainda aceita), por isso a tela
`/fiscal/certificados` do ERP funciona. Só o Ruby/OpenSSL precisa:

```bash
export OPENSSL_MODULES=/usr/lib/x86_64-linux-gnu/ossl-modules
```

Só isso **não basta**: o `app.rb` carrega o provider explicitamente no boot
(`OpenSSL::Provider.load`), e a gem chama `load_legacy_provider` no
`Client#initialize`. O caminho do módulo varia por distro — procure com
`find /usr/lib -name legacy.so`.

> Solução definitiva: pedir reemissão à AC com **AES** em vez de RC2/3DES.

### 2. A empresa é do Simples Nacional → leiaute **1**, obrigatoriamente

A gem vinha com `XSD_VERSION = 2` fixo. A prefeitura recusou:

> **641** — *"Contribuinte cadastrado como Simples Nacional na data informada.
> Deverá ser utilizado o leiaute 1."*

A escolha do leiaute **não é do integrador**: a prefeitura compara com o
cadastro dela. Agora é configurável:

```bash
NFSE_SP_XSD_VERSION=1   # Simples Nacional
NFSE_SP_XSD_VERSION=2   # regime normal (IBS/CBS, LC 214/2025)
```

A escolha também muda **os campos** e **a assinatura** (ver abaixo).

### 3. Os XSDs da pasta são do leiaute 1 — e servem para esta empresa

`NFSe-SaoPaulo-SP/esquema nota servico sp/*.xsd` traz `Versao` com
`fixed="1"`. Como a empresa é do Simples Nacional, **esses schemas são os
corretos** e o gem não deve mandar os campos de IBS/CBS.

O que é do leiaute 2 e **não** deve ser enviado aqui: `NCM`, `NBS`,
`cLocPrestacao`, `cPaisPrestacao`, `IBSCBS`, `ExigibilidadeSuspensa`,
`ValorInicialCobrado`/`ValorFinalCobrado`, `ValorMulta`, `ValorJuros`,
`ValorIPI`. Enviar `ValorServicos` num leiaute 2 também quebra, e
`ValorServicos` é obrigatório no leiaute 1 (`minOccurs=1`).

### 4. A assinatura do RPS tem geometria diferente em cada leiaute

O erro **1206** devolve a string que a prefeitura verificou, o que torna o
diagnóstico direto:

> **1206** — *"Assinatura Digital do RPS incorreta - String verificada
> (02130033BC   000000000001202609251NN00000000000010000000000000000000101100086946749120)"*

Duas divergências na gem:

| Posição | Leiaute 1 | Leiaute 2 | O que a gem fazia |
|---|---|---|---|
| 1 — IM do prestador | **8** posições | 12 posições | sempre 12 |
| 8 — valor | **Valor dos Serviços** | Valor Inicial/Final Cobrado | sempre Inicial/Final |

Além disso, a posição 7 (ISS Retido) comparava `valor.to_s === true`, que é
sempre falso: **uma nota COM ISS retido era assinada como se não tivesse**.
Agora trata `true`, `"true"` e `1`.

---

## Regras de negócio da prefeitura (o que o XSD não valida)

Todas descobertas emitindo nota. São o que trava depois que o XML passa.

| Código | Regra |
|---|---|
| **306** | Código de serviço inexistente na tabela municipal |
| **307** | Código existe, mas não está cadastrado **para este prestador** |
| **223** | RPS já processado antes; a NF-e dele está cancelada — o número do RPS não retrocede |
| **640** | `ValorInicialCobrado` foi descontinuado; use só `ValorFinalCobrado` |
| **1223** | Serviço tributado em SP ou exportação **não** informa `MunicipioPrestacao` |
| **1301** | NFS-e já cancelada (usado para confirmar o cancelamento) |

O **307** desapareceu com o código certo: ver abaixo.

### Códigos de serviço — RESOLVIDO

O código do ERP (`bc_fis_servico_lc116`) usa o formato **nacional** da
LC 116 (`01.07`). **A prefeitura de São Paulo não aceita esse formato.**

O mapeamento está em [`codigos-servico-sp.json`](codigos-servico-sp.json) —
314 pares, extraídos de `NFSe-SaoPaulo-SP/Cod.-de-servico-SP-x-Campinas.pdf`
(o nome do arquivo engana: o conteúdo é a correlação da Tabela de Serviços da
Prefeitura de São Paulo — coluna "COD. SERV. PREFEITÃO SÃO PAULO", 4 dígitos —
contra o código nacional `XX.XX`).

Para serviços de TI:

| LC 116 | Código SP | Serviço |
|---|---|---|
| 01.01 | 2660 | Análise e desenvolvimento de sistemas |
| **01.02** | **2668** | **Programação** |
| 01.03 | 2684 | Processamento, armazenamento ou hospedagem de dados |
| 01.04 | 2692 | Elaboração de programas de computadores (software) |
| 01.05 | 2800 | Licenciamento ou cessão de direito de uso |
| 01.06 | 2881 | Assessoria e consultoria em informática |
| **01.07** | **2919** | **Suporte técnico em informática** |
| 01.08 | 2935 | Planejamento/manutenção de páginas eletrônicas |
| 01.09 | 2961-2963 | Disponibilização de conteúdo pela internet |

**2919 (LC 116 01.07) é o que está cadastrado para a IM 2130033** — confirmado
em 25/09/2026: a NFS-e nº 11 foi emitida **sem** o alerta 307, enquanto a
nº 9 com o código `1023` saiu com ele.

Um mesmo item da LC 116 pode ter **vários** códigos em SP; use o que estiver
cadastrado para o prestador.

> O formato aceito é **4 dígitos**. `107` e `010701` são recusados pelo XSD
> (`tpCodigoServico` é 4-5) e `0107` passa no XSD mas responde **306
> inexistente** — esse código não existe na tabela municipal.

---

## Campos que a gem não emitia (bugs corrigidos)

| Correção | Efeito |
|---|---|
| `NumeroEncapsulamento` movido para antes de `ValorTotalRecebido` | o XSD é `<xs:sequence>`; fora de ordem a prefeitura recusa |
| `Simple#add_tag_to_xml` ignora string vazia | `<InscricaoMunicipalTomador></...>` vazio era rejeitado |
| `Hash.from_xml` substituído por parser próprio | o método **não existe** no ActiveSupport 8; a resposta nem era lida |
| nó de retorno buscado sem diferenciar caixa | a prefeitura devolve `<RetornoConsultaCNPJ>`, não `<retorno_consulta>` |
| container de filho único não vira array | `<Cabecalho><Sucesso>true</...>` virava `["true"]` e quebrava `success?` |
| `with_indifferent_access` removido do `Complex` | dependência do ActiveSupport que a gem não declara |

---

## Como rodar

```bash
cd src/main/resources/microservices/nfse-sp-bridge
./run-bridge.sh          # porta 4567
```

Variáveis:

| Variável | Padrão | Papel |
|---|---|---|
| `NFSE_SP_CERT_PATH` | `/etc/brasil-saas/certs/sp_cert.p12` | caminho do A1 |
| `NFSE_SP_CERT_PASS` | — | senha do A1 (**obrigatória**) |
| `NFSE_SP_XSD_VERSION` | `1` | 1 = Simples Nacional, 2 = IBS/CBS |
| `OPENSSL_MODULES` | `/usr/lib/x86_64-linux-gnu/ossl-modules` | provider legacy |

Exemplo:

```bash
curl -X POST http://127.0.0.1:4567/consulta_cnpj \
  -H 'Content-Type: application/json' \
  -d '{"cnpj":"00000000000191"}'
```

```json
{"success":true,
 "retorno":{"cabecalho":{"sucesso":"true"},
            "detalhe":{"inscricao_municipal":"2130033","emite_nfe":"true"}}}
```

---

## Pagamento parcelado

`PagamentoParceladoAntecipado` **não é mais usado** desde a versão 3.3.6
(14/05/2026) e foi comentado no `tp_rps.rb`. Manter assim.

## O que não dá para testar daqui

A gem expõe só quatro métodos — `sync_envio_rps`, `sync_cancelamento_nfe`,
`sync_consulta_cnpj` e `sync_teste_envio_lote_rps`. **Não há consulta de NFS-e
nem consulta por período**, então a confirmação de status foi feita
reenviando o cancelamento e lendo o alerta 1301. Se o ERP precisar
pesquisar notas emitidas, isso ainda não existe aqui.
