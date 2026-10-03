# 🚀 Roadmap Final: Brasil SaaS ERP → Nível SAP + BI com Data Lake

**Data de Atualização:** 21/09/2026  
**Status Atual:** ~75% do Core ERP Implementado  
**Objetivo:** Alcançar funcionalidades comparáveis ao SAP Business One + Módulo BI Enterprise com Data Lake dedicado.

---

## 📊 Visão Geral do Gap (O que falta)

| Área | Status Atual | Meta SAP | Gap a Preencher | Prioridade |
|------|--------------|----------|-----------------|------------|
| **Cadastros** | ✅ 100% | 100% | Nenhum | - |
| **Vendas** | 🔄 80% | 100% | Pedidos complexos, tabelas de preço | 🔴 Alta |
| **Compras** | 🔄 70% | 100% | Cotações, RFQ, recebimento cego | 🟡 Média |
| **Estoque** | 🔄 75% | 100% | WMS, endereçamento, lotes | 🔴 Alta |
| **Produção** | ⚠️ 30% | 100% | MRP, BOM, Roteiros, Apontamento | 🔴 Crítica |
| **Fiscal** | ⚠️ 40% | 95% | SPED, EFD, ECD, ECF (Pendente Certificado) | ⚪ Baixa (Aguardando Cert) |
| **Financeiro** | 🔄 80% | 100% | Fluxo de caixa, conciliação avançada | 🟡 Média |
| **RH/Folha** | ⚠️ 30% | 100% | Folha completa, eSocial, Ponto | 🟡 Média |
| **Ativo Imobilizado** | ❌ 0% | 100% | Depreciação, manutenção | 🟢 Baixa |
| **Qualidade** | ❌ 0% | 90% | Inspeção, NC, ISO | 🟢 Baixa |
| **BI & Analytics** | ❌ 0% | 100% | Data Lake, Cubos, Dashboards | 🔴 Alta (Estratégico) |
| **IA Assistiva** | ❌ 0% | 80% | Chatbot, Predição | 🟢 Futuro |

---

## 🏗️ 1. Módulo de Produção Industrial (Nível SAP)

**Objetivo:** Transformar o módulo atual (básico) em um sistema MRP II completo.

### 1.1. Estrutura de Dados (Migrations V47-V52)
- [ ] **V47:** `engenharia_produto` (BOM Multinível)
  - Tabela: `prod_estruturas` (pai, filho, quantidade, perda, subconjunto)
  - Tabela: `prod_versoes` (histórico de alterações de engenharia)
- [ ] **V48:** `roteiros_fabricacao`
  - Tabela: `prod_roteiros` (sequência de operações)
  - Tabela: `prod_operacoes` (centro de custo, tempo setup, tempo unitário)
  - Tabela: `prod_maquinas_recursos` (capacidade disponível)
- [ ] **V49:** `mrp_calculos` (Motor de Planejamento)
  - Tabela: `mrp_demanda` (vendas + previsões)
  - Tabela: `mrp_sugestoes` (comprar vs produzir)
  - Tabela: `mrp_parametros` (lote mínimo, lead time, estoque segurança)
- [ ] **V50:** `apontamento_chao_fabrica`
  - Tabela: `prod_apontamentos` (quantidade produzida, refugo, operador)
  - Tabela: `prod_ordens_fase` (status por operação)
- [ ] **V51:** `custeio_producao`
  - Tabela: `custo_padrao` (material + MO + CIF)
  - Tabela: `custo_real` (apontado vs previsto)
- [ ] **V52:** `ordens_producao_avancado`
  - Campos extras na tabela atual: `prioridade`, `data_prometida`, `status_detail`

### 1.2. Backend (Java - Pacote `producao`)
- [ ] `BomService.java`: Explosão de BOM (recursivo), cálculo de necessidade líquida.
- [ ] `MrpEngine.java`: Rodagem noturna de MRP (demanda x suprimento).
- [ ] `RoteiroService.java`: Balanceamento de linha, cálculo de capacidade.
- [ ] `ApontamentoService.java`: Registro de produção em tempo real, baixa de componentes.
- [ ] `CusteioService.java`: Rateio de custos indiretos, análise de variância.

### 1.3. Frontend (React - Pasta `producao/`)
- [ ] `EngenhariaProduto.jsx`: Árvore BOM visual (drag-and-drop), versionamento.
- [ ] `RoteirosFab.jsx`: Editor de sequências operacionais, definição de máquinas.
- [ ] `PlanejamentoMRP.jsx`: Dashboard de sugestões de compra/produção, simulação "What-if".
- [ ] `ChaoDeFabrica.jsx`: Tela de apontamento (touch-friendly), status das OPs.
- [ ] `AnaliseCustos.jsx`: Comparativo Custo Padrão vs Real, curvas ABC de produção.

---

## 📦 2. Estoque Avançado (WMS Light)

**Objetivo:** Sair do controle de saldo simples para gestão de endereçamento e rastreabilidade.

### 2.1. Estrutura de Dados (Migrations V53-V55)
- [ ] **V53:** `wms_enderecos`
  - Tabela: `est_enderecos` (rua, prateleira, nível, tipo, capacidade)
  - Tabela: `est_regras_enderecamento` (fixo, aleatório, FIFO)
- [ ] **V54:** `lotes_validades`
  - Tabela: `est_lotes` (número lote, data fab, val, fornecedor)
  - Tabela: `est_saldos_lote` (saldo por endereço e lote)
- [ ] **V55:** `rastreabilidade`
  - Tabela: `est_movimentacoes_det` (origem, destino, usuário, motivo)

### 2.2. Backend (Pacote `estoque`)
- [ ] `EnderecamentoService.java`: Sugestão automática de guarda/picking.
- [ ] `LoteService.java`: Controle de validade (FEFO), bloqueio de lotes.
- [ ] `InventarioService.java`: Rotinas de contagem cega, conferência.

### 2.3. Frontend (React - Pasta `estoque/`)
- [ ] `MapaArmazem.jsx`: Visualização gráfica dos endereços (ocupado/livre).
- [ ] `GestaoLotes.jsx`: Consulta por validade, histórico de rastreio.
- [ ] `Inventario.jsx`: Interface para coletores de dados (mobile web).

---

## 💰 3. Financeiro Gerencial & Contábil

**Objetivo:** Suporte a múltiplos centros de custo e fluxo de caixa projetado.

### 3.1. Estrutura de Dados (Migrations V56-V58)
- [ ] **V56:** `centros_custo_lucro`
  - Tabela: `fin_centros_custo` (hierarquia ilimitada)
  - Tabela: `fin_rateios` (regras automáticas de distribuição)
- [ ] **V57:** `fluxo_caixa_projetado`
  - Tabela: `fin_previssoes` (receitas/despesas futuras recorrentes)
  - Tabela: `fin_cenarios` (otimista, pessimista, real)
- [ ] **V58:** `conciliacao_bancaria_avancada`
  - Tabela: `fin_arquivos_ofx` (importação bruta)
  - Tabela: `fin_regras_conciliacao` (matching automático)

### 3.2. Frontend
- [ ] `FluxoCaixa.jsx`: Gráfico de entradas/saídas projetadas (12 meses).
- [ ] `DRE_Gerencial.jsx`: Demonstrativo de resultados por centro de custo.

---

## 👥 4. RH Completo (Folha & eSocial)

**Objetivo:** Processamento de folha e obrigações acessórias trabalhistas.

### 4.1. Estrutura de Dados (Migrations V59-V62)
- [ ] **V59:** `folha_pagamento_base`
  - Tabelas de verbas, eventos, faixas salariais.
- [ ] **V60:** `ponto_eletronico`
  - Registro de batidas, tratamento de marcas, banco de horas.
- [ ] **V61:** `beneficios_terceiros`
  - VT, VR, Plano de Saúde (cálculos automáticos).
- [ ] **V62:** `esocial_eventos` (Aguardando certificado para implementação da lógica de envio).

---

## 🏭 5. Ativo Imobilizado & Manutenção

### 5.1. Estrutura (Migrations V63-V64)
- [ ] **V63:** `ativo_imobilizado`
  - Tabela: `ati_bens` (data aquisição, vida útil, método depreciação).
  - Tabela: `ati_depreciacao_mes` (lançamento automático mensal).
- [ ] **V64:** `manutencao_ativos`
  - Ordens de serviço internas para manutenção de máquinas.

---

## 🔬 6. Gestão da Qualidade

### 6.1. Estrutura (Migrations V65-V66)
- [ ] **V65:** `qualidade_inspecao`
  - Planos de amostragem (ISO 2859), critérios de aceitação.
- [ ] **V66:** `nao_conformidade`
  - Registro de NCs, ações corretivas (8D), causas raízes.

---

## 🧠 7. BI Enterprise com Data Lake Dedicado (PostgreSQL)

**Arquitetura Proposta:**
O módulo de BI não lerá diretamente das tabelas transacionais (OLTP). Ele utilizará um **Data Lake relacional** no mesmo PostgreSQL, mas em um schema separado (`bi_datalake`), alimentado por jobs assíncronos (ETL). Isso garante performance e histórico (snapshot).

### 7.1. Infraestrutura de Dados (Migrations V67-V70)
- [ ] **V67:** `bi_schema_setup`
  - Criação do schema `bi_datalake`.
  - Criação de usuários dedicados (leitura apenas para o BI).
- [ ] **V68:** `bi_fatos_vendas`
  - Tabela fatiada: `fato_vendas_diario` (agregado por dia, produto, cliente).
  - Tabela fatiada: `fato_vendas_item` (granularidade máxima).
- [ ] **V69:** `bi_fatos_estoque_financ`
  - `fato_saldo_estoque_diario` (evolução do estoque).
  - `fato_fluxo_financeiro` (realizado vs previsto).
- [ ] **V70:** `bi_dimensoes`
  - `dim_tempo` (calendário fiscal completo).
  - `dim_cliente_segmentado`, `dim_produto_categoria`, `dim_vendedor_meta`.

### 7.2. Motor ETL (Backend - Pacote `bi`)
- [ ] `EtlnJobScheduler.java`: Agendamento (ex: toda noite às 02:00).
- [ ] `SalesEtL.java`: Extrai vendas do dia, transforma (regras de negócio), carrega no `bi_datalake`.
- [ ] `StockSnapshotJob.java`: Tira "foto" do saldo atual para histórico.
- [ ] `ConsolidationService.java`: Pré-cálculo de cubos OLAP (agregações rápidas).

### 7.3. Frontend BI (React - Pasta `bi/`)
- [ ] `DashboardExecutive.jsx`: KPIs principais (Faturamento, Margem, Giro).
- [ ] `CubeExplorer.jsx**: Explorador de dados dinâmico (Drag & Drop de dimensões).
- [ ] `RelatorioPersonalizado.jsx`: Construtor de relatórios ad-hoc.
- [ ] `AlertasInteligentes.jsx**: Configuração de gatilhos (ex: venda abaixo da meta).

---

## 🤖 8. IA Assistiva (Opcional/Futuro)

- [ ] Chatbot para consultas ("Qual o saldo do produto X?").
- [ ] Previsão de demanda (Séries temporais).
- [ ] OCR para notas fiscais de entrada.

---

## 📅 Cronograma Sugerido (Sprints de 2 semanas)

| Sprint | Foco | Entregáveis Principais |
|--------|------|------------------------|
| **Sprint 1** | **Produção Core** | BOM, Roteiros, Estrutura de OPs. |
| **Sprint 2** | **MRP & Chão de Fábrica** | Motor MRP, Apontamento, Back-end de Custeio. |
| **Sprint 3** | **WMS & Qualidade** | Endereçamento, Lotes, Inspeção de Entrada. |
| **Sprint 4** | **Financeiro & RH** | Centros de Custo, Folha Básica, Ponto. |
| **Sprint 5** | **BI Data Lake (Base)** | Schema BI, Jobs ETL Vendas/Estoque. |
| **Sprint 6** | **BI Dashboards** | Frontend BI, Cubos, Relatórios Gerenciais. |
| **Sprint 7** | **Ativo & Consolidação** | Imobilizado, Fechamento de Sprint. |
| **Sprint 8** | **Polimento & Fiscal** | Ajustes finais, Preparação para SPED (quando tiver cert). |

---

## 🛠️ Tecnologias & Ferramentas Necessárias

1.  **Backend:** Spring Boot 3, Spring Batch (para ETL do BI), Quartz (Agendamento).
2.  **Database:** PostgreSQL 16+ (Recursos de JSONB e Particionamento para o Data Lake).
3.  **Frontend:** React 19, Recharts ou ApexCharts (Gráficos BI), AG-Grid (Tabelas pesadas).
4.  **Infra:** Docker Compose para subir o ambiente completo de BI.

---

## ⚠️ Notas Importantes sobre o Fiscal (SPED)

- **Bloqueio Atual:** A implementação lógica de geração de arquivos SPED (ECD, ECF, EFD Contribuições/ICMS) está condicionada à obtenção do **Certificado Digital A1/A3**.
- **Plano de Ação:**
  1.  Manter as migrations de estrutura fiscal prontas (já existentes em parte).
  2.  Focar nas regras de negócio internas (cálculo de impostos) sem gerar o arquivo final.
  3.  Assim que o certificado estiver disponível, implementar a classe `SpedGenerator` e os validadores da Receita Federal.

---

## ✅ Checklist de Conclusão "Nível SAP"

- [ ] BOM Multinível e Roteiros Operacionais.
- [ ] MRP rodando e gerando sugestões de compra/produção.
- [ ] Estoque com endereçamento e rastreabilidade de lotes.
- [ ] Folha de pagamento calculando verbas básicas.
- [ ] Data Lake populado com histórico de vendas e estoque.
- [ ] Dashboards de BI respondendo em < 2 segundos.
- [ ] Sistema capaz de suportar uma empresa de faturamento R$ 50M+/ano.

---

**Próximo Passo Imediato:**
Finalizar os componentes **Core**, **Fiscal** e **RH** que você está criando agora. Em seguida, iniciar a **Sprint 1 (Produção)** conforme detalhado acima.
