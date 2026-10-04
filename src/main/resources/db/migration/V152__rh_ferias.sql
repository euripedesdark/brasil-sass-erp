-- Ferias: programacao, gozo e valor com terco.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_ferias (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data_inicio date NOT NULL,
    dias integer NOT NULL DEFAULT 30,
    data_fim date NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'PROGRAMADA',
    valor_ferias numeric(15,2) NOT NULL DEFAULT 0,
    valor_terco numeric(15,2) NOT NULL DEFAULT 0,
    folha_id bigint,
    observacao varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_rh_ferias_func ON brasil_saas.bc_rh_ferias (empresa_id, funcionario_id) WHERE deleted_at IS NULL;
