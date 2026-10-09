-- Completa colunas usadas pelas entidades atuais, sem substituir valores legados.
-- Campos de negocio novos ficam nulos: datas e valores antigos nao sao inferidos.
-- UUIDs recebem identificadores novos; IDs e referencias existentes sao preservados.
SET search_path TO brasil_saas, public;

ALTER TABLE brasil_saas.bc_com_contrato ADD COLUMN IF NOT EXISTS condicao_pagamento_id bigint;
ALTER TABLE brasil_saas.bc_com_contrato ADD COLUMN IF NOT EXISTS observacao TEXT;
ALTER TABLE brasil_saas.bc_com_contrato ADD COLUMN IF NOT EXISTS tipo varchar(20);
ALTER TABLE brasil_saas.bc_core_notificacao ADD COLUMN IF NOT EXISTS updated_at timestamp(6);
ALTER TABLE brasil_saas.bc_core_notificacao ADD COLUMN IF NOT EXISTS link varchar(300);
ALTER TABLE brasil_saas.bc_fin_caixa_movimento ADD COLUMN IF NOT EXISTS deleted_at timestamp(6);
ALTER TABLE brasil_saas.bc_fin_caixa_movimento ADD COLUMN IF NOT EXISTS updated_at timestamp(6);
ALTER TABLE brasil_saas.bc_fin_caixa_movimento ADD COLUMN IF NOT EXISTS uuid uuid DEFAULT gen_random_uuid();
ALTER TABLE brasil_saas.bc_fin_cobranca_acao ADD COLUMN IF NOT EXISTS deleted_at timestamp(6);
ALTER TABLE brasil_saas.bc_fin_cobranca_acao ADD COLUMN IF NOT EXISTS updated_at timestamp(6);
ALTER TABLE brasil_saas.bc_fin_cobranca_acao ADD COLUMN IF NOT EXISTS uuid uuid DEFAULT gen_random_uuid();
ALTER TABLE brasil_saas.bc_fin_promessa_pagamento ADD COLUMN IF NOT EXISTS deleted_at timestamp(6);
ALTER TABLE brasil_saas.bc_fin_promessa_pagamento ADD COLUMN IF NOT EXISTS uuid uuid DEFAULT gen_random_uuid();
ALTER TABLE brasil_saas.bc_rh_rescisao ADD COLUMN IF NOT EXISTS uuid uuid DEFAULT gen_random_uuid();
ALTER TABLE brasil_saas.bc_ven_contrato ADD COLUMN IF NOT EXISTS valor numeric(18,2);
