# Segurança — credenciais expostas (2026-10-10)

## Ações obrigatórias (dono do projeto)

1. **env.txt** continha `PGUSER` e `PGPASSWORD` versionados no Git.
   - Arquivo removido do tracking e adicionado ao `.gitignore`.
   - **Troque a senha do usuário de banco imediatamente** (Neon/Postgres).
   - Credencial antiga deve ser considerada comprometida (permanece no histórico do Git).

2. **Token GitHub** (`ghp_…`) foi exposto em conversa de agente.
   - Revogue em https://github.com/settings/tokens
   - Gere um novo se ainda precisar; não reutilize o anterior.

## Achados reportados (não apagados sem aprovação)

| Caminho | Conteúdo | Ação sugerida |
|---------|----------|---------------|
| `dump-producao-partes/` | Dump PostgreSQL real (pg_dump 18.6), ~540 MB | Remover do repo e do histórico; storage privado |
| `dump-sanitizado-partes/` | Dump em partes ~540 MB | Confirmar sanitização; preferir fora do Git |
| `certs/easyrsa` | Script Easy-RSA 3 (ferramenta, não chave privada) | OK; não versionar `pki/`, `*.key`, `*.pem` |
| `certs/[Help` | Artefato vazio/estranho | Pode remover |

## Histórico Git

Após `git rm --cached env.txt` a senha permanece em commits antigos.
Purga completa exige `git filter-repo`/BFG **com backup e acordo do time**.
