-- Complemento empresarial de GRC e TMS.
-- Mantem isolamento por empresa e adiciona execução, tratamento, auditoria operacional e fechamento.

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_plano_acao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 descricao varchar(1000) NOT NULL,
 responsavel varchar(160),
 prazo date,
 status varchar(30) NOT NULL DEFAULT 'ABERTO',
 percentual_conclusao numeric(5,2) NOT NULL DEFAULT 0,
 evidencia_id bigint,
 created_at timestamp NOT NULL DEFAULT now(),
 updated_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_grc_plano_acao_tenant ON brasil_saas.bc_grc_plano_acao(empresa_id,status,prazo);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_avaliacao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 risco_id bigint NOT NULL REFERENCES brasil_saas.bc_grc_risco(id),
 periodo char(7) NOT NULL,
 probabilidade numeric(8,2) NOT NULL,
 impacto numeric(8,2) NOT NULL,
 nivel numeric(12,2) NOT NULL,
 tendencia varchar(30),
 avaliador varchar(160),
 observacao varchar(1000),
 avaliado_em timestamp NOT NULL DEFAULT now(),
 UNIQUE(empresa_id,risco_id,periodo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_grc_log (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 entidade_tipo varchar(60) NOT NULL,
 entidade_id bigint,
 acao varchar(60) NOT NULL,
 usuario varchar(160),
 detalhes varchar(2000),
 criado_em timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_grc_log_tenant ON brasil_saas.bc_grc_log(empresa_id,criado_em);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_rota (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 codigo varchar(60) NOT NULL,
 descricao varchar(300),
 origem varchar(200) NOT NULL,
 destino varchar(200) NOT NULL,
 distancia_km numeric(12,2),
 tempo_estimado_min integer,
 pedagio_estimado numeric(15,2) DEFAULT 0,
 ativo boolean NOT NULL DEFAULT true,
 UNIQUE(empresa_id,codigo)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_parada (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 sequencia integer NOT NULL,
 tipo varchar(30) NOT NULL,
 localizacao varchar(300) NOT NULL,
 prevista_em timestamp,
 realizada_em timestamp,
 status varchar(30) NOT NULL DEFAULT 'PENDENTE',
 UNIQUE(empresa_id,ordem_id,sequencia)
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_documento_entrega (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 tipo varchar(40) NOT NULL DEFAULT 'COMPROVANTE_ENTREGA',
 numero varchar(120),
 recebedor varchar(160),
 recebido_em timestamp,
 assinatura_localizacao varchar(500),
 observacao varchar(1000),
 documento_localizacao varchar(1000),
 status varchar(30) NOT NULL DEFAULT 'PENDENTE'
);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_tms_fechamento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL,
 ordem_id bigint NOT NULL REFERENCES brasil_saas.bc_tms_ordem(id),
 frete_contratado numeric(15,2) NOT NULL DEFAULT 0,
 adicionais numeric(15,2) NOT NULL DEFAULT 0,
 descontos numeric(15,2) NOT NULL DEFAULT 0,
 frete_aprovado numeric(15,2) NOT NULL DEFAULT 0,
 documento varchar(120),
 status varchar(30) NOT NULL DEFAULT 'PENDENTE',
 aprovado_por varchar(160),
 aprovado_em timestamp,
 UNIQUE(empresa_id,ordem_id)
);

CREATE INDEX IF NOT EXISTS ix_tms_rota_tenant ON brasil_saas.bc_tms_rota(empresa_id,ativo);
CREATE INDEX IF NOT EXISTS ix_tms_parada_tenant ON brasil_saas.bc_tms_parada(empresa_id,ordem_id,sequencia);
CREATE INDEX IF NOT EXISTS ix_tms_entrega_tenant ON brasil_saas.bc_tms_documento_entrega(empresa_id,ordem_id);
