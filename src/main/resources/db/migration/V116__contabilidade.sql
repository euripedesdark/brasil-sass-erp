-- Contabilidade: diario (lancamento + partidas), razao/balancete/balanco por
-- consulta, e fechamento por periodo. Partidas referenciam o plano de contas
-- (bc_fin_plano_contas) por conta_id logico, sem FK rigida entre modulos.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ctb_lancamento (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    data date NOT NULL,
    periodo char(7) NOT NULL,
    historico varchar(500) NOT NULL,
    origem_tipo varchar(60),
    origem_id bigint,
    status varchar(20) NOT NULL DEFAULT 'RASCUNHO',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ctb_partida (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    lancamento_id bigint NOT NULL REFERENCES brasil_saas.bc_ctb_lancamento(id),
    conta_id bigint NOT NULL,
    centro_custo_id bigint,
    debito numeric(15,2) NOT NULL DEFAULT 0,
    credito numeric(15,2) NOT NULL DEFAULT 0,
    historico varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT ck_partida_um_lado CHECK ((debito > 0 AND credito = 0) OR (credito > 0 AND debito = 0))
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ctb_fechamento (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    periodo char(7) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'FECHADO',
    fechado_por bigint,
    fechado_em timestamp NOT NULL DEFAULT now(),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_ctb_fechamento UNIQUE (empresa_id, periodo)
);
CREATE INDEX IF NOT EXISTS ix_ctb_lanc_periodo ON brasil_saas.bc_ctb_lancamento (empresa_id, periodo) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_ctb_part_conta ON brasil_saas.bc_ctb_partida (empresa_id, conta_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_ctb_part_lanc ON brasil_saas.bc_ctb_partida (lancamento_id) WHERE deleted_at IS NULL;
