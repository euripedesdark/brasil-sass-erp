-- GRC enterprise: riscos, controles, testes e evidencias, tenant-scoped.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_risco (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(500) NOT NULL,
 categoria varchar(80),
 probabilidade numeric(8,2) NOT NULL DEFAULT 0,
 impacto numeric(8,2) NOT NULL DEFAULT 0,
 nivel numeric(12,2) NOT NULL DEFAULT 0,
 status varchar(30) NOT NULL DEFAULT 'ABERTO',
 responsavel varchar(160),
 prazo date,
 created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(500) NOT NULL,
 tipo varchar(40) NOT NULL DEFAULT 'PREVENTIVO',
 frequencia varchar(40),
 responsavel varchar(160),
 status varchar(30) NOT NULL DEFAULT 'ATIVO',
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_risco_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 controle_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_controle(id),
 UNIQUE(empresa_id,risco_id,controle_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_teste_controle (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 controle_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_controle(id),
 periodo char(7) NOT NULL,
 resultado varchar(30) NOT NULL DEFAULT 'PENDENTE',
 observacao varchar(1000),
 testado_por varchar(160),
 testado_em timestamp,
 UNIQUE(empresa_id,controle_id,periodo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_evidencia (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 entidade_tipo varchar(60) NOT NULL,
 entidade_id bigint NOT NULL,
 nome varchar(200) NOT NULL,
 localizacao varchar(1000),
 validade date,
 hash_documento varchar(128),
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_grc_risco_tenant ON brasil_saas.bc_grc_risco(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_grc_controle_tenant ON brasil_saas.bc_grc_controle(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_grc_teste_tenant ON brasil_saas.bc_grc_teste_controle(empresa_id,periodo);
