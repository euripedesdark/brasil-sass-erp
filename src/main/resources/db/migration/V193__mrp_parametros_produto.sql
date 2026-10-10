-- Item 3: parametros de planejamento MRP temporal no cadastro de produto.
-- Usados por MrpPlanejadorTemporal (lead time, estoque de seguranca, lote minimo, multiplo).
-- Idempotente: ADD COLUMN IF NOT EXISTS.

ALTER TABLE brasil_saas.bc_cad_produto
  ADD COLUMN IF NOT EXISTS lead_time_dias integer NOT NULL DEFAULT 0;

ALTER TABLE brasil_saas.bc_cad_produto
  ADD COLUMN IF NOT EXISTS estoque_seguranca numeric(15,4) NOT NULL DEFAULT 0;

ALTER TABLE brasil_saas.bc_cad_produto
  ADD COLUMN IF NOT EXISTS lote_minimo numeric(15,4) NOT NULL DEFAULT 0;

ALTER TABLE brasil_saas.bc_cad_produto
  ADD COLUMN IF NOT EXISTS multiplo_lote numeric(15,4) NOT NULL DEFAULT 0;

-- Semente: estoque de seguranca herda estoque_minimo quando ainda zerado.
UPDATE brasil_saas.bc_cad_produto
   SET estoque_seguranca = estoque_minimo
 WHERE estoque_seguranca = 0
   AND estoque_minimo IS NOT NULL
   AND estoque_minimo > 0
   AND deleted_at IS NULL;

COMMENT ON COLUMN brasil_saas.bc_cad_produto.lead_time_dias IS
  'Lead time em dias para MRP temporal (liberacao = necessidade - lead_time).';
COMMENT ON COLUMN brasil_saas.bc_cad_produto.estoque_seguranca IS
  'Estoque de seguranca do MRP; dispara ordem quando saldo projetado fica abaixo.';
COMMENT ON COLUMN brasil_saas.bc_cad_produto.lote_minimo IS
  'Quantidade minima de uma ordem planejada pelo MRP.';
COMMENT ON COLUMN brasil_saas.bc_cad_produto.multiplo_lote IS
  'Multiplo de arredondamento da quantidade planejada (0 = sem arredondamento).';

-- Restricoes basicas (nao negativas). IF NOT EXISTS via drop+add seguro.
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'ck_cad_produto_mrp_nao_negativo'
      AND conrelid = 'brasil_saas.bc_cad_produto'::regclass
  ) THEN
    ALTER TABLE brasil_saas.bc_cad_produto
      ADD CONSTRAINT ck_cad_produto_mrp_nao_negativo
      CHECK (
        lead_time_dias >= 0
        AND estoque_seguranca >= 0
        AND lote_minimo >= 0
        AND multiplo_lote >= 0
      );
  END IF;
END $$;
