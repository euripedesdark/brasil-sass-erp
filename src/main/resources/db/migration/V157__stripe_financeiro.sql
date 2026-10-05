CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_stripe_customer (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    empresa_id BIGINT NOT NULL,
    pessoa_id BIGINT NOT NULL,
    stripe_customer_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_fin_stripe_customer_empresa_pessoa UNIQUE (empresa_id, pessoa_id),
    CONSTRAINT uk_fin_stripe_customer_stripe UNIQUE (stripe_customer_id)
);
CREATE INDEX IF NOT EXISTS idx_fin_stripe_customer_empresa ON brasil_saas.bc_fin_stripe_customer (empresa_id);

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_stripe_payment (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    empresa_id BIGINT NOT NULL,
    titulo_id BIGINT NOT NULL,
    stripe_customer_id VARCHAR(64),
    checkout_session_id VARCHAR(128),
    payment_intent_id VARCHAR(128),
    invoice_id VARCHAR(128),
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    checkout_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_fin_stripe_payment_session
    ON brasil_saas.bc_fin_stripe_payment (checkout_session_id)
    WHERE checkout_session_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fin_stripe_payment_titulo
    ON brasil_saas.bc_fin_stripe_payment (empresa_id, titulo_id);
CREATE INDEX IF NOT EXISTS idx_fin_stripe_payment_invoice
    ON brasil_saas.bc_fin_stripe_payment (invoice_id)
    WHERE invoice_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_fin_stripe_webhook_event (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    empresa_id BIGINT NOT NULL,
    stripe_event_id VARCHAR(128) NOT NULL UNIQUE,
    event_type VARCHAR(128) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fin_stripe_webhook_empresa
    ON brasil_saas.bc_fin_stripe_webhook_event (empresa_id, processed_at);
