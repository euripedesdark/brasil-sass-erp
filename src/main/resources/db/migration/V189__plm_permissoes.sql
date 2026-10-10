-- Permissoes do ciclo de vida de engenharia (PLM).
-- Sem estas linhas, todo endpoint /api/plm responde 403, inclusive
-- SUPERUSER. Concede a SUPERUSER e ADMIN de todas as empresas.
-- Idempotente: nada muda se rodar de novo.

INSERT INTO brasil_saas.bc_core_permissao (codigo, recurso, acao, descricao)
SELECT v.codigo, v.recurso, v.acao, v.descricao
FROM (VALUES
    ('plm:leitura', 'plm', 'leitura', 'Consultar mudancas, efeitos e revisoes'),
    ('plm:escrita', 'plm', 'escrita', 'Criar mudancas, efeitos e revisoes, enviar e implementar'),
    ('plm:aprovar', 'plm', 'aprovar', 'Decidir etapa de aprovacao de mudanca')
) AS v(codigo, recurso, acao, descricao)
WHERE NOT EXISTS (
    SELECT 1 FROM brasil_saas.bc_core_permissao
    WHERE codigo = v.codigo AND deleted_at IS NULL
);

INSERT INTO brasil_saas.bc_core_perfil_permissao (perfil_id, permissao_id)
SELECT p.id, perm.id
FROM brasil_saas.bc_core_perfil p
JOIN brasil_saas.bc_core_permissao perm
  ON perm.codigo IN ('plm:leitura', 'plm:escrita', 'plm:aprovar')
 AND perm.deleted_at IS NULL
WHERE p.deleted_at IS NULL
  AND UPPER(p.nome) IN ('SUPERUSER', 'ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM brasil_saas.bc_core_perfil_permissao pp
      WHERE pp.perfil_id = p.id AND pp.permissao_id = perm.id
  );
