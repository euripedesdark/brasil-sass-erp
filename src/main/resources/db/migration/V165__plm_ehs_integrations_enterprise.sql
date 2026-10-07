-- Enterprise completion: PLM/ECM, EHS and Integration Hub.

CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_documento (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, revisao_id bigint REFERENCES brasil_saas.bc_plm_produto_revisao(id),
 mudanca_id bigint REFERENCES brasil_saas.bc_plm_mudanca(id),
 tipo varchar(40) NOT NULL, codigo varchar(100) NOT NULL, versao varchar(30) NOT NULL DEFAULT '1',
 nome varchar(300) NOT NULL, localizacao varchar(1000), hash_documento varchar(128),
 status varchar(30) NOT NULL DEFAULT 'RASCUNHO', obrigatorio boolean NOT NULL DEFAULT false,
 aprovado_por bigint, aprovado_em timestamp, vigencia_inicio date, vigencia_fim date,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp, deleted_at timestamp,
 UNIQUE(empresa_id,codigo,versao)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_efeito_mudanca (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, mudanca_id bigint NOT NULL REFERENCES brasil_saas.bc_plm_mudanca(id),
 entidade_tipo varchar(50) NOT NULL, entidade_id bigint NOT NULL, acao varchar(30) NOT NULL,
 revisao_anterior varchar(30), revisao_nova varchar(30), efetiva_em date,
 status varchar(30) NOT NULL DEFAULT 'PENDENTE', observacao varchar(1000)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_plm_aprovacao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, mudanca_id bigint NOT NULL REFERENCES brasil_saas.bc_plm_mudanca(id),
 etapa integer NOT NULL, aprovador_id bigint, decisao varchar(30) NOT NULL DEFAULT 'PENDENTE',
 observacao varchar(1000), decidido_em timestamp,
 UNIQUE(empresa_id,mudanca_id,etapa)
);
CREATE INDEX IF NOT EXISTS ix_plm_doc_tenant ON brasil_saas.bc_plm_documento(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_plm_efeito_tenant ON brasil_saas.bc_plm_efeito_mudanca(empresa_id,mudanca_id);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_risco (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, codigo varchar(80) NOT NULL, perigo varchar(300) NOT NULL,
 atividade varchar(300), localizacao varchar(300), probabilidade numeric(8,2) NOT NULL DEFAULT 1,
 impacto numeric(8,2) NOT NULL DEFAULT 1, nivel numeric(12,2) NOT NULL DEFAULT 1,
 controle_existente text, responsavel varchar(160), status varchar(30) NOT NULL DEFAULT 'ATIVO',
 revisado_em timestamp, UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_inspecao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(80) NOT NULL,
 localizacao varchar(300), responsavel varchar(160), data_inspecao timestamp NOT NULL DEFAULT now(),
 status varchar(30) NOT NULL DEFAULT 'ABERTA', resultado varchar(30), observacao text,
 UNIQUE(empresa_id,numero)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_acao (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, ocorrencia_id bigint REFERENCES brasil_saas.bc_ehs_ocorrencia(id),
 inspecao_id bigint REFERENCES brasil_saas.bc_ehs_inspecao(id), descricao varchar(1000) NOT NULL,
 responsavel varchar(160), prazo date, status varchar(30) NOT NULL DEFAULT 'ABERTA',
 concluida_em timestamp, evidencia varchar(1000)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_ehs_permissao_trabalho (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, numero varchar(80) NOT NULL, tipo varchar(80) NOT NULL,
 localizacao varchar(300) NOT NULL, solicitante varchar(160), responsavel varchar(160),
 inicio timestamp, fim timestamp, riscos text, controles text, status varchar(30) NOT NULL DEFAULT 'SOLICITADA',
 aprovada_por varchar(160), aprovada_em timestamp, UNIQUE(empresa_id,numero)
);
CREATE INDEX IF NOT EXISTS ix_ehs_risco_tenant ON brasil_saas.bc_ehs_risco(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_ehs_inspecao_tenant ON brasil_saas.bc_ehs_inspecao(empresa_id,status);
CREATE INDEX IF NOT EXISTS ix_ehs_acao_tenant ON brasil_saas.bc_ehs_acao(empresa_id,status);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_int_endpoint (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, codigo varchar(100) NOT NULL, nome varchar(200) NOT NULL,
 tipo varchar(30) NOT NULL DEFAULT 'WEBHOOK', url varchar(1000) NOT NULL,
 segredo_hash varchar(128), eventos text, ativo boolean NOT NULL DEFAULT true,
 timeout_ms integer NOT NULL DEFAULT 10000, tentativas integer NOT NULL DEFAULT 3,
 created_at timestamp NOT NULL DEFAULT now(), updated_at timestamp,
 UNIQUE(empresa_id,codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_int_webhook_event (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint, endpoint_id bigint REFERENCES brasil_saas.bc_int_endpoint(id),
 event_id varchar(160) NOT NULL, event_type varchar(160) NOT NULL,
 payload text NOT NULL, signature varchar(500), status varchar(30) NOT NULL DEFAULT 'RECEBIDO',
 attempts integer NOT NULL DEFAULT 0, error_message varchar(2000),
 received_at timestamp NOT NULL DEFAULT now(), processed_at timestamp,
 UNIQUE(event_id)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_int_delivery (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 empresa_id bigint NOT NULL, endpoint_id bigint REFERENCES brasil_saas.bc_int_endpoint(id),
 event_type varchar(160) NOT NULL, aggregate_type varchar(100), aggregate_id bigint,
 payload text NOT NULL, status varchar(30) NOT NULL DEFAULT 'PENDENTE',
 attempts integer NOT NULL DEFAULT 0, next_attempt_at timestamp,
 last_error varchar(2000), delivered_at timestamp, correlation_id uuid,
 created_at timestamp NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_int_event_tenant ON brasil_saas.bc_int_webhook_event(empresa_id,received_at);
CREATE INDEX IF NOT EXISTS ix_int_delivery_tenant ON brasil_saas.bc_int_delivery(empresa_id,status,next_attempt_at);

SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao(codigo,recurso,acao,descricao) VALUES
 ('plm:admin','plm','admin','Administrar ciclo de vida de engenharia'),
 ('ehs:admin','ehs','admin','Administrar riscos e permissões EHS'),
 ('integracao:leitura','integracao','leitura','Consultar integrações e entregas'),
 ('integracao:escrita','integracao','escrita','Administrar endpoints e integrações')
ON CONFLICT(codigo) DO NOTHING;
