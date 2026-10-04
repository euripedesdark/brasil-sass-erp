-- Fila de eventos eSocial: registra intento + payload; transmite quando houver endpoint.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_esocial_evento (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    tipo varchar(20) NOT NULL,
    funcionario_id bigint,
    payload text,
    status varchar(20) NOT NULL DEFAULT 'PENDENTE',
    recibo varchar(200),
    protocolo varchar(200),
    erro text,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_esocial_status ON brasil_saas.bc_esocial_evento (empresa_id, status) WHERE deleted_at IS NULL;
