-- Permissao da aprovacao excepcional de conferencia de compra.
-- O endpoint POST /api/compras/conferencia-faturas/{id}/aprovacao-excepcional
-- exige hasAuthority('compras:conferencia:aprovar'). Sem esta linha, a
-- autoridade nao existe em bc_core_permissao e todo mundo recebe 403,
-- inclusive SUPERUSER: o botao na tela existiria mas negaria sempre.
-- Concede a SUPERUSER e ADMIN de todas as empresas (alcada de aprovacao).
-- Idempotente: nada muda se rodar de novo.

INSERT INTO brasil_saas.bc_core_permissao (codigo, recurso, acao, descricao)
SELECT 'compras:conferencia:aprovar', 'conferencia', 'aprovar',
       'Aprovar excepcionalmente conferencia de compra divergente'
WHERE NOT EXISTS (
    SELECT 1 FROM brasil_saas.bc_core_permissao
    WHERE codigo = 'compras:conferencia:aprovar' AND deleted_at IS NULL
);

INSERT INTO brasil_saas.bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, perm.id
FROM brasil_saas.bc_core_perfil p
JOIN brasil_saas.bc_core_permissao perm
  ON perm.codigo = 'compras:conferencia:aprovar' AND perm.deleted_at IS NULL
WHERE p.deleted_at IS NULL
  AND UPPER(p.nome) IN ('SUPERUSER', 'ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM brasil_saas.bc_core_perfil_permissao pp
      WHERE pp.perfil_id = p.id AND pp.permissao_id = perm.id
  );
