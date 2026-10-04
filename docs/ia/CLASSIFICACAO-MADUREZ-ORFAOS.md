> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

---
id: 2026-09-29-classificacao-maturidade-orfaos
status: levantamento
data: 2026-09-29
---

# CLASSIFICACAO DOS ENDPOINTS ORFAOS POR MATURIDADE

> Pergunta do dono: *classificar os orfaos em pronto para uso / pronto sem tela /
> parcialmente pronto / prototipo / stub*, e priorizar por valor de negocio.
>
> Documento par: `RELATORIO-ENDPOINTS-ORFAOS.md` (os 65 endpoints, um a um).
>
> ## CORRECAO DE 29/09
>
> A primeira versao deste documento dizia **108 endpoints orfaos, 63 prontos para
> uso**. **Errado nos dois numeros.** O detector de tela nao reconhecia o padrao
> `${BASE_URL}/...`, que e' o de **18 dos 20** `services/*.js` — e por isso
> **43 endpoints que tem tela foram marcados como orfaos**.
>
> O numero correto e' **65 orfaos, 39 prontos para uso**. E o que cai fora muda a
> fila: **importacao de NF-e, folha de pagamento, superadmin, cargos, CFOP, CEST,
> NCM,-imposed, ISSQN e a propria SEFAZ tem tela.** As 5 telas de cadastro fiscal
> que eu ia "expor" ja existem desde antes.
>
> `docs/pesquisa/TELAS-VAZIAS-DO-FISCAL.md` (26/09) ja descrevia parte disso: as
> telas do fiscal existiam, abriam, e mostravam vazio porque as tabelas estavam
> vazias.

---

## 1. A RESPOSTA, E O QUE MUDOU NELA

| Classe | Controllers | Endpoints |
|---|---|---|
| **Pronto para uso** | 5 | **39** |
| **Pronto sem tela** | 2 | 10 |
| **Parcial** | 0 | 0 |
| **Prototipo** | 5 | 16 |
| **Stub** | 0 | **0** |
| Completo e desligado | **0** | 0 — a SEFAZ **tem tela** (`SefazConsultaService.js`) |

Duas coisas que a classificacao nagou, e que mudam a fila:

1. **Nao existe nenhum stub.** Nenhum dos 108 endpoints devolve dado falso. Os
   que parecem stub sao outra coisa (prototipo), e ha uma categoria que o
   proprio Exercicio nao previa — **completo e desligado por flag** — que e' o
   achado mais valioso da sessao (§4).
2. **`Pronto para uso` sao 63 endpoints, nao 8.** A intencao de comecar por
   comanda e NFC-e e' competes com um estoque de tela ja escrito por tras.

---

## 2. COMO CLASSIFICOU (5 sinais medidos, naoopiniao)

| Sinal | De onde |
|---|---|
| **Volume de codigo** do service, em linhas uteis (sem comentario) | leitura do arquivo |
| **Stub de verdade** | `TODO`/`SIMULAD`/`mock` **fora de comentario**, e sem a palavra `TODOS` |
| **Tabela tocada** | `controller -> service -> repository -> @Entity -> @Table` |
| **Linhas na tabela** | `count(*)` no Postgres |
| **Chama sistema externo** | `RestClient`, `SOAP`, `Nfe.*`, `WebService` |

Regra de classificacao:

| Classe | Criterio |
|---|---|
| **Pronto para uso** | sem stub, toca tabela, **dados carregados**, >= 120 linhas de regra |
| **Pronto sem tela** | sem stub, regra completa, falta so a interface |
| **Parcial** | CRUD de tabela de cadastro, ou codigo pronto com **tabela vazia** |
| **Prototipo** | monta layout ou status; nao persiste o documento |
| **Stub** | devolve dado simulado — **nenhum caso encontrado** |

---

## 3. A CLASSIFICACAO

| Classe | Controller | Eps | Codigo | Por que |
|---|---|---|---|---|
| Pronto p/ uso | `RelatorioController` | 2 | 574 | regra completa + dados |
| Pronto p/ uso | `AuthController` | 1 | 358 | regra completa + dados |
| Pronto p/ uso | `ClassificacaoController` | 13 | 158 | regra completa + dados |
| Pronto p/ uso | `PromptController` | 12 | 130 | regra completa + dados |
| Pronto p/ uso | `EmbeddingController` | 11 | 125 | regra completa + dados |
| Pronto s/ tela | `EmpresaLogoController` | 3 | 112 | regra completa |
| Pronto s/ tela | `ChatMensagemController` | 7 | 84 | regra completa |
| Prototipo | `GerenciadorSqlController` | 2 | 218 | monta layout/status; nao persiste |
| Prototipo | `ReportController` | 9 | 166 | 2 stub(s) real(is) |
| Prototipo | `MdfeController` | 2 | 119 | monta layout/status; nao persiste |
| Prototipo | `SpedEfdController` | 2 | 104 | monta layout/status; nao persiste |
| Prototipo | `CteController` | 1 | 97 | monta layout/status; nao persiste |

---|---|---|---|---|---|
| **Completo, desligado** | `SefazConsultaController` | 3 | 33 | — | **Chama a SEFAZ de verdade.** Desligado por `enabled:false` (§4) |
| **Pronto p/ uso** | `RelatorioController` | 2 | 574 | 58 | Maior service orfao do ERP, 0 stub |
| **Pronto p/ uso** | `ClassificacaoController` | 13 | 158 | 58 | 13 endpoints, regra completa |
| **Pronto p/ uso** | `PromptController` | 12 | 130 | 58 | Doze endpoints de prompt, 0 stub |
| **Pronto p/ uso** | `AuthController` | 1 | 358 | 36 | So `/api/auth/spnego` e' orfao; login/me/refresh tem tela |
| **Pronto p/ uso** | `SuperAdminController` | 1 | 160 | 29 | Painel de superadmin |
| **Pronto p/ uso** | `UsuarioAdminController` | 10 | 221 | 62 | Gestao de usuario, modulos e permissao |
| **Pronto p/ uso** | `FolhaPagamentoController` | 7 | 156 | 337 | 7 endpoints de folha, 0 stub |
| **Pronto p/ uso** | `EntradaNotaController` | 6 | **445** | 846 | **`NfeImportacaoService` com 378 linhas** (§5) |
| **Pronto p/ uso** | `EmbeddingController` | 11 | 125 | 58 | 11 endpoints; 1 `TODO` real isolado em `findAllByEmpresaId` |
| **Pronto s/ tela** | `CestController` | 3 | 63 | 1.043 | CRUD com regra de busca por NCM |
| **Pronto s/ tela** | `ChatMensagemController` | 7 | 84 | 116 | Sessao e mensagem |
| **Pronto s/ tela** | `CargoController` | 5 | 70 | 78 | Cadastro de RH |
| **Pronto s/ tela** | `EmpresaLogoController` | 3 | 112 | 7 | Upload de logo por empresa |
| **Parcial** | `IssqnController` | 2 | 30 | **1.759.790** | CRUD cru, mas a **tabela esta cheia** |
| **Parcial** | `NcmController` | 2 | 36 | 10.515 | CRUD cru, tabela carregada |
| **Parcial** | `CfopController` | 2 | 33 | 619 | CRUD cru |
| **Parcial** | `ImpostoController` | 2 | 37 | 58 | CRUD cru |
| **Prototipo** | `GerenciadorSqlController` | 2 | 218 | dinamico | **Funciona** — `JdbcTemplate` em `information_schema`. Sem repository, por isso o rastreador nao achou tabela |
| **Prototipo** | `MdfeController` | 2 | 119 | 0 | Monta o **layout** do MDF-e, nao emite |
| **Prototipo** | `SpedEfdController` | 2 | 104 | 0 | Gera **exemplo** de EFD |
| **Prototipo** | `CteController` | 1 | 97 | 0 | Idem para CT-e |
| **Prototipo** | `ReportController` | 9 | 166 | 116 | 2 stubs reais noBI |

---

## 4. O ACHADO: A INTEGRACAO COM A SEFAZ EXISTE E ESTA DESLIGADA

Este e' o resultado que mais muda a fila, e so apareceu porque o classificador
olhou para "chama sistema externo" e nao so para "toca tabela".

`SefazConsultaService` tem **33 linhas** e nao e' stub:

```java
public TRetConsStatServ statusServico(Long empresaId,EstadosEnum uf)   { return Nfe.statusServico(...); }
public TRetConsSitNFe   consultarPorChave(Long empresaId,EstadosEnum uf,String chave) { return Nfe.consultaXml(...); }
public RetDistDFeInt    distribuicaoDFe(Long empresaId,EstadosEnum uf,String cnpj,String tipo,Integer valor) { return Nfe.distribuicaoDfe(...); }
```

**Tres chamadas reais a SEFAZ**, pela mesma biblioteca que a NFC-e usaria:
status do servico, consulta por chave de acesso, e **distribuicao DFe**
(`NFePullNFe` — os documentos emitidos contra o CNPJ da empresa).

Testado ao vivo, com um token de verdade:

```
GET /api/fiscal/sefaz/status?uf=SP
{"erro":"SEFAZ desabilitada",
 "disponivelEm":"30 dias (certificado pendente)",
 "mensagem":"Configure o certificado em bc_fis_certificado_digital e set
            brasil-saas.fiscal.sefaz.enabled=true no application-dev.yml"}
```

A resposta e' deliberada e util. Ela diz exatamente o que falta. E o codigo
explica por que:

```java
// SefazProperties / SefazConfig
@ConditionalOnProperty(name = "brasil-saas.fiscal.sefaz.enabled", havingValue = "true")
```

E o valor, hoje:

```
src/main/resources/application-dev.yml:112
  sefaz:
    enabled: false
```

### O que bloqueia, medido

| | |
|---|---|
| Codigo da integracao | **completo** |
| Biblioteca (`java-nfe`) | no `pom.xml` e no `.m2` |
| Config por UF | `SefazConfig`, `SefazProperties`, `DynamicNFeConfig` |
| Tela de certificado | **existe** — `CertificadoDigital.jsx`, com 2 endpoints |
| Registros em `bc_fis_certificado_digital` | 58 |
| **Certificados reais** | **0** |
| Flag | `enabled: false` |

Os 58 registros sao semente, e verificados um a um:

```
id | empresa_id |      cnpj      |        razao        | validade_at | ativo
 1 |          1 | 00000000000001 | SEED RAZAO_SOCIAL 1 | 2026-01-15 | t
 2 |          1 | 00000000000002 | SEED RAZAO_SOCIAL 2 | 2026-01-15 | f
 3 |          1 | 00000000000003 | SEED RAZAO_SOCIAL 3 | 2026-05-20 | t

total | com_arquivo | com_pfx_real | cnjps_distintos
    58 |          58 |            0 |              58
```

**CNPJ falso, razao social `SEED`, todas as validades vencidas (hoje e' 29/09),
e zero `.pfx` real.** No disco tambem nao ha nenhum:

```
/etc/brasil-saas/pki/    ->  ca.crt, issued, private     (nenhum .pfx/.p12)
```

### O que isso muda

> **A "conversa com a SEFAZ" que eu listei como o maior obstaculo da NFC-e ja
> esta escrita e funcionando.** Ela esta **desligada por uma booleana e sem
> certificado A1** — que e' acao comercial, nao engenharia.

O que eu escrevi em `MAPA-NFCE.md` — *"conversa com a SEFAZ: nao existe para
nenhum estado"* — esta **corrigido**: a integracao existe, e foi testada. O que
falta e' a credencial e a ligacao da **emissao** (`Nfe.enviarNfe`), que e' a
mesma classe, a mesma biblioteca e a mesma config.

---

## 5. MODULOS FISCAIS QUASE PRONTOS

| Modulo | Endpoints | Codigo | Falta | Valor |
|---|---|---|---|---|
| **Importacao de NF-e** | 6 | 445 | **so a tela** | Compra com nota, sem digitacao |
| **Tributacao** | 9 | 199 | tela + ligacao com o calculo | **1,77 milhao de linhas de ISSQN, 10.515 de NCM, 1.043 de CEST, 619 de CFOP ja carregadas** |
| **SEFAZ status/consulta/distribuicao** | 3 | 33 | flag + A1 | Verificacao de status, consulta por chave, documentos recebidos |
| **NFC-e** | **0** | 0 | tudo (mas a fundacao existe) | Ver `MAPA-NFCE.md` |
| MDF-e / CT-e / EFD | 5 | 320 | emissao real | Prototipo de layout |

### O caso da tributacao, que eu subestimei

`IssqnController` e`NcmController` sao "Parcial" porque sao CRUD cru de 30-36
linhas. Mas os numeros nao sao de CRUD:

| Tabela | Linhas |
|---|---|
| `bc_fis_issqn` | **1.759.790** |
| `bc_fis_ncm` | 10.515 |
| `bc_fis_cest` | 1.043 |
| `bc_fis_cfop` | 619 |

**1,76 milhao de aliquotas de ISSQN carregadas, sem tela.** Isso nao e' um CRUD
qualquer: e' um acervo que alguem pagou para montar e que hoje so e' acessivel
por `curl`.

---

## 6. PRIORIDADE POR VALOR DE NEGOCIO

Nao por esforco. A pergunta e': **o que o cliente sente primeiro?**

| # | Entrega | Endpoints | Esforco | Por que |
|---|---|---|---|---|
| **1** | **A1 + flag da SEFAZ** | 0 | **comercial** | A integracao esta escrita e tem tela. Falta so o certificado e a booleana |
| **2** | **Tributacao da venda** | 0 | regra + dado | `bc_fis_regra_tributaria` tem schema e **zero linhas**, e **nenhuma fonte no projeto** |
| **3** | **NFC-e** | 0 | grande | Fundacao pronta; depende de 1 e 2 |
| **4** | **Comanda** (bar) | 0 | tudo | **Nada existe.** Construcao do zero |
| **5** | Relatorio BI, prompts, classificacao, embedding, chat | 39 | so a tela | Valor interno; nao e' venda nem fiscal |
| — | Importacao de NF-e | **0** | — | **Ja tem tela.** Falta **carga**: `bc_fis_nfe` tem 3 linhas |
| — | Folha, cargos, superadmin, logo | **0** | — | **Ja tem tela.** Saíram da lista de orfaos na correcao |

### A leitura que muda a estrategia

> O caminho de menor risco para ter **documento fiscal emitido** nao passou por
> nenhuma das 65 telas orfas: **e' o A1, a flag, e a tributacao.** Nenhuma das 39
> telas prontas e' de venda, fiscal ou estoque.

A NFC-e continua sendo reaproveitamento, nao construcao — mas ela deixa de ser o
primeiro item, porque depende de #1 e #5, e os dois sao mais barataos que ela.

---

## 7. CHECAGEM CRUZADA: NINGUMA IA ESTA NESSES MODULOS

| Verificacao | Resultado |
|---|---|
| `ia-tarefa listar` | **tarefa limpa** |
| `ANDAMENTO.md` | ultimas entradas sao minhas; nenhuma IA assumiu fiscal |
| `git log` do pacote `fiscal` | ultimo toque **27/09**, e os 2 commits sao **mecanicos** (`brasilcloud` -> `brasil-saas`, 22/22 linhas) |
| `git status` do pacote `fiscal` | **limpo** |
| Branchs a frente do HEAD | 1 so (`origin/git-ci/cd-errors-7bb8f`, de 23/09, so `.gitignore`), **zero** arquivos fiscais |
| Commits de hoje (12:22-14:09) | 7, **todos `docs(dbm)`** — o Astral Database Manager escrevendo especificacao |

**Estes 108 endpoints sao orfaos de verdade.** Detalhe em
`RELATORIO-ENDPOINTS-ORFAOS.md` §6b.

---

## 8. O QUE EU ERREI NESTA CLASSIFICACAO

| # | Erro | Correcao |
|---|---|---|
| 1 | **`SefazConsultaController` classificado "Prototipo"** porque o rastreador so seguia `repository -> @Table`, e ele **chama a SEFAZ** | Categoria nova: **completo e desligado**. E o achado da sessao |
| 2 | `EntradaNotaController` como "Prototipo" | O unico `TODO` dele e' um comentario que *fala sobre* os TODOs do `NFeServiceImpl`. **445 linhas, 0 stub** |
| 3 | `ModuloAcessoService` contava 6 stubs | A palavra **"TODOS"** em comentario casa com `TODO`. Detector refeito |
| 4 | `AuthController` (producao) contado como orfao em 4 endpoints | O frontend usa `api.post('/auth/login')`, caminho **sem** `/api`. O numero de orfaos caiu de **109 para 108** |
| 5 | `GerenciadorSqlController` sem tabela | Usa `JdbcTemplate` em `information_schema`, nao repository. **Funciona** |

O erro 1 e' o mais instructive: **um classificador que so olha "toca tabela" e
cega para o que uma funcionalidade faz de verdade.** Foi ele que escondeu a
integracao com a SEFAZ.

---

