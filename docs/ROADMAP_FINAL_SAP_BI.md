# ROADMAP FINAL SAP BI - Brasil SaaS ERP
## Plano de Ação para Alcançar Nível SAP Business One

---

## 📊 Status Real Atual (O Que Já Existe)

### 1. Backend Sólido (Monólito + Módulos)
**Total: ~700 classes Java**

**Módulos Prontos:**
- ✅ Core (Autenticação, Autorização, Empresas, Usuários)
- ✅ Cadastro (Pessoas, Produtos, Clientes, Fornecedores, Categorias, Marcas, Serviços)
- ✅ Financeiro (Títulos, Lançamentos, Contas, Plano de Contas, Centro de Custo)
- ✅ Vendas (Pedidos, Orçamentos)
- ✅ Compras (Pedidos de Compra)
- ✅ Estoque (Saldos, Movimentações)
- ✅ Fiscal (Estrutura básica, NF-e/NFC-e/NFS-e estrutura)
- ✅ RH (Estrutura básica, Funcionários, Cargos)
- ✅ Serviços (Ordens de Serviço)
- ✅ Produção (Básico - Ordem de Produção, Apontamento)
- ✅ BI (Estrutura básica - Dashboards, Relatórios, Indicadores)
- ✅ IA (Estrutura básica - Chat, Prompts, Embeddings)

**Destaque:** Arquitetura híbrida funcionando. O módulo de Produção já tem entidades de Ordem e Apontamento, mas falta a lógica de explosão (BOM) e Roteiros complexos.

---

### 2. Frontend em Andamento (55 Componentes)

**Prontos:**
- ✅ Login (com MongoDB para imagens)
- ✅ Dashboard
- ✅ Financeiro (completo)
- ✅ Fiscal (básico)
- ✅ Cadastros principais (Pessoas, Produtos, Clientes, Fornecedores)
- ✅ Compras (completo)
- ✅ Estoque (completo)
- ✅ Vendas (completo)
- ✅ Serviços (completo)
- ✅ Produção (básico)

**Em Construção:**
- Core (completo)
- Fiscal detalhado (em andamento)
- RH (em andamento)

**Lacuna:** Faltam telas específicas de:
- ❌ Chão de Fábrica (apontamento operacional)
- ❌ Engenharia (criação de BOM/Lista de Materiais)
- ❌ Dashboards Gerenciais do BI

---

### 3. Banco de Dados & MongoDB

**PostgreSQL:**
- ✅ 46 Migrations (V1-V46) cobrindo o core transacional
- ✅ Schema principal: brasil_saas
- ✅ Tabelas para todos os módulos básicos

**MongoDB:**
- ✅ Funcional para Imagens (Login/Background)
- ❌ Coleções para documentos genéricos (PDF, XML, etc.)

**O Que Falta Aqui:**
- ❌ **DataLake BI:** Não existe schema separado nem tabelas de fatos/dimensões no Postgres
- ❌ **Gestão Documental Genérica:** Só tem imagem. Falta a entidade Documento genérica com metadados

---

## 🚀 O Que Falta Para Nível SAP Business One

> **Importante:** Para igualar o SAP, não é só "ter a tela", é ter a **regra de negócio profunda**.

---

### 1. Módulo Produção Industrial (O Coração da Indústria)
**Status Atual:** Tem tabela de Ordem de Produção e Apontamento.

**Falta (Crítico):**

#### 1.1. BOM Multinível (Lista de Materiais)
- **Estrutura:** Hierarquia pai-filho recursiva
- **Funcionalidades:**
  - Criar estruturas de produtos com múltiplos níveis
  - Explosão automática de BOM (calcular quantidades de insumos)
  - Versão de BOM (histórico de mudanças)
  - Alternativas de componentes (substituição)
- **Tabelas Necessárias:**
  - `bc_producao_bom` (cabeçalho)
  - `bc_producao_bom_item` (itens com relação pai-filho)
  - `bc_producao_bom_versao` (controle de versão)
  - `bc_producao_bom_alternativa` (componentes alternativos)

#### 1.2. Roteiros de Fabricação
- **Estrutura:** Sequência de operações para produzir um produto
- **Funcionalidades:**
  - Definição de operações (corte, usinagem, montagem, etc.)
  - Centros de custo por operação
  - Tempos padrão por operação
  - Máquinas/equipamentos por operação
  - Instruções de trabalho (textos, imagens, vídeos)
- **Tabelas Necessárias:**
  - `bc_producao_roteiro` (cabeçalho do roteiro)
  - `bc_producao_operacao` (operações do roteiro)
  - `bc_producao_centro_custo` (centros de custo)
  - `bc_producao_maquina` (máquinas/equipamentos)
  - `bc_producao_instrucao` (instruções de trabalho)

#### 1.3. MRP (Planejamento de Necessidades de Materiais)
- **Algoritmo:** Cruzamento de vendas + estoque + BOM
- **Funcionalidades:**
  - Cálculo de necessidades líquidas (demanda - estoque)
  - Sugestão de ordens de compra para itens comprados
  - Sugestão de ordens de produção para itens fabricados
  - Considerar lead times de fornecedores
  - Considerar lead times de produção
  - Considerar lotes mínimos de compra/produção
- **Services Necessários:**
  - `MrpService` (algoritmo principal)
  - `NecessidadeMaterialService` (cálculo de necessidades)
  - `SugestaoCompraService` (sugestões de compra)
  - `SugestaoProducaoService` (sugestões de produção)

#### 1.4. Custeio Industrial
- **Cálculo:** CPV (Custo dos Produtos Vendidos)
- **Componentes do Custo:**
  - Mão de obra direta (baseado em roteiros e tempos)
  - Material direto (baseado em BOM e preços)
  - Custos indiretos (rateio por centro de custo)
- **Funcionalidades:**
  - Cálculo de custo padrão por produto
  - Cálculo de custo real por ordem de produção
  - Análise de variação (padrão vs. real)
  - Rateio de custos indiretos
- **Tabelas Necessárias:**
  - `bc_producao_custo_padrao` (custos padrão)
  - `bc_producao_custo_real` (custos reais por ordem)
  - `bc_producao_rateio` (rateios de custos indiretos)

#### 1.5. Telas Frontend
- **Engenharia de Produto:**
  - `EngenhariaProdutos.jsx` - Gerenciamento de BOM
  - `BomVisualizer.jsx` - Visualizador de estrutura BOM
  - `Roteiros.jsx` - Gerenciamento de roteiros
  - `Operacoes.jsx` - Cadastro de operações

- **Planejamento MRP:**
  - `Mrp.jsx` - Execução do MRP
  - `NecessidadesMaterial.jsx` - Listagem de necessidades
  - `SugestoesCompra.jsx` - Sugestões de compra
  - `SugestoesProducao.jsx` - Sugestões de produção

- **Apontamento de Chão de Fábrica (PCP):**
  - `ApontamentoPcp.jsx` - Apontamento operacional
  - `OrdemProducaoPcp.jsx` - Gerenciamento de ordens em andamento
  - `TemposProducao.jsx` - Registros de tempos

---

### 2. BI & DataLake (Inteligência)
**Status Atual:** Pacote com estrutura básica (Dashboards, Relatórios, Indicadores).

**Falta:**

#### 2.1. Migration DataLake
- **Schema:** `bi_datalake` no PostgreSQL
- **Tabelas de Fatos:**
  - `ft_vendas` (fatos de vendas)
  - `ft_compras` (fatos de compras)
  - `ft_estoque` (fatos de estoque)
  - `ft_financeiro` (fatos financeiros)
  - `ft_producao` (fatos de produção)
- **Tabelas de Dimensões:**
  - `dm_tempo` (dimensão tempo - dia, semana, mês, ano)
  - `dm_cliente` (dimensão cliente)
  - `dm_fornecedor` (dimensão fornecedor)
  - `dm_produto` (dimensão produto)
  - `dm_categoria` (dimensão categoria)
  - `dm_empresa` (dimensão empresa)
  - `dm_unidade` (dimensão unidade de medida)

#### 2.2. ETL (Spring Batch)
- **Jobs Noturnos:**
  - `VendasEtlJob` - Extrair dados de vendas
  - `ComprasEtlJob` - Extrair dados de compras
  - `EstoqueEtlJob` - Extrair dados de estoque
  - `FinanceiroEtlJob` - Extrair dados financeiros
  - `ProducaoEtlJob` - Extrair dados de produção
- **Agendamento:** Cron expression para execução noturna
- **Incremental:** Somente dados novos/modificados

#### 2.3. Frontend: Dashboards Executivos
- **DRE (Demonstrativo de Resultados):**
  - `DashboardDre.jsx` - Visualização de receitas, despesas, lucro
  - Gráficos de evolução mensal/anual
  - Comparativo com orçamento

- **Giro de Estoque:**
  - `DashboardGiroEstoque.jsx` - Análise de giro por produto/categoria
  - Identificação de produtos lentos/parados
  - Cálculo de giro médio

- **Curva ABC:**
  - `DashboardCurvaAbc.jsx` - Classificação ABC de produtos
  - Análise de contribuição para faturamento
  - Identificação de produtos classe A, B, C

- **Painel Gerencial:**
  - `DashboardGerencial.jsx` - Visão consolidada
  - KPIs principais (faturamento, margem, estoque, etc.)
  - Alertas automáticos

---

### 3. Gestão Documental (ECM - Enterprise Content Management)
**Status Atual:** Apenas imagens no MongoDB (Login/Background).

**Falta:**

#### 3.1. Backend: Service Genérico
- **Entidade:** `Documento` (genérica)
- **Campos:**
  - `tipo` (PDF, DOCX, XML, XLSX, etc.)
  - `nome` (nome do arquivo)
  - `descricao` (descrição opcional)
  - `entidadeTipo` (tipo da entidade - ex: PEDIDO_VENDA, NOTA_FISCAL)
  - `entidadeId` (ID da entidade)
  - `caminho` (caminho no storage)
  - `tamanho` (tamanho em bytes)
  - `mimeType` (tipo MIME)
  - `checksum` (hash para integridade)
  - `criadoPor` (quem uploadou)
  - `dataCriacao` (quando foi uploadado)
- **Services:**
  - `DocumentoService` - CRUD de documentos
  - `DocumentoStorageService` - Armazenamento (MongoDB GridFS ou FileSystem)
  - `DocumentoDownloadService` - Download com streaming

#### 3.2. Frontend: Componente Universal
- **Componente:** `Anexos.jsx`
- **Funcionalidades:**
  - Upload de múltiplos arquivos
  - Listagem de anexos
  - Visualização/Download de arquivos
  - Exclusão de anexos
  - Preview para PDFs e imagens
- **Integração:** Componente reutilizável em todas as telas de detalhes

---

### 4. RH Profundo (Folha de Pagamento)
**Status Atual:** Cadastro de funcionários e cargos.

**Falta (para implementação futura):**
- Motor de cálculo de folha
- Cálculo de férias
- Cálculo de 13º salário
- Integração eSocial (aguardando certificado)
- Módulo de Ponto Eletrônico

---

### 5. Fiscal Completa (SPED)
**Status Atual:** Emissão de NF-e/NFC-e básica.

**Falta (para implementação futura - após certificado):**
- Geradores de SPED (ICMS/IPI)
- Geradores de SPED (PIS/COFINS)
- Geradores de SPED (Contribuições)
- ECD/ECF (Escrituração Contábil Digital)

---

## 📝 Resumo do Plano de Ação Imediato

**Prioridade 1 (Crítico para Gestão Industrial):**

1. **Criar Migration do DataLake**
   - Schema `bi_datalake` no PostgreSQL
   - Tabelas de fatos e dimensões
   - Índices otimizados para queries analíticas

2. **Explosão do Módulo Produção**
   - Criar classes de BOM (Estrutura, Item, Versão, Alternativa)
   - Criar classes de Roteiro (Roteiro, Operação, Centro de Custo, Máquina, Instrução)
   - Implementar lógica do MRP (algoritmo de planejamento)
   - Implementar custeio industrial (cálculo de CPV)

3. **Gestão Documental**
   - Implementar entidade Documento genérica
   - Service de upload/download
   - Integração com MongoDB GridFS

4. **Frontend de Produção**
   - Telas de Engenharia (BOM, Roteiros)
   - Telas de Planejamento (MRP, Sugestões)
   - Telas de Apontamento (PCP)

**Prioridade 2 (BI e Dashboards):**

5. **ETL com Spring Batch**
   - Jobs para extração de dados
   - Agendamento noturno
   - Processamento incremental

6. **Dashboards Executivos**
   - DRE
   - Giro de Estoque
   - Curva ABC
   - Painel Gerencial

---

## 🎯 Conclusão

**O sistema está 90% pronto na infraestrutura, mas apenas 60% pronto nas regras de negócio industriais complexas que diferenciam um ERP "de cadastro" de um ERP "de gestão industrial (SAP)".**

### Próximos Passos Imediatos:
1. ✅ **Já feito:** Módulos básicos (Compras, Estoque, Vendas, Serviços) - 100% completos
2. ⏳ **Em andamento:** Core, Fiscal detalhado, RH - Finalização
3. 🎯 **Próximo:** DataLake + Produção Industrial + Gestão Documental

### Dependências:
- **SPED/Fiscal Completo:** Aguardando certificado digital
- **eSocial:** Aguardando certificado digital

---

## 📅 Timeline Estimado

| Fase | Duração | Entregáveis |
|------|---------|-------------|
| **Fase 1: DataLake** | 1 semana | Schema BI, Tabelas, ETL básico |
| **Fase 2: Produção Industrial** | 2 semanas | BOM, Roteiros, MRP, Custeio |
| **Fase 3: Gestão Documental** | 3 dias | Service genérico, Frontend |
| **Fase 4: Frontend Produção** | 1 semana | Telas de Engenharia, MRP, PCP |
| **Fase 5: BI Dashboards** | 1 semana | DRE, Giro Estoque, Curva ABC |
| **Total** | **5-6 semanas** | Sistema SAP-like completo |

---

## 🔗 Arquivos Relacionados
- [docs/relatorios/RESUMO_FINAL_COMPLETO.md](docs/relatorios/RESUMO_FINAL_COMPLETO.md) - Resumo geral do projeto
- [docs/roadmap.md](docs/roadmap.md) - Roadmap original
- [docs/modulos/producao.md](docs/modulos/producao.md) - Detalhes do módulo Produção
- [docs/modulos/bi.md](docs/modulos/bi.md) - Detalhes do módulo BI

---

**Data:** 21/09/2026  
**Status:** Plano de Ação Definido  
**Próximo:** Aguardando OK nas telas atuais para iniciar Produção e DataLake
