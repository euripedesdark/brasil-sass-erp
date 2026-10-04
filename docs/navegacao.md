# Navegação e Menus — Brasil SaaS ERP

> Atualizado em 04/10/2026 — rotas verificadas no `App.jsx`.

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

## Rotas do sistema (App.jsx)

### Núcleo e autenticação

| Rota | Componente | Descrição |
|------|-----------|-----------|
| `/login` | `Login.jsx` | Tela de login |
| `/` | `Dashboard.jsx` | Dashboard principal |
| `/inicio` | — | Página inicial |
| `/perfil` | `Perfil.jsx` | Perfil do usuário |
| `/sobre` | — | Sobre o sistema |
| `/configurar-empresa` | — | Configuração da empresa |

### Administração

| Rota | Descrição |
|------|-----------|
| `/admin/usuarios` | Gestão de usuários |
| `/admin/sql` | Gerenciador SQL |
| `/admin/endpoints` | Lista de endpoints |
| `/admin/armazenamento` | Armazenamento |
| `/admin/configuracoes` | Configurações |
| `/admin/paridade-erp` | Paridade ERP |

### Cadastros

| Rota | Descrição |
|------|-----------|
| `/cadastro/pessoas` | Pessoas (PF/PJ) |
| `/cadastro/clientes` | Clientes |
| `/cadastro/fornecedores` | Fornecedores |
| `/cadastro/produtos` | Produtos |
| `/cadastro/servicos` | Serviços |
| `/cadastro/categorias` | Categorias |
| `/cadastro/marcas` | Marcas |
| `/cadastro/unidades-medida` | Unidades de medida |
| `/cadastro/transportadoras` | Transportadoras |
| `/cadastro/municipios` | Municípios (IBGE) |
| `/cadastro/bancos` | Bancos |

### Financeiro

| Rota | Descrição |
|------|-----------|
| `/financeiro` | Visão geral financeira |
| `/financeiro/aprovacoes-titulos` | Aprovações de títulos |
| `/financeiro/boletos` | Boletos |
| `/financeiro/caixa` | Caixa |
| `/financeiro/emprestimos` | Empréstimos |
| `/financeiro/orcamento` | Orçamento |
| `/financeiro/renegociacao` | Renegociacao |

### Estoque

| Rota | Descrição |
|------|-----------|
| `/estoque/depositos` | Depósitos |
| `/estoque/enderecos` | Endereços |
| `/estoque/expedicoes` | Expedições |
| `/estoque/inventarios` | Inventários |
| `/estoque/lotes` | Lotes |
| `/estoque/movimentacoes` | Movimentações |
| `/estoque/reservas` | Reservas |
| `/estoque/transferencias` | Transferências |

### Vendas e Compras

| Rota | Descrição |
|------|-----------|
| `/vendas/pdv` | Ponto de venda |
| `/vendas/tabelas-preco` | Tabelas de preço |
| `/compras/supply-chain` | Supply chain |
| `/compras/recebimentos` | Recebimentos |
| `/compras/conferencia-faturas` | Conferência de faturas |

### Produção

| Rota | Descrição |
|------|-----------|
| `/producao/apontamentos` | Apontamentos |
| `/producao/capacidade` | Capacidade |
| `/producao/estrutura` | Estrutura de produto |
| `/producao/mrp` | MRP |
| `/producao/romaneios` | Romaneios |
| `/producao/roteiros` | Roteiros |

### Fiscal

| Rota | Descrição |
|------|-----------|
| `/fiscal/entradas` | Entradas de notas |
| `/fiscal/impostos` | Impostos |
| `/fiscal/issqn` | ISSQN |
| `/fiscal/ncm` | NCM |
| `/fiscal/nfse` | NFS-e |
| `/fiscal/sefaz` | SEFAZ |
| `/fiscal/sped` | SPED |
| `/fiscal/busca` | Busca fiscal |
| `/fiscal/cte-mdfe` | CT-e / MDF-e |

### Demais módulos

| Rota | Descrição |
|------|-----------|
| `/crm` | CRM (leads, pipeline, forecast) |
| `/bi` | BI (dashboards, KPIs, relatórios) |
| `/bi/auditoria-funcional` | Auditoria funcional |
| `/bi/kpis` | KPIs |
| `/bi/relatorios` | Relatórios |
| `/bi/relatorios-agendados` | Relatórios agendados |
| `/rh/cargos` | Cargos |
| `/rh/folha` | Folha de pagamento |
| `/rh/fotos` | Fotos de funcionários |
| `/servicos/ordens` | Ordens de serviço |
| `/projetos` | Projetos |
| `/qualidade` | Qualidade |
| `/ativos` | Ativos |
| `/dms` | DMS (gestão de documentos) |
| `/documentos` | Documentos |
| `/wms` | WMS (warehouse) |
| `/workflow` | Workflow |
| `/portais` | Portais |
| `/portal` | Portal |
| `/ia` | IA assistiva |
| `/assistente` | Assistente IA |
| `/doacoes` | Doações |
| `/relatorios` | Relatórios |
