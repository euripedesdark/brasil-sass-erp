> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

# Matriz Frontend x Backend — Brasil SaaS ERP

**Atualização:** 24/09/2026

## 1. Diagnóstico

A auditoria do código atual mostrou que o problema não era apenas quantidade de componentes React.

O backend possui uma superfície funcional muito maior do que a navegação do frontend estava expondo.

### Backend

Na árvore atual de src/main/java/br/com/brasil_saas existem áreas como:

- core
- cadastro
- compras
- estoque
- financeiro
- fiscal
- produção
- RH
- serviços
- vendas
- BI
- IA
- integrações
- portais
- shared

Nos módulos de negócio auditados a auditoria atual encontrou **63 controllers** nos módulos funcionais de `core`, `cadastro`, `compras`, `estoque`, `financeiro`, `fiscal`, `produção`, `RH`, `serviços`, `vendas`, `BI` e `IA`.

A documentação anterior também registra uma base de centenas de classes Java; portanto, o número de telas precisa ser avaliado pela cobertura funcional e não somente pela quantidade de arquivos JSX.

## 2. Frontend encontrado

A árvore atual possui **58 arquivos JSX** dentro de src/main/resources/static/react/src.

Isso inclui componentes de infraestrutura da interface, como App, Layout, Login, contextos, widgets e componentes de logo/foto, além dos componentes funcionais de negócio.

### Componentes funcionais relevantes já existentes

#### Cadastros
- Pessoas
- Produtos
- Categorias
- Clientes
- Fornecedores
- Marcas
- Serviços
- Transportadoras
- Unidades de Medida
- Municípios

#### Financeiro
- Visão financeira
- Caixa
- Comissões
- Condição de Pagamento
- Conta Bancária
- Extrato
- Lançamento Contábil
- Plano de Contas
- Tipo de Pagamento
- Títulos

#### Fiscal
- NCM
- CFOP
- CEST
- ISSQN
- Entrada de Nota
- Consulta SEFAZ

#### RH
- Colaboradores
- Cargos
- Folha de Pagamento

#### Operações
- Vendas
- Compras
- Estoque
- Serviços
- Ordens de Serviço
- Produção
- Romaneio de Produção

#### Gestão
- Relatórios
- BI
- IA
- Perfil
- Administração
- Dashboard

## 3. Cobertura por domínio

A contagem de controllers não deve ser comparada 1:1 com JSX: vários controllers são APIs auxiliares consumidas por uma única tela (por exemplo logos, fotos, movimentações e chat). Ainda assim, há lacunas funcionais reais:

| Domínio | Controllers | Cobertura atual do frontend | Observação |
|---|---:|---|---|
| Cadastro | 12 | Alta | Logos são suporte às telas de cliente/fornecedor. |
| Compras | 1 | Alta | Pedido de compra concentrado em `Compras.jsx`. |
| Vendas | 1 | Alta | Pedido de venda concentrado em `Vendas.jsx`. |
| Estoque | 2 | Alta | Movimentação é integrada em `Estoque.jsx`. |
| Financeiro | 8 | Alta | Há telas específicas para os principais recursos. |
| Fiscal | 7 | Parcial | `ImpostoController` ainda não possui tela dedicada. |
| Produção | 3 | Parcial | Apontamento de produção ainda não possui tela dedicada. |
| RH | 4 | Alta | Funcionários, cargos e folha são cobertos pelas telas existentes. |
| Serviços | 1 | Alta | Ordem de serviço possui tela. |
| Core | 8 | Alta/infra | Parte é autenticação, auditoria, logos e administração. |
| BI | 6 | Parcial | `BI.jsx` cobre dashboards/indicadores, mas KPIs e relatórios/agendamentos ainda precisam de telas próprias. |
| IA | 9 | Parcial | `IA.jsx` cobre chat/sessões/mensagens; configuração, análises, classificações, embeddings e prompts ainda precisam de telas próprias. |

O principal déficit de produto está, portanto, em **IA, BI e algumas funções Fiscal/Produção**, e não simplesmente em quantidade de arquivos React.

## 4. O problema encontrado na navegação

Antes desta correção, o Layout.jsx expunha somente uma fração dos componentes existentes.

Vários componentes já existentes no repositório não tinham entrada no menu.

Além disso, o App.jsx registrava somente algumas rotas de alto nível. Componentes financeiros, fiscais, RH, cadastro, BI e IA existentes não estavam devidamente registrados como rotas individuais.

Isso criava uma diferença entre:

**Backend implementado → Frontend existente → Rotas → Menu**

O resultado era uma interface que parecia ter muito menos funcionalidade do que o sistema realmente possui.

## 5. Correção aplicada

O menu lateral voltou a usar **PrimeReact PanelMenu**, alinhado à documentação do projeto em docs/frontend/STATUS_PRIME_REACT.md e docs/frontend/FRONTEND_ANALISE_COMPLETA.md.

Foram adicionadas entradas para:

- Administração
- Cadastros
- Vendas
- Compras
- Estoque
- Financeiro
- Fiscal
- Produção
- Serviços
- Recursos Humanos
- Inteligência & Análise
- Dashboard

O Financeiro agora expõe suas funcionalidades existentes, incluindo:

- Contas a Pagar e Receber
- Títulos
- Comissões
- Caixa
- Extratos
- Contas Bancárias
- Plano de Contas
- Lançamentos Contábeis
- Centro de Custos
- Condições de Pagamento
- Tipos de Pagamento

O Fiscal agora expõe:

- NCM
- CFOP
- CEST
- ISSQN
- Entradas de NF
- Consulta SEFAZ

O RH agora expõe:

- Colaboradores
- Cargos
- Folha de Pagamento

Produção, Serviços, Cadastros, BI e IA também passaram a ter entradas próprias.

## 6. Rotas

O App.jsx foi ampliado para registrar os componentes funcionais que já existiam no repositório.

Exemplos:

    /cadastro/clientes
    /cadastro/fornecedores
    /cadastro/categorias
    /cadastro/marcas
    /cadastro/servicos
    /cadastro/transportadoras
    /cadastro/unidades-medida

    /financeiro/lancamentos
    /financeiro/titulos
    /financeiro/comissoes
    /financeiro/caixa
    /financeiro/extrato
    /financeiro/contas-bancarias
    /financeiro/plano-contas
    /financeiro/contabil
    /financeiro/centro-custos
    /financeiro/condicoes-pagamento
    /financeiro/tipos-pagamento

    /fiscal/ncm
    /fiscal/cfop
    /fiscal/cest
    /fiscal/issqn
    /fiscal/entradas
    /fiscal/sefaz

    /rh
    /rh/cargos
    /rh/folha

    /producao
    /producao/romaneios

    /ordens-servico
    /bi
    /ia
    /relatorios

## 7. Problema visual do texto do menu

O projeto já usava PrimeReact, mas o CSS da navegação não garantia explicitamente a cor dos elementos internos gerados pelo PanelMenu.

Foram adicionadas regras específicas para:

- .p-panelmenu-header-link
- .p-menuitem-link
- .p-menuitem-text
- .p-menuitem-icon
- .p-submenu-icon

Os textos agora usam explicitamente as variáveis visuais do Brasil SaaS ERP, evitando que o tema do PrimeReact faça o texto desaparecer ou ficar ilegível.

## 8. Autenticação

A autenticação continua separada da autorização.

### Usuário ERP

    username + senha
          |
          v
    bc_core_usuario.senha_hash
          |
          v
        BCrypt
          |
          v
    perfil + permissões
          |
          v
         JWT

### PostgreSQL SUPERUSER

Uma role PostgreSQL somente pode substituir o BCrypt quando:

    PostgreSQL autentica
           +
    pg_roles.rolsuper = true
           |
           v
       SUPERADMIN

A senha PostgreSQL:

- não é armazenada no ERP;
- não é colocada no JWT;
- não substitui o cadastro de perfil;
- é usada somente na conexão temporária de autenticação.

O usuário SUPERUSER recebe ROLE_ADMIN e ROLE_SUPERADMIN e pode acessar as funções administrativas.

A conexão técnica da aplicação continua separada da autenticação do usuário.

O datasource usa a identidade técnica sa com certificado.

Na autenticação de um SUPERUSER:

- sslcert é removido;
- sslkey é removido;
- sslrootcert é removido;
- sslmode=require é forçado.

Isso impede que o certificado da conta técnica sa seja reutilizado para autenticar outra role.

O hardening com certificado de cliente por role permanece planejado para uma etapa posterior.

## 9. JWT

O sistema mantém dois tokens:

- accessToken
- refreshToken

O refreshToken é aceito somente em POST /api/auth/refresh.

Ele não pode ser usado como Authorization: Bearer <refreshToken> nas APIs protegidas.

## 10. Erro de build local observado em 24/09/2026

O checkout local apresentou:

    Could not resolve "../contexts/AuthContext" from "src/components/fiscal/EntradaNota.jsx"

A versão atual de `main` já usa o caminho correto em `EntradaNota.jsx`:

    import { useAuth } from '../../contexts/AuthContext';

Como `fiscal/EntradaNota.jsx` está dois níveis abaixo de `src`, `../../contexts/AuthContext` é o caminho correto. Se o erro continuar localmente, o checkout local está atrás do `main` ou possui uma alteração local não commitada. Depois de sincronizar, executar:

    git pull --ff-only origin main
    cd src/main/resources/static/react
    npm run build

Não alterar o backend para contornar esse erro: é um erro de resolução de módulo do frontend.

## 11. Documentação de autenticação

A descrição detalhada do fluxo está em:

    docs/autenticacao.md
    docs/arquitetura.md

Esses documentos devem ser tratados como referência para futuras alterações na autenticação.

## 12. Regra para evolução

Novas funcionalidades de backend não devem ser consideradas concluídas do ponto de vista do produto até que exista, quando aplicável:

1. serviço/API;
2. componente React;
3. rota;
4. entrada no menu;
5. autorização correspondente;
6. tratamento de erro/loading;
7. documentação mínima.

A meta é evitar novamente o cenário em que o backend possui a função, o componente React existe, mas o usuário não consegue chegar à função pela interface.
