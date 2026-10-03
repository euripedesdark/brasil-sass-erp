-- CRM: leads com funil, atividades e agenda. Forecast e pipeline por consulta.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_crm_lead (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    nome varchar(200) NOT NULL,
    empresa_nome varchar(200),
    email varchar(150),
    telefone varchar(20),
    origem varchar(60),
    etapa varchar(30) NOT NULL DEFAULT 'PROSPECCAO',
    status varchar(20) NOT NULL DEFAULT 'ABERTO',
    valor_estimado numeric(15,2) NOT NULL DEFAULT 0,
    probabilidade integer NOT NULL DEFAULT 10,
    responsavel varchar(200),
    data_prev_fechamento date,
    observacao text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_crm_tarefa (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    lead_id bigint REFERENCES brasil_saas.bc_crm_lead(id),
    tipo varchar(20) NOT NULL DEFAULT 'TAREFA',
    assunto varchar(200) NOT NULL,
    descricao text,
    data_agendada timestamp,
    concluida boolean NOT NULL DEFAULT false,
    concluida_em timestamp,
    responsavel varchar(200),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_crm_lead_etapa ON brasil_saas.bc_crm_lead (empresa_id, etapa) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_crm_tarefa_lead ON brasil_saas.bc_crm_tarefa (lead_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_crm_tarefa_agenda ON brasil_saas.bc_crm_tarefa (empresa_id, data_agendada) WHERE deleted_at IS NULL;
