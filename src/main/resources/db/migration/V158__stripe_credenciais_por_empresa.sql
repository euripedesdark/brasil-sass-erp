ALTER TABLE brasil_saas.bc_core_empresa
    ADD COLUMN IF NOT EXISTS stripe_secret_key_encrypted TEXT,
    ADD COLUMN IF NOT EXISTS stripe_webhook_secret_encrypted TEXT,
    ADD COLUMN IF NOT EXISTS stripe_habilitada BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS stripe_account_id VARCHAR(64);

COMMENT ON COLUMN brasil_saas.bc_core_empresa.stripe_secret_key_encrypted
    IS 'Chave secreta Stripe da empresa, cifrada com APP_SECURITY_SECRET_ENCRYPTION_KEY';
COMMENT ON COLUMN brasil_saas.bc_core_empresa.stripe_webhook_secret_encrypted
    IS 'Signing secret do webhook Stripe da empresa, cifrado';
