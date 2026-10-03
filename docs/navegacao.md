# Navegação e Menus — Brasil SaaS ERP

## Objetivo

Este documento registra a estrutura de navegação do frontend React e estabelece uma regra simples: **menu, rota e componente precisam apontar para a mesma funcionalidade real**.

O menu lateral usa **PrimeReact PanelMenu**. Não deve ser substituído por uma navegação HTML paralela para corrigir um submenu isolado.

## Fonte de verdade

A cadeia oficial é:

1. controller/API do backend;
2. serviço JavaScript;
3. componente React;
4. rota em `App.jsx`;
5. item do `PanelMenu`.

Documentação funcional antiga pode orientar nomenclatura, mas não deve criar rotas que não existam no `App.jsx`.

## Menu lateral atual

### Dashboard
- Dashboard → `/dashboard`

### Administração
Disponível para usuários com `isAdmin` ou `isDiretoria`.
- Usuários e Permissões → `/admin/usuarios`
- Imagens do Sistema → `/admin/configuracoes`
- Meu Perfil → `/perfil`
- Console SQL → `/admin/sql` (somente `isAdmin`)

### Cadastros
- Pessoas → `/cadastro/pessoas`
- Clientes → `/cadastro/clientes`
- Fornecedores → `/cadastro/fornecedores`
- Produtos → `/cadastro/produtos`
- Categorias → `/cadastro/categorias`
- Marcas → `/cadastro/marcas`
- Serviços → `/cadastro/servicos`
- Transportadoras → `/cadastro/transportadoras`
- Unidades de Medida → `/cadastro/unidades-medida`
- Municípios IBGE → `/cadastro/municipios`

### Vendas
- Pedidos de Venda → `/vendas`

### Compras
- Pedidos de Compra → `/compras`

### Estoque
- Produtos e Saldos → `/estoque`

### Financeiro
- Visão Geral → `/financeiro`
- Contas a Pagar e Receber → `/financeiro/lancamentos`
- Títulos → `/financeiro/titulos`
- Comissões → `/financeiro/comissoes`
- Caixa → `/financeiro/caixa`
- Extratos → `/financeiro/extrato`
- Contas Bancárias → `/financeiro/contas-bancarias`
- Plano de Contas → `/financeiro/plano-contas`
- Lançamentos Contábeis → `/financeiro/contabil`
- Centro de Custos → `/financeiro/centro-custos`
- Condições de Pagamento → `/financeiro/condicoes-pagamento`
- Tipos de Pagamento → `/financeiro/tipos-pagamento`

### Fiscal
- NCM → `/fiscal/ncm`
- CFOP → `/fiscal/cfop`
- CEST → `/fiscal/cest`
- ISSQN → `/fiscal/issqn`
- Entradas de NF → `/fiscal/entradas`
- Impostos → `/fiscal/impostos`
- Consulta SEFAZ → `/fiscal/sefaz`

### Produção
- Ordens de Produção → `/producao`
- Romaneios de Produção → `/producao/romaneios`
- Apontamentos de Produção → `/producao/apontamentos`

### Serviços
- Cadastro de Serviços → `/cadastro/servicos`
- Ordens de Serviço → `/ordens-servico`

### Recursos Humanos
- Colaboradores → `/rh`
- Cargos → `/rh/cargos`
- Folha de Pagamento → `/rh/folha`

### Inteligência & Análise
- Business Intelligence → `/bi`
- Inteligência Artificial → `/ia`
- Relatórios → `/relatorios`

## Rotas e cobertura

`App.jsx` possui rotas específicas para os componentes funcionais existentes. Alguns módulos também aceitam rotas curingas, por exemplo `/vendas/*`, `/compras/*`, `/estoque/*`, `/rh/*`, `/producao/*` e `/relatorios/*`.

Não criar um item de menu para uma API que ainda não tenha componente React correspondente. Quando uma nova tela for implementada, a ordem é:

**API → service → componente → rota → menu → teste de build.**

## Problema visual do menu

O projeto usa o tema Lara Light do PrimeReact, mas o menu tem identidade visual própria. O CSS força contraste para `.p-panelmenu-header-link`, `.p-menuitem-link`, `.p-menuitem-text`, ícones e submenus.

O `PanelMenu` **não deve receber `unstyled`**, porque isso remove a base visual do componente e aumenta o risco de incompatibilidade entre a estrutura gerada pelo PrimeReact e o CSS do projeto.

A correção atual mantém o componente PrimeReact com o tema carregado e aplica o CSS empresarial por cima.

## Regra para manutenção

Ao alterar a navegação:

1. conferir o controller/API;
2. conferir o service JS;
3. conferir o componente React;
4. conferir `App.jsx`;
5. conferir `Layout.jsx`;
6. executar `npm run build`;
7. executar `mvn clean package -DskipTests`;
8. atualizar `static/dist` antes de testar a aplicação empacotada.

Assim evitamos o cenário em que uma tela existe no backend e até no React, mas fica inacessível ou quebra por uma rota incorreta.
