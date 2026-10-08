-- Campos usados pela entidade de conferencia por item, ausentes em V171/V172.
ALTER TABLE brasil_saas.bc_com_conferencia_fatura_item
    ADD COLUMN IF NOT EXISTS tolerancia numeric(18,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'DIVERGENTE';

UPDATE brasil_saas.bc_com_conferencia_fatura_item
SET status = CASE WHEN conforme THEN 'APROVADA' ELSE 'DIVERGENTE' END;
