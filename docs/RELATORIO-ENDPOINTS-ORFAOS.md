# RELATORIO: ENDPOINTS ORFAOS — BASELINE 29/09

> **STATUS DE BASELINE — 30/09/2026:** este relatório é o levantamento de 29/09 e não representa sozinho o estado atual. Depois dele foram adicionados `ComissaoController` (7 endpoints) e `CaixaController` (5 endpoints), ambos já consumidos pelo frontend. A cobertura atual e os contratos corrigidos estão em `docs/auditorias/MAPA_COBERTURA_FRONTEND_ATUAL_2026-09-30.md`.

> 29/09/2026 · por que existe: antes de criar comanda, NFC-e, pré-venda ou
> qualquer fluxo novo, saber se já não existe.
>
> O caso que motivou: as **múltiplas formas de pagamento**. Desenhei a tela na
> cabeça e só depois descobri que `POST /financeiro/titulos/{id}/baixar` já
> suportava baixa parcial. Nenhuma linha de backend foi necessária. Se eu
> tivesse inventado um endpoint de "fechar venda com N formas", teria criado
> um segundo lugar onde a verdade mora.
>
> Tudo aqui é **medido**, não estimado. Como reproduzir: §7.
>
> ## CORRECAO DE 29/09 (leia antes de usar os numeros)
>
> A primeira versao dizia **108 endpoints sem tela**. **Errado.** O detector so
> reconhecia 2 das 4 formas que o frontend chama a API. As outras 2 faziam
> **43 endpoints COM tela parecerem orfaos**.
>
> | Padrao no `services/*.js` | Arquivos | Reconhecido antes? |
> |---|---|---|
> | `api.get('/x')` | 6 | sim |
> | `fetch('/api/x')` | 1 | sim |
> | **`${BASE_URL}/fiscal/cfop`** | **18** | **nao** |
>
> `ApiConfig.API_BASE_URL = '/api'`, e 18 dos 20 servicos montam a URL com
> template literal. Nenhum deles e' orfao.
>
> **O numero correto e' 65, nao 108.** Isso invalidou duas conclusoes que eu dei
> como verdade: a "importacao de NF-e pronta, so falta a tela" e a "folha com
> tela". **As duas tem tela.** Ver §2, §4.1 e §8.

---

## 1. RESUMO

| | |
|---|---|
| Controllers | 88 |
| Endpoints | **466** |
| Endpoints com tela | **401** |
| **Endpoints sem tela** | **65** |
| Controllers com ao menos 1 endpoint sem tela | 12 |
| **Controllers inteiros sem tela** | **7** |
| Órfãos com tabela rastreada | **58** |
| Órfãos que não tocam tabela via repository | 7 |
| **Órfãos com tabela SEM migration** | **0** |
| **Órfãos fiscais** (prefixo `/api/fiscal`) | **5** |

Duas leituras que importam:

1. **Nenhum endpoint órfão trabalha contra uma tabela que não tem migration.**
   Toda tabela que um endpoint sem tela toca foi criada por um `V*.sql` do
   Flyway e está aplicada. Isso elimina uma classe inteira de dúvida: não há
   endpoint órfão apontando para schema inexistente.
2. **"Sem tela" não é "quebrado".** A API responde 200, a regra de negócio
   existe, o que falta é a interface. Vários já são de produção.

---

## 2. A PERGUNTA QUE DECIDE: "ele ja resolve o que estou construindo?"

| Vou construir | Endpoint orfao? | Service? | Ja resolve? | Decisao |
|---|---|---|---|---|
| **Comanda** (bar/restaurante) | nao | nao | nao | **Construir.** Zero ocorrencia de "comanda" ou "mesa" no ERP inteiro |
| **Pre-venda / reserva** | nao | nao | nao | **Construir.** Zero ocorrencia de "prevenda"/"pre-venda" |
| **Sessao de caixa** | nao | nao | nao | **Construir.** `bc_fin_caixa` e' a tabela do caixa; sessao nao existe |
| **Orcamento de venda** | parcial | parcial | **PARCIAL** | **Reaproveitar.** `confirmar()` aceita so `tipo=ORCAMENTO` e valida estoque. Falta tela |
| **Multiplas formas** | sim | sim | **SIM** | **Reaproveitado** (2B.2). Backend ja suportava baixa parcial |
| **Fechamento de venda** | sim | sim | **SIM** | **Reaproveitado** (2B.1). `POST /financeiro/titulos/{id}/baixar` |
| **Importacao de NF-e (entrada)** | **nao — TEM tela** | 378 linhas, 0 stub | **SIM** | **Ja entregue.** `EntradaNota.jsx` + `ImportarNotaXml.jsx` existem. Falta **carga** |
| **NFC-e** | nao | parcial | nao | **Ver §4.2** |
| **Tributacao (CFOP/CEST/NCM/imposto/ISSQN)** | **SIM, 9 eps** | parcial | **parcial** | **Reaproveitar as tabelas.** Ver §4.3 |
| **MDF-e / CT-e / EFD** | **SIM, 5 eps** | **SIM** | **NAO** | Sao **stub de layout**, nao emissao. Ver §4.4 |

### Como verifiquei os "nao"

```bash
cd ~/BrasilCloudERP/src/main/java/br/com/brasil_saas
grep -rliE "comanda|\bmesa\b" .        # (nenhum)
grep -rliE "prevenda|pre-venda" .     # (nenhum)
grep -rliE "sessao" . | grep -i caixa # (nenhum)
```

Comanda e pre-venda sao construcoes limpas. Sem risco de colisao.

---

## 3. OS 17 CONTROLLERS INTEIROS SEM NENHUMA TELA

| Pacote | Controller | Eps | Service | Prefixo | Tabela | Migration |
|---|---|---|---|---|---|---|
| ia | `PromptController` | **12** | `PromptService` | `/api/ia/prompts` | `bc_ia_prompt` | `V52` |
| core | `UsuarioAdminController` | **10** | `ModuloAcessoService,PermissionService,[repo direto]` | `/api/superadmin/usuarios` | `bc_core_empresa`, `bc_core_modulo`, `bc_core_perfil`, `bc_core_usuario`, `bc_core_usuario_modulo` | `V2`, `V91` |
| bi | `ReportController` | **9** | `ReportService` | `/api/bi/reports` | `bc_bi_report`, `bc_bi_report_parameter` | `V48` |
| ia | `ChatMensagemController` | **7** | `ChatMensagemService` | `/api/ia/mensagens` | `bc_ia_chat_mensagem`, `bc_ia_chat_sessao` | `V52` |
| rh | `FolhaPagamentoController` | **7** | `FolhaPagamentoService` | `/api/rh/folhas` | `bc_fin_titulo`, `bc_rh_folha` | `V23`, `V5` |
| fiscal | `EntradaNotaController` | **6** | `EntradaNotaService,NfeImportacaoService` | `/api/fiscal/entradas` | `bc_cad_pessoa`, `bc_cad_produto`, `bc_est_deposito`, `bc_est_movimentacao`, `bc_est_saldo`, `bc_fis_manifestacao`, `bc_fis_nfe`, `bc_fis_nfe_item` | `V22`, `V3`, `V4`, `V59` |
| rh | `CargoController` | **5** | `[repo direto]` | `/api/rh/cargos` | `bc_rh_cargo` | `V23` |
| fiscal | `CestController` | **3** | `[repo direto]` | `/api/fiscal/cest` | `bc_fis_cest` | `V4` |
| core | `EmpresaLogoController` | **3** | `GenericoImagemService,[repo direto]` | `/api/core/empresas` | `bc_core_empresa` | `V2` |
| fiscal | `SefazConsultaController` | **3** | `SefazConsultaService` | `/api/fiscal/sefaz` | _nenhuma via repository_ | — |
| fiscal | `CfopController` | **2** | `[repo direto]` | `/api/fiscal/cfop` | `bc_fis_cfop` | `V4` |
| fiscal | `ImpostoController` | **2** | `[repo direto]` | `/api/fiscal/impostos` | `bc_fis_imposto` | `V4` |
| fiscal | `IssqnController` | **2** | `[repo direto]` | `/api/fiscal/issqn` | `bc_fis_issqn` | `V4` |
| fiscal | `MdfeController` | **2** | `MdfeEmissaoService` | `/api/fiscal/mdfe` | _nenhuma via repository_ | — |
| fiscal | `NcmController` | **2** | `[repo direto]` | `/api/fiscal/ncm` | `bc_fis_ncm` | `V4` |
| fiscal | `SpedEfdController` | **2** | `SpedEfdIcmsService` | `/api/fiscal/sped` | _nenhuma via repository_ | — |
| fiscal | `CteController` | **1** | `CteEmissaoService` | `/api/fiscal/cte` | _nenhuma via repository_ | — |

**7 dos 17 sao fiscais.**

`CestController`, `CfopController`, `NcmController`, `ImpostoController` e
`IssqnController` **nao tem service**: vao direto no repository. Sao CRUD de
tabela de cadastro — os 11 endpoints mais baratos de expor do sistema inteiro,
porque a tela e' a unica coisa que falta.

---

## 4. OS CASOS QUE MERECEM DECISAO

### 4.1 Importacao de NF-e: completa, com tela — nao era orfa

**Correcao.** Escrevi aqui "6 endpoints orfaos, e o maior service fiscal pronto,
so falta a tela". **A tela existe:**

```
components/fiscal/EntradaNota.jsx
components/fiscal/ImportarNotaXml.jsx
components/fiscal/EntradaNota.css
services/EntradaNotaService.js
```

Os 6 endpoints de `EntradaNotaController` estao todos ligados. O servico tem
**378 linhas** e **zero stub** — o unico `TODO` dele e' um comentario que *fala
sobre* os TODOs do `NFeServiceImpl`. Isso continua verdade, e continua sendo o
maior service fiscal do ERP.

O que **nao** existe e' a **carga**: `bc_fis_nfe` tem **3 linhas** e
`bc_fis_nfe_item` tem **0**. A tela abre e nao ha nada para ver — o mesmo sintoma
de `docs/pesquisa/TELAS-VAZIAS-DO-FISCAL.md`.

E' o problema de `bc_fis_regra_tributaria` pelo motivo inverso: aqui a tela
existe e o **dado** nao.

### 4.2 NFC-e: modelagem comeca, controller e service nao existem

| Camada | Estado |
|---|---|
| `bc_fis_nfce` | **17 colunas**, 58 linhas seed |
| `bc_fis_nfce_item` | **7 colunas**, 58 linhas seed |
| `fiscal/model/Nfce.java`, `NfceItem.java` | existem |
| `fiscal/repository/NfceRepository`, `NfceItemRepository` | existem |
| **Controller** | **nao existe** |
| **Service** | **nao existe** |
| **Migration** | a tabela esta la desde sempre, **sem colunas fiscais** |

O item:

```
bc_fis_nfce_item: id, nfce_id, produto_id, quantidade, valor_unitario,
                  valor_total, created_at
```

Sete colunas, **nenhuma fiscal**. Sem `cfop`, `cst`, `ncm`, `cest`, `unidade`,
`aliquota_icms`. Um item de NFC-e sem CFOP nao gera XML.

**E a peca boa: `bc_fis_nfe_item` tem 21 colunas e e' exatamente o que falta.**

```
id, nfe_id, produto_id, numero_item, ncm, cfop, cest, quantidade, unidade,
valor_unitario, valor_total, aliquota_icms, valor_icms, aliquota_ipi,
valor_ipi, codigo_produto, codigo_barras, uuid, created_at, updated_at,
deleted_at
```

**Decisao: ampliar `bc_fis_nfce_item` usando `bc_fis_nfe_item` como modelo de
migration. Nao criar tabela nova, nao criar do zero.**

### 4.3 Tributacao: 9 endpoints orfaos de CRUD sobre tabelas que existem

`CfopController` (2), `CestController` (3), `NcmController` (2),
`ImpostoController` (2) — 9 endpoints, **zero service**, direto no repository.
Mais `RegraTributaria` (model + repository) e `SefazConsultaService`.

Para emitir NFC-e o ERP precisa saber CFOP, CST, origem, unidade e aliquota de
ICMS de cada item. **A base de cadastro existe; o que falta e' a tela e a
ligacao dela com o calculo da venda.**

### 4.4 MDF-e, CT-e e EFD: existem, mas sao stub de layout

Estes 3 merecem cuidado, porque o nome sugere que estao prontos.

| Service | Linhas | Sinais de stub | O que faz |
|---|---|---|---|
| `MdfeEmissaoService` | 119 | 0 | monta o **layout** do MDF-e |
| `CteEmissaoService` | 97 | 1 | idem para CT-e |
| `SpedEfdIcmsService` | 104 | 1 | gera **exemplo** de EFD |

Nenhum deles fala com SEFAZ. Sao geradores de estrutura, o que e' util e nao e'
o que o nome sugere. **Nao contam como "pronto"** para decisao de fila.

### 4.5 `NFeService`: a assinatura existe, o corpo e' simulado

O achado que mais importa para NFC-e, porque NFC-e e' NF-e com modelo 65:

```java
// fiscal/service/NFeService.java — as 3 assinaturas existem e recebem PedidoVenda
String emitirNFe(Long empresaId, PedidoVenda pedido)
String cancelarNFe(Long empresaId, String chaveAcesso, String motivo)
String consultarSituacao(Long empresaId, String chaveAcesso)
```

```java
// fiscal/service/impl/NFeServiceImpl.java
public String emitirNFe(Long empresaId, PedidoVenda pedido) throws Exception {
    // TODO: montar TEnviNFe a partir do pedido e chamar
    //       br.com.swconsultoria.nfe.Nfe.montaNfe/envia
    String protocolo = "PROTOCOLO-SIMULADO-" + System.currentTimeMillis();
    return protocolo;
}
```

Os tres metodos devolvem `"PROTOCOLO-SIMULADO-"`, `"CANCELAMENTO-SIMULADO-"` e
`"SITUACAO-SIMULADA-"`. **Nenhum dos tres e' orfao — nao ha controller nenhum
chamando `NFeService`.** A tabela `bc_fis_nfe` tem 34 colunas, 3 linhas.

O que ja esta pronto do lado do NF-e: `br.com.swconsultoria:java-nfe:4.1.3` no
`pom.xml`, `DynamicNFeConfig`, `SefazConfig`, `SefazProperties`,
`CertificadoDigitalService`, e a assinatura XMLDSig do `nfse-sp-api`.

**Decisao: a fundacao e' real, a conversa com a SEFAZ nao existe. Ver
`ANALISE-FLUXO-FISCAL.md`.**

---

## 5. RESPOSTA DIRETA: "existe alguma funcionalidade fiscal pronta sem tela?"

Sim, tres coisas — e **nenhuma delas e' NFC-e**:

| Funcionalidade | Pronta? | Falta |
|---|---|---|
| **Importacao de NF-e (entrada)** | **Sim, 378 linhas, 0 stub** | Tela |
| Cadastro de tributacao (CFOP, CEST, NCM, ISSQN, imposed) | Sim, tabelas + 9 endpoints CRUD | Tela, e a ligacao com o calculo |
| **Emissao de qualquer modelo (NF-e, NFC-e, MDF-e, CT-e)** | **Nao** | Assinatura->SEFAZ. `NFeServiceImpl` e' simulado; MDF-e/CT-e/EFD sao layout |

**E a resposta sobre NFC-e specifically:**

> Nao existe controller, nao existe service, e a tabela de item **nao tem uma
> unica coluna fiscal**. O que existe e' a fundacao: a assinatura XMLDSig da
> NFS-e, a biblioteca `java-nfe` no `pom`, a config SEFAZ, o certificado, e
> `bc_fis_nfe_item` como modelo de migration.

---

## 6. INVENTARIO COMPLETO DOS 65 ENDPOINTS SEM TELA

Ordenado por pacote. Para cada controller: service, tabelas, migration, e cada
endpoint com seu objetivo (extraido do `@Operation(summary=...)` do codigo).


#### `ReportController` — 9 endpoint(s) · `bi`

- **Service:** ReportService
- **Tabelas (2):** `bc_bi_report`, `bc_bi_report_parameter`
- **Migration:** `V48`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `POST` | `/api/bi/reports` | Criar relatrio |
| `GET` | `/api/bi/reports` | Listar relatrios |
| `GET` | `/api/bi/reports/category/{category}` | Listar relatrios por categoria |
| `PUT` | `/api/bi/reports/{id}` | Atualizar relatrio |
| `GET` | `/api/bi/reports/{id}` | Obter relatrio por ID |
| `DELETE` | `/api/bi/reports/{id}` | Excluir relatrio |
| `POST` | `/api/bi/reports/{id}/generate` | Gerar relatrio |
| `POST` | `/api/bi/reports/{id}/schedule` | Agendar relatrio |
| `POST` | `/api/bi/reports/{id}/unschedule` | Desagendar relatrio |

#### `EmpresaLogoController` — 3 endpoint(s) · `core`

- **Service:** GenericoImagemService,[repo direto]
- **Tabelas (1):** `bc_core_empresa`
- **Migration:** `V2`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `POST` | `/api/core/empresas/{id}/logo` | Atualizar logo da empresa |
| `GET` | `/api/core/empresas/{id}/logo` | Obter logo da empresa |
| `DELETE` | `/api/core/empresas/{id}/logo` | Remover logo da empresa |

#### `RelatorioController` — 2 endpoint(s) · `core`

- **Service:** PdfGeneratorService,RelatorioService
- **Tabelas (1):** `bc_bi_relatorio`
- **Migration:** `V49`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/relatorios/pdf/{tipo}` | getRelatorioPdf |
| `GET` | `/api/relatorios/{tipo}` | getRelatorio |

#### `AuthController` — 1 endpoint(s) · `core`

- **Service:** AuthService,SpnegoService
- **Tabelas (3):** `bc_core_empresa`, `bc_core_perfil`, `bc_core_usuario`
- **Migration:** `V2`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/auth/spnego` | spnego |

#### `MdfeController` — 2 endpoint(s) · `fiscal`

- **Service:** MdfeEmissaoService
- **Tabelas (0):** _nenhuma via repository (status/config ou SQL direto)_
- **Migration:** —
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/fiscal/mdfe/recibo` | Consulta o recibo e obtém chave e protocolo |
| `GET` | `/api/fiscal/mdfe/status` | Consulta o recibo e obtém chave e protocolo |

#### `SpedEfdController` — 2 endpoint(s) · `fiscal`

- **Service:** SpedEfdIcmsService
- **Tabelas (0):** _nenhuma via repository (status/config ou SQL direto)_
- **Migration:** —
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/fiscal/sped/efd/exemplo` | Gera um EFD de exemplo, para conferir o formato |
| `POST` | `/api/fiscal/sped/efd/gerar` | Gera um EFD de exemplo, para conferir o formato |

#### `CteController` — 1 endpoint(s) · `fiscal`

- **Service:** CteEmissaoService
- **Tabelas (0):** _nenhuma via repository (status/config ou SQL direto)_
- **Migration:** —
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/fiscal/cte/status` | Status do serviço CT-e na SVRS (não exige certificado) |

#### `ClassificacaoController` — 13 endpoint(s) · `ia`

- **Service:** ClassificacaoService
- **Tabelas (1):** `bc_ia_classificacao`
- **Migration:** `V52`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `POST` | `/api/ia/classificacoes` | Criar nova classificacao |
| `GET` | `/api/ia/classificacoes` | Listar todas as classificacoes da empresa |
| `POST` | `/api/ia/classificacoes/classificar-lote` | Classificar lote de entidades |
| `POST` | `/api/ia/classificacoes/classificar-produto` | Classificar produto |
| `GET` | `/api/ia/classificacoes/entidade/{entidadeId}` | Listar classificacoes por entidade |
| `GET` | `/api/ia/classificacoes/high-confidence/{tipo}` | Listar classificacoes com alta confianca |
| `GET` | `/api/ia/classificacoes/pendentes/{tipo}` | Listar classificacoes pendentes por tipo |
| `GET` | `/api/ia/classificacoes/tipo/{tipo}` | Listar classificacoes por tipo |
| `PUT` | `/api/ia/classificacoes/{id}` | Atualizar classificacao |
| `GET` | `/api/ia/classificacoes/{id}` | Buscar classificacao por ID |
| `DELETE` | `/api/ia/classificacoes/{id}` | Excluir classificacao |
| `POST` | `/api/ia/classificacoes/{id}/aprovar` | Aprovar classificacao |
| `POST` | `/api/ia/classificacoes/{id}/rejeitar` | Rejeitar classificacao |

#### `PromptController` — 12 endpoint(s) · `ia`

- **Service:** PromptService
- **Tabelas (1):** `bc_ia_prompt`
- **Migration:** `V52`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `POST` | `/api/ia/prompts` | Criar novo prompt |
| `GET` | `/api/ia/prompts` | Listar todos os prompts da empresa |
| `GET` | `/api/ia/prompts/buscar` | Buscar prompts por termo |
| `GET` | `/api/ia/prompts/categoria/{categoria}` | Listar prompts por categoria |
| `GET` | `/api/ia/prompts/favoritos` | Listar prompts favoritos |
| `GET` | `/api/ia/prompts/mais-usados` | Listar prompts mais usados |
| `GET` | `/api/ia/prompts/publicos` | Listar prompts publicos |
| `PUT` | `/api/ia/prompts/{id}` | Atualizar prompt |
| `GET` | `/api/ia/prompts/{id}` | Buscar prompt por ID |
| `DELETE` | `/api/ia/prompts/{id}` | Excluir prompt |
| `POST` | `/api/ia/prompts/{id}/favorito` | Alternar favorito do prompt |
| `POST` | `/api/ia/prompts/{id}/incrementar-uso` | Incrementar contador de uso do prompt |

#### `EmbeddingController` — 11 endpoint(s) · `ia`

- **Service:** EmbeddingService
- **Tabelas (1):** `bc_ia_embedding`
- **Migration:** `V52`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `POST` | `/api/ia/embeddings` | Criar novo embedding |
| `GET` | `/api/ia/embeddings` | Listar todos os embeddings da empresa |
| `GET` | `/api/ia/embeddings/entidade-tipo/{entidadeTipo}` | Listar embeddings por tipo de entidade |
| `GET` | `/api/ia/embeddings/entidade/{entidadeTipo}/{entidadeId}` | Listar embeddings por entidade |
| `DELETE` | `/api/ia/embeddings/entidade/{entidadeTipo}/{entidadeId}` | Excluir embeddings por entidade |
| `GET` | `/api/ia/embeddings/mais-similar` | Buscar embedding mais similar |
| `POST` | `/api/ia/embeddings/reindexar` | Reindexar embeddings |
| `GET` | `/api/ia/embeddings/similares` | Buscar embeddings similares |
| `PUT` | `/api/ia/embeddings/{id}` | Atualizar embedding |
| `GET` | `/api/ia/embeddings/{id}` | Buscar embedding por ID |
| `DELETE` | `/api/ia/embeddings/{id}` | Excluir embedding |

#### `ChatMensagemController` — 7 endpoint(s) · `ia`

- **Service:** ChatMensagemService
- **Tabelas (2):** `bc_ia_chat_mensagem`, `bc_ia_chat_sessao`
- **Migration:** `V52`
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `POST` | `/api/ia/mensagens` | Criar nova mensagem de chat |
| `GET` | `/api/ia/mensagens/sessao/{sessaoId}` | Listar mensagens por sessao |
| `DELETE` | `/api/ia/mensagens/sessao/{sessaoId}` | Excluir todas as mensagens de uma sessao |
| `GET` | `/api/ia/mensagens/sessao/{sessaoId}/ordenado` | Listar mensagens por sessao ordenadas |
| `GET` | `/api/ia/mensagens/{id}` | Buscar mensagem por ID |
| `DELETE` | `/api/ia/mensagens/{id}` | Excluir mensagem |
| `POST` | `/api/ia/mensagens/{id}/classificar` | Classificar mensagem |

#### `GerenciadorSqlController` — 2 endpoint(s) · `shared`

- **Service:** GerenciadorSqlService
- **Tabelas (0):** _nenhuma via repository (status/config ou SQL direto)_
- **Migration:** —
- **Tela:** nenhuma

| Verbo | Endpoint | Objetivo |
|---|---|---|
| `GET` | `/api/superadmin/sql/tabelas/{tabela}` | tabela |
| `GET` | `/api/superadmin/sql/tabelas/{tabela}/dados` | dados |

---

## 6b. CHECAGEM CRUZADA: ISTO E' ORFAO, OU ESTAO FAZENDO?

> Pergunta do dono: *"existe o risco de endpoint sem tela nao significar
> funcionalidade abandonada, mas funcionalidade em desenvolvimento por outra
> IA?"*

**Resposta: nao. Os 108 sao orfaos de verdade.** Cinco verificacoes:

| # | Verificacao | Resultado |
|---|---|---|
| 1 | Fila do `ia-tarefa` | **"tarefa limpa"** — nenhuma IA declarou trabalho em andamento |
| 2 | `ANDAMENTO.md` (ia-hub) | As ultimas entradas sao minhas (2B.2, mapa de microsservicos). Nenhuma IA assumiu trabalho em fiscal |
| 3 | Git: quem mexeu no pacote `fiscal` | **Ultimo toque em 27/09**, e os dois commits (`1a02eea6`, `2fd41eae`) sao **mecanicos**: 22 insercoes e 22 remocoes, todas `brasilcloud` -> `brasil-saas`. Nenhuma mudanca funcional |
| 4 | Working tree: ha algo não commitado no fiscal? | **Nada.** `git status` do pacote `fiscal` volta limpo |
| 5 | Branchs em voo | **1 branch a frente do HEAD**, `origin/git-ci/cd-errors-7bb8f`, de **23/09**, tocando `.gitignore` e um `.bak`. **Zero** arquivos de fiscal, vendas ou PDV |

E os commits de **hoje** (12:22 as 14:09, sete commits) sao todos `docs(dbm)` —
o Astral Database Manager escrevendo especificacao. Nenhum toca o ERP em codigo.

```
$ git log --since="2026-09-29" --format="  %h %ad %s"
  44180ea8 14:09 docs(dbm): remove os dois caracteres chineses...
  081d5f8f 14:09 docs(dbm): banco proprio do DBM...
  88c9475a 13:46 docs(dbm): corrige as duas sobras de portugues...
  ... (7 commits, todos docs(dbm))
  c055d875 07:29 fix(nfse-sp): devolve inscricao_municipal...
```

**A regra para frente:** quando um modulo aparecer sem tela, a checagem e' sempre
esta, nao so a de codigo:

1. Endpoint existe?
2. Service existe?
3. Migration existe?
4. Documentacao existe?
5. **Alguma IA declarou ou esta mexendo nisso?** (`ia-tarefa`, `git log`,
   `git status` do modulo, branchs a frente do HEAD)

Sem o passo 5, "orfao" e' so uma inferencia. Com ele, e' um fato.


---

## 7. COMO MEDIR

```bash
cd ~/BrasilCloudERP
python3 /tmp/inv.py          # gera /tmp/inv.json com os 466 endpoints

# so os orfaos
python3 -c "
import json
for r in json.load(open('/tmp/inv.json')):
    if not r['tela']: print(r['verb'], r['path'], r['ctrl'], r['svc'], r['tabs'], r['mig'])
"
```

O script, em ordem:

1. Le `V*.sql` e monta `tabela -> versao` (`CREATE TABLE` e `ALTER TABLE`).
   **Atencao:** o SQL e' qualificado com schema (`CREATE TABLE brasil_saas.x`).
   Um regex sem o schema capture o nome errado — ja caí nisso e reportei
   "10 endpoints com tabela sem migration" quando o numero real era **zero**.
2. Casa `@Table(name=)` de cada `@Entity` com a tabela do passo 1.
3. Resolve `Repository -> Entidade` pela classe generica do `JpaRepository`.
4. Resolve `Service -> Repositories` pelo **tipo** do campo `private final`,
   varredura global do arquivo (o codigo declara varias dependencias na mesma
   linha).
5. Cruza `@RequestMapping`/`@Get|Post|Put|DeleteMapping` com as URLs que o
   frontend chama — **em duas passadas**:
   - literais com `/api` (qualquer `.js`/`.jsx`);
   - chamadas `api.get('/x')` dentro de `src/services/`, o `baseURL` do axios
     ja tendo `/api`.
   **Sem a segunda passada o numero sai errado.** Era o erro 6 da §8.

**Dois detalhes que custaram tempo e mudaram o resultado:**

- **As URLs estao em `src/services/*.js`, nao nos `.jsx`.** As telas chamam
  Service. Uma varredura que procure `axios` dentro do `.jsx` diz que 43 telas
  nao tem API, e isso e' falso. Ja registrei esse erro.
- **A ordem de `@Operation` e `@GetMapping` varia no codigo.** Em
  `ReportController` o `@Operation` vem **depois**; nos demais, antes. A janela
  de busca do objetivo tem que ir do mapping anterior ate o proximo.

---

## 8. MEUS ERROS DE MEDICAO NESTE LEVANTAMENTO

Registrados porque jalestick agora vale mais que a medida:

| # | Erro | Como foi | Correcao |
|---|---|---|---|
| 1 | "10 endpoints orfaos com tabela **sem migration**" | regex de `CREATE TABLE` nao tratava `brasil_saas.` qualificado; capturou `brasil_saas` como nome de tabela | numero real: **0** |
| 2 | "migrations vao ate V99" | `ls \| tail` ordena **lexicografico**: `V100..V107` ordenam **antes** de `V94` | vao ate **V107**, e o banco tem V107 aplicado |
| 3 | "o objetivo do endpoint esta deslocado" | a janela de busca pegava o `@Operation` do metodo seguinte | janela entre o mapping anterior e o proximo |
| 4 | "controller sem tabela" | usei o **nome** do campo (`svc`) em vez do **tipo** | por tipo, e global no arquivo |
| 5 | "a 4567 nao existe" | o proxy existe e e' do dono; o que falta e' a **unit** | `FAILOVER-4567-PENDENTE.md` |
| 6 | "**109** endpoints orfaos" | o frontend usa `api.post('/auth/login')` — caminho **sem** `/api`, porque o `baseURL` do axios ja traz. Eu so casava literais com `/api`, e marquei 3 endpoints de producao como orfaos | **108**. O unico orfao real de `AuthController` e' `/api/auth/spnego` |
| 7 | "**43 endpoints com tela** marcados como orfaos" | so reconhecia `api.get('/x')` e `fetch('/api/x')`; **18 dos 20 `services/*.js` montam a URL com `${BASE_URL}`** | **65**, nao 108 |
| 8 | "importacao de NF-e pronta, so falta a tela" | a tela existe. Falta **carga**: `bc_fis_nfe` tem 3 linhas, `bc_fis_nfe_item` tem 0 |Corrigido em §4.1 |

Nenhum desses cinco alterou a **conclusao** — que e' "comanda e pre-venda nao
existem, orcamento e multiplas formas ja existem, NFC-e e' reuso de fundacao".
Mas quatro deles alteraram numeros que eu ia reportar como verdade.

---

## 9. DECISAO

| Vou construir | Veredito |
|---|---|
| Comanda | **Construir.** Nada existe |
| Pre-venda | **Construir.** Nada existe |
| Sessao de caixa | **Construir.** A tabela do caixa existe, a sessao nao |
| **Orcamento de venda** | **Reaproveitar.** `confirmar()` pronto, falta tela |
| **Multiplas formas** | **Reaproveitado.** Backend ja suportava |
| **Fechamento** | **Reaproveitado.** API pronta |
| **Importacao de NF-e** | **Reaproveitar.** 378 linhas, 0 stub, falta tela |
| **Tributacao (CFOP/CST/NCM/CEST)** | **Reaproveitar as tabelas**, falta tela e a ligacao com o calculo |
| **NFC-e** | **Reaproveitar a fundacao.** `bc_fis_nfe_item` como modelo de migration, `java-nfe` no pom, certificado e config prontos, `NfseSpSigner.assinarXml` ja faz o XMLDSig que a SEFAZ exige. Falta: colunas fiscais no item, controller, service, e a conversa com a SEFAZ |
| MDF-e / CT-e / EFD | **Nao usar como base.** Sao layout, nao emissao |

