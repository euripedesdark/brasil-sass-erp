-- Devolucao de compra: solicita e baixa estoque ao devolver ao fornecedor.
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cmp_devolucao (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    pedido_id bigint NOT NULL,
    motivo varchar(500) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'SOLICITADA',
    devolvida_em timestamp,
    observacao varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE TABLE IF NOT EXISTS brasil_saas.bc_cmp_devolucao_item (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    empresa_id bigint NOT NULL,
    devolucao_id bigint NOT NULL REFERENCES brasil_saas.bc_cmp_devolucao(id),
    produto_id bigint,
    quantidade numeric(15,3) NOT NULL DEFAULT 0,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    deleted_at timestamp
);
CREATE INDEX IF NOT EXISTS ix_cmp_devolucao_pedido ON brasil_saas.bc_cmp_devolucao (empresa_id, pedido_id) WHERE deleted_at IS NULL;
SET search_path TO brasil_saas, public;
INSERT INTO bc_core_permissao (codigo, recurso, acao, descricao) VALUES
 ('compras:devolucao:leitura','devolucao','leitura','Consultar devolucoes de compra'),
 ('compras:devolucao:escrita','devolucao','escrita','Solicitar e devolver compras')
ON CONFLICT (codigo) DO NOTHING;
INSERT INTO bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, pe.id FROM bc_core_perfil p CROSS JOIN bc_core_permissao pe
WHERE p.nome IN ('ADMIN','SUPERUSER') AND pe.codigo IN ('compras:devolucao:leitura','compras:devolucao:escrita')
AND NOT EXISTS (SELECT 1 FROM bc_core_perfil_permissao pp WHERE pp.perfil_id=p.id AND pp.permissao_id=pe.id);
