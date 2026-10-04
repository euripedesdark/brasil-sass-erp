> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

# MAPA DOS MICROSSERVICOS FISCAIS

> 29/09/2026 · levantamento **sem implementar nada**. Pergunta que responde:
> existe API pronta para NFC-e? O que dá para reaproveitar? O que já está
> padronizado?
> Pasta: `src/main/resources/microservices/` · 25 diretórios, ~200 MB.

> Já existe um `INVENTARIO.md` de 26/09 nessa mesma pasta, que classifica as
> 22 pastas em "fonte, especificação ou código inerte". **Este documento não
> repete aquilo**: entra no que faltava, que eram os endpoints, os payloads, a
> autenticação, os fluxos e a pergunta específica do NFC-e.

---

## 1. RESPOSTA CURTA

**Não existe API de NFC-e. Não existe nada pronto para varejo.**

O que existe:

| | |
|---|---|
| Material didático de NFC-e | `Java_NFe` — 10 arquivos, `NFCeUtil`, DANFE em Jasper |
| Tabelas de NFC-e no banco | `bc_fis_nfce` (17 colunas) e `bc_fis_nfce_item`, **58 linhas cada** |
| Entidades Java | `Nfce.java` e `NfceItem.java` |
| Controller de NFC-e | **NENHUM** |
| API de emissão de NFC-e | **NENHUMA** |
| Padrão de emissão que dá para copiar | `nfse-sp-api` + `NfseEmissaoService` |

A resposta curta é: **a parte de dados e de modelo já existe, a parte de serviço
não existe.** Reaproveitar o que já está modelado e seguir o padrão do NFS-e é
o caminho.

---

## 2. A PERGUNTA ESPECIFICA: NFC-e JA EXISTE ALGUM VEZES?

### 2.1 No material de microsservicos

Sim, como **material de referência**, não como serviço:

```
microservices/Java_NFe/src/main/java/br/com/swconsultoria/nfe/util/NFCeUtil.java
microservices/Java_NFe/src/main/resources/jasper/nfce/danfce.jasper
microservices/Java_NFe/src/main/resources/jasper/nfce/danfce.jrxml
microservices/Java_NFe/src/main/java/br/com/swconsultoria/nfe/schemas/TTribNFCe.java
```

`Java_NFe` é a API open-source da **Sw Consultoria**, com 367 arquivos Java e
schemas da SEFAZ. Tem o utilitário de NFC-e e o **DANFE em Jasper**, que é o
cupom que o caixa entrega ao cliente. `danfce.jrxml` é editável; o `.jasper` é
o compilado.

O `INVENTARIO.md` de 26/09 classifica `Java_NFe` como **"material didático, usar
com cuidado"**. Confirmedo: serve para ler como a SEFAZ modela NFC-e, não para
rodar.

### 2.2 No ERP

O ERP tem **entidades e tabelas, sem serviço**:

| Camada | Estado |
|---|---|
| `bc_fis_nfce` | existe, 17 colunas, **58 linhas** |
| `bc_fis_nfce_item` | existe, **58 linhas** |
| `fiscal/model/Nfce.java` | existe |
| `fiscal/model/NfceItem.java` | existe |
| `fiscal/repository/NfceRepository.java` | existe |
| `fiscal/repository/NfceItemRepository.java` | existe |
| **`fiscal/controller/NfceController.java`** | **NÃO EXISTE** |
| **`fiscal/service/` de NFC-e** | **NÃO EXISTE** |

As 58 linhas são seed: `numero` 753, status `SEED`, chave `SEED CHAVE_ACESSO 1`,
protocolo `SEED PROTOCOLO 1`. É a mesma assinatura de dado sintético do resto do
ERP.

### 2.3 As colunas de `bc_fis_nfce`

```sql
id, uuid, empresa_id, pessoa_id, numero, serie, chave_acesso,
data_emissao, status, protocolo, valor_total, xml,
created_at, updated_at, created_by, updated_by, deleted_at
```

Isso **é** o modelo completo de um documento fiscal-authorizado: `chave_acesso`
(44 dígitos da SEFAZ), `protocolo` (o retorno), `xml` (o documento assinado) e
`status`. O que falta para NFC-e é a mesma coisa que falta em qualquer modelo
fiscal completo: a tabela de **itens com CFOP, NCM, CST, origem, unidade e
valor** — e essa existe, `bc_fis_nfce_item`, que ainda não li coluna a coluna.

**Conclusão:** reaproveitar `bc_fis_nfce` + `bc_fis_nfce_item` + as entidades
Java. Não criar tabelas novas. Criar serviço e controller.

---

## 3. O PADRAO DO ECOSSISTEMA: COMO EMISSAO FUNCIONA HOJE

Este é o padrão a seguir. Medido em `NfseEmissaoService` e no `nfse-sp-api`.

### 3.1 A divisao de responsabilidade

```
ERP (Tomcat, :8080)
  └── NfseEmissaoService
        │  RestClient, baseUrl = http://127.0.0.1:4567/api/nfse-sp
        │  timeout 60.000 ms (configurável)
        ▼
      nginx :4567  ── failover ──┐
        │                       │
        ▼ (primário)            ▼ (se cair)
  nfse-sp-api :4568          nfse-sp-bridge :4569
  (Java, é o que roda)       (Ruby, fallback)
```

O ERP **não fala com a SEFAZ**. Fala com um microsserviço local, que fala com a
prefeitura. Isso é o que permite trocar a implementação sem mexer no ERP: o
commit `c055d875` ("devolve inscricao_municipal, que o watchdog exige") mudou o
comportamento do microsserviço sem tocar em nenhuma tela.

### 3.2 Os endpoints do `nfse-sp-api`

| Método | Rota | O que faz |
|---|---|---|
| `GET` | `/api/nfse-sp/status` | vivo? o watchdog consulta antes de decidir trocar |
| `POST` | `/api/nfse-sp/emitir-rps` | emite a RPS |
| `POST` | `/api/nfse-sp/cancelar` | cancela por protocolo |
| `POST` | `/api/nfse-sp/consulta-cnpj` | cadastro de terceiros |

Quatro verbos, um prefixo de módulo, um controller. É esse o padrão de
superfície de um microsserviço fiscal aqui.

### 3.3 Os endpoints do ERP para NFS-e

`/api/fiscal/nfse` — 8 endpoints, e o desenho é o que uma NFC-e deve copiar:

| Endpoint | Papel |
|---|---|
| `POST /emitir` | dispara a emissão |
| `POST /{id}/cancelar` | cancela |
| `GET /{id}/retornos` | o que a prefeitura respondeu |
| `GET /{id}/retornos/{retornoId}/bruto` | resposta crua, para auditoria |
| `GET /retornos/recusas` | fila de recusa a resolver |
| `GET /retornos/para-conferer` | retorno pendente de conferência humana |
| `GET /{id}/xml` | o XML assinado |
| `GET /{id}/pdf` | o documento para o cliente |

O par **`/retornos/para-conferer` + `/retornos/recusas`** é o mais
interessante: a emissão de documento fiscal **não é síncrona nem confiável**, e o
sistema tem uma fila de retornos que exigem decisão humana. Uma NFC-e que
ignore isso vai travar o caixa quando a SEFAZ estiver fora.

### 3.4 Autenticação

**O `nfse-sp-api` não tem autenticação.** Medido: nenhum `SecurityFilterChain`,
nenhuma `ApiKey`, nenhum header de autorização na pasta. O serviço escuta em
`0.0.0.0:4568` e a proteção vem do firewall.

Para NFC-e isso **não pode ser repetido**: uma API que emite documento fiscal
para a SEFAZ tem que ser autenticada, mesmo que só por rede.

### 3.5 Como o ERP configura a ponte

```java
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}") String url,
@Value("${brasil-saas.fiscal.nfse.inscricao-municipal:2130033}") String inscricaoMunicipal,
@Value("${brasil-saas.fiscal.nfse.serie-rps:BC}") String serieRps,
@Value("${brasil-saas.fiscal.nfse.timeout-ms:60000}") long timeoutMs
```

Configuração por property, com default. Timeout explícito. Inscrição municipal
e série **não são fixas no código**, vêm de configuração — porque são por
empresa. Uma NFC-e que maximise os dois dígitos do CNPJ, o número do terminal e
o código do NFC-e precisa do mesmo cuidado: são por empresa e por instalação.

---

## 4. AS OUTRAS APIS FISCAIS

| Pasta | O que é | Roda? | Reaproveitar |
|---|---|---|---|
| `nfse-sp-api` | API Java, emissão de RPS em São Paulo capital | **sim**, :4568 | o padrão de superfície |
| `nfse-sp-bridge` | bridge Ruby, fallback quando a API Java cai | **sim**, :4569 | o padrão de failover |
| `nfse-failover` | failover em Ruby | **inativo** desde 26/09 | não, substituído pelo nginx na 4567 |
| `nfse-watchdog` | detecta e troca a implementação | **sim** | o contrato de saúde |
| `nfse` | NFS-e **nacional** (padrão nacional, não SP) | não | outro web service, não substitui o de SP |
| `nfe` | NFS-e nacional, 2.178 arquivos Java | não | material de referência |
| `nfse-nacional-api` | API do NFS-e nacional, 22 arquivos | não | ver `nfse` |
| `nfse_prefeitura_sp` | gem Ruby, web service real de SP | não (o bridge usa) | implementação real de SP |
| `NFSe-SaoPaulo-SP` | **fonte oficial** da prefeitura | não | fonte, não código |
| `Java_NFe` | API NF-e + NFC-e da Sw Consultoria, 367 arquivos | não | **material didático de NFC-e** |
| `Java_CTe` | API CT-e, 220 arquivos | não | com cuidado |
| `Java_MDFe` | **vazia** | não | não |
| `Java-Efd-Icms` | EFD ICMS, leiaute pré-reforma, 600 arquivos | não | com cuidado |
| `Java-Efd-Contribuicoes` | EFD Contribuições, 457 arquivos | não | com cuidado |
| `PL_MDFe_300b_NT012025_1.05` | **41 XSD oficiais do MDF-e 3.00b** | não | sim, são os esquemas |
| `sped-mdfe` | PHP, mapeia leiaute 3.00a | não | parcialmente |
| `esocial` | app do TST, **5.073 arquivos** | não | não é do ERP |
| `l10n-brazil` | OCA, especificação e tabelas | não | tabelas |
| `boleto-cnab-api` | API de CNAB, 1 arquivo Python | não | ver |

### Varejo: o que existe e é útil

| Recurso | Onde | Estado |
|---|---|---|
| Modelo de dados da NFC-e | `bc_fis_nfce` + `_item` | **pronto, 58 linhas seed** |
| Entidades Java | `Nfce.java`, `NfceItem.java` | prontas |
| DANFC-e (cupom) | `Java_NFe/.../jasper/nfce/danfce.jrxml` | template pronto, precisa adaptar ao layout |
| Utilitário de NFC-e da SEFAZ | `Java_NFe/.../util/NFCeUtil.java` | didático |
| CFOP | `bc_fis_cfop` + `/api/fiscal/cfop` | pronto |
| NCM | `bc_fis_ncm` + `/api/fiscal/ncm` | pronto |
| CEST | `bc_fis_cest` + `/api/fiscal/cest` | pronto |
| Certificado digital | `/api/fiscal/certificados` | pronto |
| Tabela de preço | `bc_ven_tabela_preco` | pronto |
| PDV | `/vendas/pdv` | pronto, validado |

O que **não** existe em lugar nenhum, nem no ERP nem nos microsserviços:

- API de emissão de NFC-e
- Integração com a SEFAZ de **qualquer estado** (o ERP só conversa com a
  prefeitura de SP, via NFS-e)
- Contingência de NFC-e (`epc`/`fsat`/offline) — que NFC-e exige e é a
  parte difícil
- Código de autorização de NFC-e (QR Code, CSC)

---

## 5. O QUE FALTA

| Lacuna | Gravidade |
|---|---|
| **Controller e service de NFC-e** | bloqueia tudo |
| **Integração com a SEFAZ** (não existe para nenhum estado) | bloqueia tudo |
| **Emissão offline / contingência** | exige antes de ir a produção |
| Autorização, assinatura e QR Code | bloqueia tudo |
| `bc_fis_nfce_item` sem CFOP, CST, NCM, origem, unidade e descontos | **bloqueia**: XML não sai
| API de NFC-e sem autenticação seria repetir o defeito da 4568 | segurança |
| Impressão do DANFC-e no layout do ERP | operação |
| Série e código de terminal por empresa | multi-empresa |
| Fila de retornos da SEFAZ, como o NFS-e tem | operação |

## 6. COMO UMA NFC-e DEVERIA SEGUIR OS PADRÕES EXISTENTES

1. **Microsserviço separado, porta própria, talking com o ERP só por HTTP
   interno.** Como `nfse-sp-api` na 4568. O ERP não fala com a SEFAZ.
2. **Prefixo de módulo e quatro verbos**: `/api/nfce/status`, `/emitir`,
   `/cancelar`, `/consulta`. O nome segue o do recurso, como o
   `/api/nfse-sp`.
3. **Autenticação por empresa**, diferente da 4568. Documento fiscal não se
   emite para quem não pediu.
4. **Configuração por property**, não constante: CNPJ, CSC, id de terminal,
   série, URL, timeout.
5. **Fila de retornos**, não resposta síncrona. Reaproveitar o desenho de
   `/retornos/para-conferer` e `/retornos/recusas` do NFS-e.
6. **`GET /{id}/xml` e `GET /{id}/pdf`**, com o PDF do DANFC-e. O ERP já faz
   isso no NFS-e; para NFC-e, o PDF é o cupom.
7. **Gravado nas tabelas que já existem**: `bc_fis_nfce` com `chave_acesso`,
   `protocolo`, `status` e `xml`; ampliar `bc_fis_nfce_item` com as colunas
   fiscais que faltam, em migration, antes da primeira emissão.
8. **Reaproveitar `Java_NFe` para ler o leiaute**, não para rodar. O utilitário
   e o Jasper dão o caminho, a implementação é nova.

---

## 7. COMO VERIFICAR

```bash
cd ~/BrasilCloudERP/src/main/resources/microservices
ls                                    # 25 diretorios
head -40 INVENTARIO.md                # classificacao de 26/09
grep -rl "nfc" -i . --include=*.java # o material didatico
curl -s localhost:4568/api/nfse-sp/status    # o microsservico vivo
cd ~/BrasilCloudERP
ls src/main/java/br/com/brasil_saas/fiscal/controller/   # NfceController nao esta
PGPASSWORD=ALTERE_ME psql -h 127.0.0.1 -U postgres -d brasil-saas \
  -c "\d brasil_saas.bc_fis_nfce" -c "\d brasil_saas.bc_fis_nfce_item"
```

