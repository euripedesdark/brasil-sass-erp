-- Helpdesk / chamados
CREATE TABLE IF NOT EXISTS brasil_saas.bc_hdp_chamado (
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id      BIGINT NOT NULL,
    numero          VARCHAR(30) NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    descricao       TEXT,
    prioridade      VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    status          VARCHAR(30) NOT NULL DEFAULT 'ABERTO',
    categoria       VARCHAR(60),
    solicitante     VARCHAR(150),
    responsavel     VARCHAR(150),
    cliente_id      BIGINT,
    aberto_em       TIMESTAMP NOT NULL DEFAULT NOW(),
    fechado_em      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_hdp_chamado_emp ON brasil_saas.bc_hdp_chamado(empresa_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_hdp_chamado_status ON brasil_saas.bc_hdp_chamado(empresa_id, status) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_hdp_comentario (
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id      BIGINT NOT NULL,
    chamado_id      BIGINT NOT NULL REFERENCES brasil_saas.bc_hdp_chamado(id),
    autor           VARCHAR(150),
    texto           TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_hdp_com_chamado ON brasil_saas.bc_hdp_comentario(chamado_id) WHERE deleted_at IS NULL;

-- Agenda operacional (CRM / interno)
CREATE TABLE IF NOT EXISTS brasil_saas.bc_agd_evento (
    id              BIGSERIAL PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid(),
    empresa_id      BIGINT NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    descricao       TEXT,
    tipo            VARCHAR(40) NOT NULL DEFAULT 'REUNIAO',
    inicio          TIMESTAMP NOT NULL,
    fim             TIMESTAMP,
    local_evento    VARCHAR(200),
    lead_id         BIGINT,
    cliente_id      BIGINT,
    responsavel     VARCHAR(150),
    status          VARCHAR(30) NOT NULL DEFAULT 'AGENDADO',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);
CREATE INDEX IF NOT EXISTS idx_agd_emp_inicio ON brasil_saas.bc_agd_evento(empresa_id, inicio) WHERE deleted_at IS NULL;
