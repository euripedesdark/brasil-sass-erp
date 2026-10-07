-- TMS enterprise: ordens de transporte, tracking, eventos e fechamento de frete.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_ordem (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 numero varchar(60) NOT NULL,
 origem varchar(200),
 destino varchar(200),
 transportadora_id bigint,
 modalidade varchar(40) NOT NULL DEFAULT 'RODOVIARIO',
 status varchar(30) NOT NULL DEFAULT 'PLANEJADA',
 data_prevista_saida timestamp,
 data_prevista_entrega timestamp,
 peso numeric(15,3) DEFAULT 0,
 volume numeric(15,3) DEFAULT 0,
 frete_previsto numeric(15,2) DEFAULT 0,
 frete_real numeric(15,2) DEFAULT 0,
 UNIQUE(empresa_id,numero)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_evento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 tipo varchar(50) NOT NULL,
 data_evento timestamp NOT NULL DEFAULT now(),
 localizacao varchar(200),
 descricao varchar(500)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_tracking (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 codigo varchar(100) NOT NULL,
 transportadora varchar(160),
 ultimo_status varchar(80),
 ultima_atualizacao timestamp,
 UNIQUE(empresa_id,ordem_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_frete (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 componente varchar(80) NOT NULL,
 valor numeric(15,2) NOT NULL DEFAULT 0,
 documento varchar(120),
 status varchar(30) NOT NULL DEFAULT 'PENDENTE'
);
CREATE INDEX IF NOT EXISTS ix_tms_ordem_tenant ON brasil_saas.bc_tms_ordem(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_tms_evento_ordem ON brasil_saas.bc_tms_evento(empresa_id,ordem_id,data_evento);
