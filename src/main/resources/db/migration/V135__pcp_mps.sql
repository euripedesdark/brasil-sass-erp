-- MPS: plano mestre a partir da demanda (pedidos) menos estoque.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_pcp_mps (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    periodo char(7) NOT NULL,
    produto_id bigint NOT NULL,
    qtd_demandada numeric(15,3) NOT NULL DEFAULT 0,
    qtd_estoque numeric(15,3) NOT NULL DEFAULT 0,
    qtd_planejada numeric(15,3) NOT NULL DEFAULT 0,
    origem varchar(20) NOT NULL DEFAULT 'PEDIDOS',
    status varchar(20) NOT NULL DEFAULT 'RASCUNHO',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_pcp_mps UNIQUE (empresa_id, periodo, produto_id)
);
CREATE INDEX IF NOT EXISTS ix_pcp_mps_periodo ON brasil_saas.bc_pcp_mps (empresa_id, periodo) WHERE deleted_at IS NULL;
