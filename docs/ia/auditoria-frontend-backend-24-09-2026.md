> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Auditoria Backend x Frontend — Brasil SaaS ERP
## 24/09/2026

Esta auditoria substitui os números antigos dos documentos de 21/09/2026 quando houver divergência com o código atual de `main`.

## 1. Inventário atual

O monólito ativo está organizado em módulos Java dentro de `src/main/java/br/com/brasil_saas`.

Controllers encontrados no código atual (excluindo backups e microserviços copiados em resources):

- Cadastro: 12
- Compras: 1
- Estoque: 2
- Financeiro: 8
- Fiscal: 7
- IA: 9
- BI: 6
- Produção: 3
- RH: 4
- Serviços: 1
- Vendas: 1
- Core: 9

Total auditado: **63 controllers** nesses módulos.

O frontend ativo está em `src/main/resources/static/react` e possui **58 arquivos JSX**, considerando os componentes da raiz e dos subdiretórios, além de **40 serviços JavaScript**.

Portanto, o problema não é simplesmente quantidade de arquivos. O ponto principal é **cobertura funcional e profundidade de cada tela**: alguns JSX são telas completas, outros são containers, e vários endpoints do backend ainda não possuem uma operação equivalente na UI.

## 2. Cobertura por domínio

### Cadastro

Backend:
- Categoria
- Cliente
- ClienteLogo
- Fornecedor
- FornecedorLogo
- Marca
- Município
- Pessoa
- Produto
- Serviço
- Transportadora
- Unidade de Medida

Frontend atual possui componentes para todos esses grupos principais, incluindo:
- CadastroPessoas
- CadastroProdutos
- Categoria
- Cliente
- Fornecedor
- Marca
- ServiçoCadastro
- Transportadora
- UnidadeMedida
- ClienteLogo
- FornecedorLogo
- Municípios

**Situação:** cobertura de telas muito melhor que a documentada no mapa antigo de 21/09.

### Compras

Backend: `PedidoCompraController`.

Frontend: `Compras.jsx`.

A rota principal `/api/compras/pedidos` está coerente.

### Vendas

Backend: `PedidoVendaController` em `/api/vendas/pedidos`.

Frontend: `Vendas.jsx` + `PedidoVendaService.js`.

A rota principal está coerente.

### Estoque

Backend:
- `MovimentacaoEstoqueController`
- `SaldoEstoqueController`

Frontend:
- `Estoque.jsx`
- `MovimentacaoEstoqueService.js`

As APIs principais utilizadas pelo componente estão em `/api/estoque/*`.

### Serviços

Backend:
- `OrdemServicoController` em `/api/servicos/os`
- `ServicoController` em `/api/cadastro/servicos`

Frontend possui os dois conceitos:
- `OrdemServico.jsx` / `OrdemServicoService.js`
- `Servicos.jsx` / `ServicoCadastro.jsx`

**Importante:** são dois domínios diferentes e não devem ser tratados como a mesma tela.

### Financeiro

Backend possui 8 controllers:
- títulos
- lançamentos contábeis
- centro de custos
- contas bancárias
- extrato
- plano de contas
- condições de pagamento
- tipos de pagamento

Frontend possui componentes correspondentes para esses grupos.

Além disso, a documentação do backend menciona funcionalidades adicionais como conciliação bancária, fluxo de caixa, DRE, análise de rentabilidade e outros processos financeiros. Essas funcionalidades precisam ser verificadas individualmente contra as telas existentes; não devem ser consideradas cobertas apenas porque existe uma página "Financeiro".

### Fiscal

Backend possui:
- NCM
- CFOP
- CEST
- ISSQN
- Entrada de NF
- Impostos
- SEFAZ

Frontend atual possui componentes correspondentes:
- Ncm
- Cfop
- Cest
- Issqn
- EntradaNota
- Impostos
- SefazConsulta

Há ainda um componente legado `Fiscal.jsx` na raiz, que não é a estrutura principal atual do roteamento.

A regra de negócio solicitada para este projeto é que o fluxo de **entrada de nota de produto** seja tratado pelo módulo de entrada fiscal; não transformar `nfe` em um módulo genérico de emissão sem necessidade.

### RH

Backend possui:
- Funcionários
- Cargos
- Folha de Pagamento
- Foto de Funcionário

Frontend possui:
- RH
- Cargo
- FolhaPagamento
- FuncionarioFoto

Além disso, existe o módulo externo `microservices/esocial`, com unidade systemd própria. Ele deve ser tratado como integração de RH/eSocial, não como parte do CRUD básico de funcionários.

### Produção

Backend:
- Produção
- Apontamentos
- Romaneios

Frontend:
- Producao
- ApontamentosProducao
- RomaneioProducao

Aqui existe cobertura de tela para os três controllers.

### BI

Backend possui controllers para:
- dashboards
- indicadores
- KPIs
- relatórios
- relatórios agendados
- relatórios adicionais

Frontend possui `BI.jsx` e `BIService.js`, mas a cobertura ainda não equivale à quantidade de operações administrativas disponíveis no backend.

Há uma diferença importante:
- backend `/api/bi/kpis`
- backend `/api/bi/relatorios`
- backend `/api/bi/relatorios-agendados`
- frontend atual também utiliza `/api/bi/indicadores` e `/api/bi/dashboards`.

O frontend deve evoluir para expor essas operações em telas próprias, em vez de considerar um único dashboard como cobertura total de BI.

## 3. IA / Spring AI

O sistema **usa a API do Spring AI no backend**.

Evidências atuais:
- dependência `org.springframework.ai:spring-ai-openai-spring-boot-starter`;
- `ChatClientConfig`;
- `ChatServiceImpl` injeta `org.springframework.ai.chat.client.ChatClient`;
- `ChatController` expõe `/api/ia/chat`;
- existem controllers próprios para sessões, mensagens, classificação, análise preditiva, embeddings, prompts e configuração.

### Estado da integração com OpenRouter

O backend foi configurado para usar o OpenRouter como provedor OpenAI-compatible do Spring AI.

Configuração atual:

```yaml
spring:
  ai:
    openai:
      base-url: https://openrouter.ai/api/v1
      api-key: ${OPENROUTER_API_KEY:}
      chat:
        enabled: true
        options:
          model: openrouter/auto
```

Portanto:

**Spring AI está integrado e o ChatClient está configurado para usar o OpenRouter em `/api/v1/chat/completions`.**

A chave fica exclusivamente em variável de ambiente e não é armazenada no ERP nem enviada ao frontend.

Os endpoints OpenRouter `/api/v1/responses` e `/api/v1/messages` não são usados pelo ChatClient atual; se forem necessários, devem ser implementados posteriormente como adapters backend.

Quando não houver um `ChatModel` real disponível, `ChatClientConfig` continua fornecendo um `fallbackChatModel` sem chamada externa.

Isso significa que "Spring AI instalado" e "IA externa funcionando" são estados diferentes.

### Correção realizada em 24/09

O frontend IA estava usando rotas antigas:

- `/api/ia/chat/sessoes`
- `/api/ia/chat/{id}/mensagens`

Os controllers atuais usam:

- `/api/ia/sessoes`
- `/api/ia/mensagens/sessao/{id}/ordenado`
- `/api/ia/chat`

Além disso, os controllers exigem o header:

```
X-Empresa-Id
```

O frontend foi alinhado a esses contratos.

Commits:
- `d0087fc0c411967f1f892355d45a58c62e23c87b` — serviço IA alinhado aos controllers
- `81784f7f2b2e00acdb0592bcf30fe7093345636f` — tela IA alinhada ao contrato atual

## 4. Autenticação

O fluxo de autenticação foi documentado em:

`docs/autenticacao.md`

Regra atual:

1. usuário ERP normal autentica por BCrypt;
2. se BCrypt falhar, o backend tenta autenticar a role PostgreSQL;
3. somente se a role autenticar e `rolsuper=true` ela pode substituir o BCrypt;
4. um SUPERUSER PostgreSQL pode ser provisionado automaticamente como usuário ERP;
5. recebe `ROLE_ADMIN` e `ROLE_SUPERADMIN`;
6. a senha PostgreSQL não é armazenada no ERP;
7. a aplicação continua usando `sa` como identidade técnica do datasource;
8. a conexão temporária do login PostgreSQL remove `sslcert`, `sslkey` e `sslrootcert` e força `sslmode=require`;
9. refresh token não é aceito como Bearer token de API.

O hardening posterior por certificado de cliente/mTLS continua separado dessa regra.

## 5. Módulos externos fornecidos para integração

Os diretórios informados devem ser tratados como integrações/microserviços e não simplesmente copiados para dentro do CRUD principal.

### RH
- `esocial` → integração eSocial; usar dentro do domínio RH.

### Fiscal / documentos
- `Java_Certificado` → certificados.
- `Java_CTe` → CT-e.
- `Java-Efd-Contribuicoes` → EFD-Contribuições.
- `Java-Efd-Icms` → EFD ICMS.
- `Java_MDFe` → MDF-e.
- `Java_NFe` → NF-e.
- `Java_Pdf_Signature` → assinatura PDF.
- `nfe` → entrada de nota de produto, conforme regra do projeto.
- `nfse` → NFSe nacional.
- `nfse_prefeitura_sp` → integração antiga/funcional da Prefeitura de São Paulo; usar o PDF atualizado da prefeitura como referência de contrato.
- `NFSe-SaoPaulo-SP` → material/instruções atualizadas de NFSe São Paulo.
- `nfse-sp-bridge` → bridge da NFSe São Paulo.

### Spring AI
- `spring-ai` → código/documentação do ecossistema Spring AI.
- A aplicação principal usa a dependência Spring AI/OpenAI diretamente no monólito; a pasta copiada em `microservices` não é, por si só, a execução do Spring AI do ERP.

## 6. Problemas reais encontrados

### P0 — Menu

O menu estava com labels visualmente apagados/brancos e a estilização do PrimeReact podia deixar conteúdo de submenu com contraste inadequado.

Foi reforçado o CSS do `PanelMenu` para:
- fundo escuro nos conteúdos;
- texto explícito em `#eaf1fb`;
- `opacity: 1`;
- `visibility: visible`;
- `-webkit-text-fill-color` explícito;
- hover separado.

Commit:
`6ceca90b0fbad443564c91a5536a811237ed4aa6`.

### P0 — Contratos IA

O frontend IA não correspondia aos paths atuais dos controllers.

Corrigido nos commits indicados na seção de IA.

### P1 — Documentação antiga

`docs/frontend/FRONTEND_ANALISE_COMPLETA.md` e `docs/auditorias/MAPA_COMPLETO_COBERTURA.md` de 21/09 afirmam números e faltas que já não representam exatamente o código atual.

Exemplo: o mapa antigo marca vários componentes como ausentes, mas eles já existem em `main`.

Esses documentos não devem ser usados isoladamente para decidir o que falta hoje.

## 7. Próxima matriz de implementação

A evolução do frontend deve ser orientada por **controller → endpoints → serviço JS → tela → operação**, e não por quantidade de arquivos.

Prioridade técnica:

1. validar todos os endpoints consumidos por cada tela contra o controller atual;
2. separar CRUD de cadastro de operação de negócio;
3. completar Financeiro avançado;
4. completar BI administrativo;
5. integrar eSocial ao RH;
6. integrar NF-e/CT-e/MDF-e/EFD/NFSe conforme os contratos dos microserviços;
7. transformar IA em módulo real usando Spring AI com configuração de modelo habilitada;
8. manter o backend existente como contrato, evitando alterações destrutivas nas APIs.

## 8. Regra de arquitetura

O backend continua sendo a fonte de verdade para:

- rotas REST;
- permissões;
- modelos;
- regras de negócio;
- integrações fiscais;
- autenticação.

O frontend deve se adaptar a esses contratos. Não devemos criar uma rota fictícia no React só porque uma documentação antiga menciona uma URL diferente.
