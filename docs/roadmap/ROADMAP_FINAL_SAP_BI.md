# 🚀 Roadmap Final: Nivelamento SAP & BI DataLake

**Data da Atualização:** 21/09/2026  
**Status Atual:** 90% Core ERP Implementado  
**Próximo Marco:** Módulo Produção Industrial + BI DataLake

---

## 1. 📊 Gap Analysis: BRASIL-SAAS vs SAP Business One

Esta seção detalha o que falta para atingir a paridade funcional com o SAP Business One, excluindo obrigações fiscais brasileiras (SPED/eSocial) que dependem de certificado digital.

### 1.1. Matriz de Funcionalidades

| Área | Funcionalidade SAP | Status BRASIL-SAAS | Gap | Prioridade |
|------|-------------------|--------------------|-----|------------|
| **Produção** | Lista Material (BOM) Multinível | ❌ Não Implementado | Estrutura pai/filho recursiva | 🔴 Crítica |
| | Roteiros de Produção | ❌ Não Implementado | Operações, centros de trabalho | 🔴 Crítica |
| | MRP II (Planejamento) | ❌ Não Implementado | Explosão de demanda | 🟠 Alta |
| | Apontamento Chão de Fábrica | ⚠️ Parcial (Ordens) | Baixa real por operação | 🟠 Alta |
| | Custeio Industrial (Padrão/Real) | ❌ Não Implementado | Rateios, subprodutos | 🟠 Alta |
| **Estoque (WMS)** | Endereçamento Lógico | ❌ Não Implementado | Ruas, prateleiras, bins | 🟠 Alta |
| | Gestão de Lotes e Validades | ⚠️ Básico | Rastreabilidade completa | 🟠 Alta |
| | Inventário Rotativo | ❌ Não Implementado | Contagem cíclica | 🟡 Média |
| **Qualidade** | Plano de Inspeção (Entrada/Processo/Saída) | ❌ Não Implementado | Amostras, critérios | 🟡 Média |
| | Gestão de Não Conformidade | ❌ Não Implementado | Ações corretivas | 🟡 Média |
| **Financeiro** | Centros de Custo/Lucro | ⚠️ Estrutura Básica | Rateios automáticos complexos | 🟠 Alta |
| | Fluxo de Caixa Projetado | ❌ Não Implementado | Cenários hipotéticos | 🟠 Alta |
| | Ativo Imobilizado (Depreciação) | ❌ Não Implementado | Cálculo mensal fiscal/contábil | 🟡 Média |
| **RH** | Folha de Pagamento Completa | ❌ Não Implementado | Cálculos CLT, pró-labore | 🔴 Crítica (Legale) |
| | Gestão de Ponto Eletrônico | ❌ Não Implementado | Integração relógios | 🟠 Alta |
| **BI/Analytics** | Data Warehouse / Data Lake | ❌ Não Implementado | Schema estrela, ETL | 🔴 Crítica |
| | Cubos OLAP | ❌ Não Implementado | Pré-cálculo de métricas | 🟠 Alta |
| | Dashboards Executivos | ⚠️ Básico | KPIs em tempo real | 🟠 Alta |
| **Gestão Documental** | Repositório Central (DMS) | ⚠️ Imagens (Mongo) | Qualquer tipo de arquivo, versionamento | 🟠 Alta |

---

## 2. 🏭 Módulo de Produção Nível SAP

Para competir com ERPs industriais, o módulo de produção precisa evoluir de "Ordens Simples" para "Engenharia de Manufatura".

### 2.1. Estrutura de Dados (Migrations Pendentes: V47-V52)

#### V47: BOM Multinível (Lista de Materiais)
```sql
CREATE TABLE prod_bom (
    id BIGSERIAL PRIMARY KEY,
    produto_pai_id BIGINT REFERENCES produtos(id),
    versao VARCHAR(20),
    valida_desde DATE,
    valida_ate DATE,
    status VARCHAR(20) -- ATIVO, INATIVO, EM_PROJETO
);

CREATE TABLE prod_bom_item (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT REFERENCES prod_bom(id),
    componente_id BIGINT REFERENCES produtos(id),
    quantidade DECIMAL(15,4),
    unidade_medida VARCHAR(10),
    percentual_perda DECIMAL(5,2),
    ordem INT
);
-- Permite recursividade: componente pode ser outro produto com próprio BOM
```

#### V48: Roteiros e Operações
```sql
CREATE TABLE prod_roteiro (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT REFERENCES produtos(id),
    versao VARCHAR(20),
    tempo_ciclo_total DECIMAL(10,2)
);

CREATE TABLE prod_operacao (
    id BIGSERIAL PRIMARY KEY,
    roteiro_id BIGINT REFERENCES prod_roteiro(id),
    sequencia INT,
    descricao VARCHAR(255),
    centro_trabalho_id BIGINT, -- Recurso/Máquina
    tempo_setup DECIMAL(10,2),
    tempo_maquina DECIMAL(10,2),
    tempo_mao_obra DECIMAL(10,2),
    custo_hora_maquina DECIMAL(15,2),
    custo_hora_homem DECIMAL(15,2)
);
```

#### V49: Ordens de Produção Evoluídas
- Vincular OP ao BOM e Roteiro específicos.
- Campos de status detalhado: `PLANEJADA`, `LIBERADA`, `EM_FABRICACAO`, `PARCIAL`, `CONCLUIDA`.
- Baixa automática de insumos baseada no BOM (Backflush).

#### V50: Apontamento de Chão de Fábrica
```sql
CREATE TABLE prod_apontamento (
    id BIGSERIAL PRIMARY KEY,
    op_id BIGINT REFERENCES prod_ordem_producao(id),
    operacao_id BIGINT REFERENCES prod_operacao(id),
    data_hora_inicio TIMESTAMP,
    data_hora_fim TIMESTAMP,
    quantidade_boa INT,
    quantidade_refugo INT,
    operador_id BIGINT,
    observacoes TEXT
);
```

### 2.2. Regras de Negócio a Implementar
1. **Explosão de Demanda (MRP):** Calcular necessidade líquida = Demanda - Estoque Atual - Em Compra + Reserva.
2. **Backflush:** Dar baixa nos componentes do BOM automaticamente quando a OP for concluída.
3. **Custeio:** Somar custos de material (BOM) + custos de operação (Roteiro) para gerar Custo Unitário Padrão.

---

## 3. 📦 WMS Light & Qualidade

### 3.1. Endereçamento de Estoque (Migration V53)
Adicionar tabela `estoque_endereco` e vincular saldos a endereços específicos.
- Estrutura: `Armazém > Rua > Prateleira > Nível > Posição (Bin)`.
- Regra: Um produto pode estar em múltiplos endereços; o sistema deve sugerir o melhor para picking (ex: FIFO).

### 3.2. Lotes e Validades (Migration V54)
- Reforçar rastreabilidade: Entrada de Nota → Gera Lote → Saída de OP/Venda consome Lote específico.
- Alertas de vencimento no Dashboard.

### 3.3. Qualidade (Migration V55)
- Tabela `qualidade_inspecao`: Vinculada a Entrada de Nota ou OP.
- Itens de inspeção: Dimensões, Cor, Funcionalidade.
- Resultado: Aprovado, Reprovado, Condicionado.

---

## 4. 🧠 Arquitetura BI DataLake (PostgreSQL Dedicado)

O BI não deve rodar queries pesadas diretamente nas tabelas transacionais (OLTP). Será criado um **Data Lake** dentro do próprio PostgreSQL, mas em schema isolado.

### 4.1. Estrutura do DataLake
- **Banco:** `brasil-saas_erp` (mesmo DB, instância separada lógica).
- **Schema Transacional:** `public` (Operações diárias).
- **Schema Analítico:** `bi_datalake` (Fatos e Dimensões).

### 4.2. Modelo Dimensional (Star Schema)
Migrations V56-V59 criarão as tabelas analíticas:

#### Dimensões (Tabelas de Consulta)
- `dim_tempo`: Dia, mês, ano, trimestre, feriado.
- `dim_produto`: Hierarquia (Categoria, Marca, Família).
- `dim_cliente`: Região, Vendedor, Segmento.
- `dim_fornecedor`: Categoria, País.
- `dim_empresa`: Centro de custo, filial.

#### Fatos (Tabelas de Métricas)
- `fato_vendas`: Valor vendido, quantidade, desconto, imposto.
- `fato_compras`: Valor comprado, prazo pagamento.
- `fato_estoque`: Saldo atual, giro, ruptura.
- `fato_producao`: Quantidade produzida, refugo, eficiência.
- `fato_financeiro`: Contas pagas, recebidas, inadimplência.

### 4.3. Mecanismo de ETL (Spring Batch)
- **Job Noturno:** Rodar às 02:00 AM.
- **Processo:**
  1. Ler alterações nas tabelas `public` desde a última execução.
  2. Transformar dados (ex: calcular margem, agrupar por hierarquia).
  3. Carregar (Upsert) nas tabelas `bi_datalake`.
- **Tecnologia:** Spring Batch com leitores JPA e escritores JDBC para performance.

### 4.4. Frontend BI
- Componente `DashboardExecutivo.jsx`: Gráficos de tendência (Vendas x Meta).
- Componente `ExploradorDados.jsx`: Tabela dinâmica estilo Excel (Pivot Table).
- Exportação: PDF, Excel, CSV.

---

## 5. 🗄️ Gestão Documental Genérica (MongoDB)

Atualmente o MongoDB guarda apenas imagens do sistema. Evoluir para um **DMS (Document Management System)** completo.

### 5.1. Estrutura NoSQL (Coleção `documentos`)
```json
{
  "_id": "ObjectId",
  "tipo": "CONTRATO_FORNECEDOR",
  "entidade_referencia": {
    "tipo": "FORNECEDOR",
    "id": 1050
  },
  "arquivo": {
    "nome_original": "contrato_2026.pdf",
    "mime_type": "application/pdf",
    "tamanho_bytes": 102400,
    "hash_sha256": "a1b2c3...",
    "bucket_path": "/docs/2026/09/1050_contrato.pdf"
  },
  "metadados": {
    "data_upload": "ISODate",
    "usuario_id": 12,
    "tags": ["juridico", "vigente"],
    "data_expiracao": "ISODate"
  }
}
```

### 5.2. Backend Implementation
- **Endpoint:** `POST /api/documentos/upload` (Multipart file).
- **Endpoint:** `GET /api/documentos/{id}/download` (Stream do GridFS).
- **Segurança:** Validar permissão de acesso baseada na entidade vinculada.
- **Armazenamento:** Usar **GridFS** do MongoDB para arquivos > 16MB.

### 5.3. Frontend
- Componente `AnexosGenericos.jsx`: Lista de arquivos vinculados a um cadastro.
- Upload por Drag-and-Drop.
- Visualizador inline para PDFs e Imagens.

---

## 6. 🖥️ Frontend: Telas Pendentes

### 6.1. Módulo Fiscal (7 telas)
1. `ConfiguracaoImpostos.jsx`
2. `EmissaoNFe.jsx`
3. `EntradaNotas.jsx`
4. `HistoricoFiscal.jsx`
5. `LivrosFiscais.jsx`
6. `CertificadoDigital.jsx`
7. `MonitoramentoSEFAZ.jsx`

### 6.2. Módulo RH (3 telas)
1. `FolhaPagamento.jsx`
2. `GestaoPonto.jsx`
3. `Beneficios.jsx`

### 6.3. Módulo Core (4 telas)
1. `AuditoriaSistema.jsx`
2. `ParametrosGlobais.jsx`
3. `IntegracoesAPI.jsx`
4. `BackupRestore.jsx`

### 6.4. Módulo Produção (5 telas)
1. `EngenhariaProduto.jsx` (BOM e Roteiros)
2. `PlanejamentoMRP.jsx`
3. `ChaoDeFabrica.jsx`
4. `CusteioIndustrial.jsx`
5. `OrdensProducao.jsx` (Melhoria)

### 6.5. Módulo BI (3 telas)
1. `DashboardVendas.jsx`
2. `DashboardFinanceiro.jsx`
3. `ExploradorCubos.jsx`

---

## 7. 📅 Cronograma de Sprints (8 Sprints de 2 Semanas)

| Sprint | Foco | Entregáveis Principais |
|--------|------|------------------------|
| **01** | Produção Base | Migrations BOM/Roteiro, Telas Engenharia de Produto |
| **02** | Produção Avançada | MRP Simplificado, Apontamento Chão de Fábrica |
| **03** | WMS & Qualidade | Endereçamento, Lotes/Validades, Inspeção Qualidade |
| **04** | Financeiro Gerencial | Centros de Custo, Fluxo de Caixa, Ativo Imobilizado |
| **05** | RH Básico | Cadastro Funcionário, Estrutura Salarial, Ponto Simples |
| **06** | BI DataLake | Schema `bi_datalake`, Jobs ETL, Dashboards |
| **07** | Gestão Documental | Upload Genérico Mongo, GridFS, Vinculação a Entidades |
| **08** | Polimento & Performance | Índices DB, Cache Redis, Testes de Carga, UI Final |

**Total Estimado:** 16 semanas (~4 meses).

---

## 8. ⚠️ Notas sobre SPED

- **SPED Fiscal/Contábil:** Implementação suspensa até aquisição do Certificado Digital.
- **eSocial:** Suspenso pendente de configuração de ambiente de produção.
- **Estratégia:** Focar em regras de negócio internas (Produção, BI, Gestão) que entregam valor imediato.

---

## 9. ✅ Checklist de Validação Atual

- [x] Core ERP (Vendas, Compras, Estoque, Financeiro Básico)
- [x] Autenticação JWT + Imagens Dinâmicas (Mongo)
- [x] Estrutura de Módulos Spring Boot
- [x] Frontend React com Layout Responsivo
- [ ] BOM Multinível e Roteiros
- [ ] DataLake BI com ETL
- [ ] Gestão Documental Genérica
- [ ] Telas Fiscais Completas
- [ ] Folha de Pagamento

**Próximo Passo Imediato:** Iniciar Sprint 01 (Produção Industrial).
