-- WMS: ondas de separacao, volumes/packing. Reaproveita enderecos, reservas
-- e expedicoes do estoque (bc_est_*); nada aqui duplica cadastro.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wms_onda (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    deposito_id bigint NOT NULL,
    codigo varchar(60) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'ABERTA',
    responsavel varchar(200),
    liberada_em timestamp,
    concluida_em timestamp,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_wms_onda UNIQUE (empresa_id, deposito_id, codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wms_onda_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    onda_id bigint NOT NULL REFERENCES brasil_saas.bc_wms_onda(id),
    origem_tipo varchar(20) NOT NULL DEFAULT 'RESERVA',
    origem_id bigint,
    produto_id bigint NOT NULL,
    qtd_solicitada numeric(15,3) NOT NULL,
    qtd_separada numeric(15,3) NOT NULL DEFAULT 0,
    endereco_id bigint,
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wms_volume (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    expedicao_id bigint NOT NULL,
    codigo varchar(60) NOT NULL,
    peso numeric(15,3),
    status varchar(20) NOT NULL DEFAULT 'ABERTO',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_wms_volume UNIQUE (empresa_id, expedicao_id, codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wms_volume_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    volume_id bigint NOT NULL REFERENCES brasil_saas.bc_wms_volume(id),
    produto_id bigint NOT NULL,
    quantidade numeric(15,3) NOT NULL,
    onda_item_id bigint,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_wms_onda_status ON brasil_saas.bc_wms_onda (empresa_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_wms_onda_item ON brasil_saas.bc_wms_onda_item (onda_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_wms_vol_exp ON brasil_saas.bc_wms_volume (expedicao_id) WHERE deleted_at IS NULL;
