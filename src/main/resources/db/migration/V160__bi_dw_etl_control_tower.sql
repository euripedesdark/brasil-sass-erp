-- BI/DW: camada analitica integrada, historico de dimensoes, fatos, metricas,
-- snapshots, execucoes ETL e control tower. Sem alterar as tabelas transacionais.
CREATE SCHEMA IF NOT EXISTS brasil_saas;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_dim_tempo (
 data date PRIMARY KEY, ano integer NOT NULL, trimestre integer NOT NULL, mes integer NOT NULL,
 mes_nome varchar(20) NOT NULL, semana integer NOT NULL, dia integer NOT NULL,
 dia_semana integer NOT NULL, fim_mes boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_dim_empresa (
 sk bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL,
 validade_inicio timestamp NOT NULL DEFAULT now(), validade_fim timestamp,
 atual boolean NOT NULL DEFAULT true, hash_registro varchar(128), atributos jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_dim_cliente (
 sk bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, cliente_id bigint NOT NULL,
 validade_inicio timestamp NOT NULL DEFAULT now(), validade_fim timestamp,
 atual boolean NOT NULL DEFAULT true, hash_registro varchar(128), atributos jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_dim_produto (
 sk bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, produto_id bigint NOT NULL,
 validade_inicio timestamp NOT NULL DEFAULT now(), validade_fim timestamp,
 atual boolean NOT NULL DEFAULT true, hash_registro varchar(128), atributos jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_dim_funcionario (
 sk bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, funcionario_id bigint NOT NULL,
 validade_inicio timestamp NOT NULL DEFAULT now(), validade_fim timestamp,
 atual boolean NOT NULL DEFAULT true, hash_registro varchar(128), atributos jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_fato_vendas (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, data date NOT NULL,
 pedido_id bigint, cliente_id bigint, produto_id bigint, vendedor_id bigint,
 quantidade numeric(18,4) DEFAULT 0, valor_bruto numeric(18,2) DEFAULT 0,
 desconto numeric(18,2) DEFAULT 0, valor_liquido numeric(18,2) DEFAULT 0,
 custo numeric(18,2) DEFAULT 0, margem numeric(18,2) DEFAULT 0, origem varchar(40), origem_id bigint,
 atributos jsonb DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_fato_financeiro (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, data date NOT NULL,
 titulo_id bigint, tipo varchar(20), conta_id bigint, centro_custo_id bigint,
 valor numeric(18,2) DEFAULT 0, pago numeric(18,2) DEFAULT 0, saldo numeric(18,2) DEFAULT 0,
 vencimento date, liquidacao date, origem varchar(40), origem_id bigint,
 atributos jsonb DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_fato_estoque (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, data date NOT NULL,
 produto_id bigint, deposito_id bigint, tipo_movimento varchar(40),
 quantidade numeric(18,4) DEFAULT 0, custo numeric(18,2) DEFAULT 0, valor numeric(18,2) DEFAULT 0,
 lote varchar(100), origem varchar(40), origem_id bigint, atributos jsonb DEFAULT '{}'::jsonb,
 created_at timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_fato_producao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, data date NOT NULL,
 ordem_id bigint, produto_id bigint, centro_trabalho_id bigint,
 quantidade_planejada numeric(18,4) DEFAULT 0, quantidade_produzida numeric(18,4) DEFAULT 0,
 quantidade_refugo numeric(18,4) DEFAULT 0, horas numeric(18,4) DEFAULT 0,
 custo numeric(18,2) DEFAULT 0, eficiencia numeric(10,4) DEFAULT 0,
 atributos jsonb DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_fato_rh (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, data date NOT NULL,
 funcionario_id bigint, folha_id bigint, bruto numeric(18,2) DEFAULT 0,
 descontos numeric(18,2) DEFAULT 0, liquido numeric(18,2) DEFAULT 0,
 encargos numeric(18,2) DEFAULT 0, horas numeric(18,2) DEFAULT 0,
 faltas numeric(18,2) DEFAULT 0, afastamentos integer DEFAULT 0,
 atributos jsonb DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_metrica (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint,
 codigo varchar(80) NOT NULL, nome varchar(200) NOT NULL, categoria varchar(80),
 unidade varchar(30), formula_sql text, meta numeric(18,4), alerta_min numeric(18,4),
 alerta_max numeric(18,4), ativo boolean NOT NULL DEFAULT true, configuracao jsonb DEFAULT '{}'::jsonb,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_snapshot (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, data date NOT NULL,
 tipo varchar(50) NOT NULL, chave varchar(150) NOT NULL, valor numeric(20,6) DEFAULT 0,
 dimensoes jsonb DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now(),
 UNIQUE(empresa_id,data,tipo,chave)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_etl_execucao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint, processo varchar(120) NOT NULL,
 inicio timestamp NOT NULL DEFAULT now(), fim timestamp, status varchar(20) NOT NULL DEFAULT 'EXECUTANDO',
 registros integer DEFAULT 0, erro text, parametros jsonb DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_bi_consulta (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL,
 nome varchar(200) NOT NULL, codigo varchar(80) NOT NULL, descricao varchar(500),
 sql_select text NOT NULL, parametros jsonb DEFAULT '{}'::jsonb,
 publico boolean NOT NULL DEFAULT false, ativo boolean NOT NULL DEFAULT true,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);

CREATE INDEX IF NOT EXISTS ix_bi_vendas_data ON brasil_saas.bc_bi_fato_vendas(empresa_id,data);
CREATE INDEX IF NOT EXISTS ix_bi_fin_data ON brasil_saas.bc_bi_fato_financeiro(empresa_id,data);
CREATE INDEX IF NOT EXISTS ix_bi_est_data ON brasil_saas.bc_bi_fato_estoque(empresa_id,data);
CREATE INDEX IF NOT EXISTS ix_bi_rh_data ON brasil_saas.bc_bi_fato_rh(empresa_id,data);
CREATE INDEX IF NOT EXISTS ix_bi_etl_empresa ON brasil_saas.bc_bi_etl_execucao(empresa_id,inicio);
