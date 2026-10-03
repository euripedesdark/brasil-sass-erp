-- Motor de workflow transversal: definicoes, etapas, instancias e tarefas.
-- Aprovacao multinivel + SLA por etapa + escalonamento. Transversal: qualquer
-- entidade (titulo, pedido, OS, solicitacao) abre instancia apontando
-- entidade_tipo + entidade_id, sem FK rigida para nao acoplar modulos.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wkf_definition (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    codigo varchar(60) NOT NULL,
    nome varchar(200) NOT NULL,
    descricao text,
    entidade_alvo varchar(60) NOT NULL,
    ativo boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_wkf_definition UNIQUE (empresa_id, codigo)
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wkf_stage (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    definition_id bigint NOT NULL REFERENCES brasil_saas.bc_wkf_definition(id),
    ordem integer NOT NULL,
    nome varchar(200) NOT NULL,
    tipo varchar(20) NOT NULL DEFAULT 'APROVACAO',
    sla_horas integer NOT NULL DEFAULT 48,
    aprovadores text,
    exige_todos boolean NOT NULL DEFAULT false,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wkf_instance (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    definition_id bigint NOT NULL REFERENCES brasil_saas.bc_wkf_definition(id),
    entidade_tipo varchar(60) NOT NULL,
    entidade_id bigint NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
    etapa_atual integer NOT NULL DEFAULT 1,
    solicitado_por bigint,
    observacao text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    concluded_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_wkf_task (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    instance_id bigint NOT NULL REFERENCES brasil_saas.bc_wkf_instance(id),
    stage_id bigint NOT NULL REFERENCES brasil_saas.bc_wkf_stage(id),
    responsavel varchar(200),
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    decided_at timestamp,
    decidido_por bigint,
    comentario text,
    sla_limite timestamp,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_wkf_instance_entidade ON brasil_saas.bc_wkf_instance (empresa_id, entidade_tipo, entidade_id);
CREATE INDEX IF NOT EXISTS ix_wkf_task_pendente ON brasil_saas.bc_wkf_task (empresa_id, status) WHERE deleted_at IS NULL;
