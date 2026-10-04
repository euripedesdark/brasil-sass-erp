-- Ponto: batidas por funcionario/dia com horas calculadas.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_rh_ponto (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data date NOT NULL,
    e1 time,
    s1 time,
    e2 time,
    s2 time,
    horas_trabalhadas numeric(5,2) NOT NULL DEFAULT 0,
    falta boolean NOT NULL DEFAULT false,
    observacao varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp,
    CONSTRAINT uk_rh_ponto UNIQUE (empresa_id, funcionario_id, data)
);
CREATE INDEX IF NOT EXISTS ix_rh_ponto_mes ON brasil_saas.bc_rh_ponto (empresa_id, data) WHERE deleted_at IS NULL;
