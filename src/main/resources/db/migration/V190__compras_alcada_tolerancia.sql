-- Alcada de aprovacao de pedido de compra + tolerancia cadastral de conferencia.
-- Idempotente.
ALTER TABLE brasil_saas.bc_com_pedido
    ADD COLUMN IF NOT EXISTS status_aprovacao VARCHAR(20) NOT NULL DEFAULT 'APROVADO',
    ADD COLUMN IF NOT EXISTS aprovado_por BIGINT,
    ADD COLUMN IF NOT EXISTS aprovado_em TIMESTAMP,
    ADD COLUMN IF NOT EXISTS motivo_rejeicao TEXT;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_alcada (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    valor_limite NUMERIC(15,2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_com_alcada_empresa ON brasil_saas.bc_com_alcada (empresa_id) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS brasil_saas.bc_com_tolerancia (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    empresa_id BIGINT NOT NULL,
    tipo VARCHAR(12) NOT NULL DEFAULT 'VALOR',
    limite NUMERIC(15,4) NOT NULL,
    produto_id BIGINT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_com_tolerancia_empresa ON brasil_saas.bc_com_tolerancia (empresa_id) WHERE deleted_at IS NULL;

INSERT INTO brasil_saas.bc_core_permissao (codigo, recurso, acao, descricao)
SELECT 'compras:pedido:aprovar', 'pedido', 'aprovar', 'Aprovar ou rejeitar pedido de compra acima da alcada'
WHERE NOT EXISTS (
    SELECT 1 FROM brasil_saas.bc_core_permissao
    WHERE codigo = 'compras:pedido:aprovar' AND deleted_at IS NULL
);

INSERT INTO brasil_saas.bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, perm.id
FROM brasil_saas.bc_core_perfil p
JOIN brasil_saas.bc_core_permissao perm
  ON perm.codigo = 'compras:pedido:aprovar' AND perm.deleted_at IS NULL
WHERE p.deleted_at IS NULL
  AND UPPER(p.nome) IN ('SUPERUSER', 'ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM brasil_saas.bc_core_perfil_permissao pp
      WHERE pp.perfil_id = p.id AND pp.permissao_id = perm.id
  );
