-- Contabilidade Enterprise: automacao, rateio, balancete, DRE/balanco e fechamento.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_regra_lancamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, codigo varchar(60) NOT NULL,
 nome varchar(200) NOT NULL, origem varchar(50) NOT NULL, conta_debito_id bigint, conta_credito_id bigint,
 centro_custo_id bigint, historico varchar(500), ativo boolean NOT NULL DEFAULT true,
 configuracao jsonb NOT NULL DEFAULT '{}'::jsonb, created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_rateio (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, lancamento_id bigint,
 centro_custo_id bigint, plano_contas_id bigint, percentual numeric(9,4) NOT NULL,
 valor numeric(18,2) NOT NULL DEFAULT 0, competencia date NOT NULL, status varchar(20) DEFAULT 'PENDENTE',
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_fechamento_check (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, periodo_id bigint NOT NULL,
 etapa varchar(50) NOT NULL, codigo varchar(80) NOT NULL, descricao varchar(300) NOT NULL,
 status varchar(20) NOT NULL DEFAULT 'PENDENTE', quantidade numeric(18,2) DEFAULT 0,
 executado_em timestamp, executado_por bigint, observacao varchar(1000),
 UNIQUE(empresa_id,periodo_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cont_relatorio_snapshot (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, empresa_id bigint NOT NULL, periodo_id bigint NOT NULL,
 tipo varchar(30) NOT NULL, conta_id bigint, codigo_conta varchar(50), descricao varchar(255),
 debito numeric(18,2) NOT NULL DEFAULT 0, credito numeric(18,2) NOT NULL DEFAULT 0,
 saldo numeric(18,2) NOT NULL DEFAULT 0, nivel integer DEFAULT 0, dados jsonb DEFAULT '{}'::jsonb,
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_cont_rateio_lanc ON brasil_saas.bc_cont_rateio(empresa_id,lancamento_id);
CREATE INDEX IF NOT EXISTS ix_cont_snapshot ON brasil_saas.bc_cont_relatorio_snapshot(empresa_id,periodo_id,tipo);
