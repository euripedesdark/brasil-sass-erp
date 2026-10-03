# Regras de perfil e o que cada um pode

> Escrito em 27/09/2026 porque a regra mudou e eu não registrei. Sem este
> arquivo a próxima sessão trabalha com a regra de ontem, que é o que já
> aconteceu.

## A regra

**SUPERADMIN = SUPERUSER.** É o mesmo perfil, um nome só. A diferença entre os
dois não é o perfil — é a **capacidade**:

| quem | banco de **dentro** do sistema | banco de **fora** (comando, script, query) |
|---|---|---|
| o superuser que **é** superadmin — o `euripedes` | **sim** | sim |
| o superuser sozinho — o `postgres` | **não** | sim |

Ou seja: o mesmo perfil `SUPERUSER` cobre os dois. O que separa é poder mexer
no banco **de dentro do sistema** (SQL rodado pelo próprio ERP). Quem é só
superuser mexe no banco **por fora**, via psql, script ou query — mas não por
dentro do sistema.

O `euripedes` tem os dois perfis, SUPERUSER e ADMIN.

`ADMIN` é o segundo nível e também tem acesso total hoje, mas está em
transição: a V104 desativou o perfil ADMIN para uso geral e passou as 221
permissões para o SUPERUSER. O perfil continua no banco com as 221 porque
ainda há código que o exige.

## Os perfis que existem no banco

| Perfil | Permissões | Papel |
|---|---|---|
| `SUPERUSER` | 221 | Topo. Acessa todas as empresas, toda a operação e o SQL |
| `ADMIN` | 221 | Acesso total, em desuso planejado (ver acima) |
| `CONSULTA` | 25 | Somente leitura |
| `FINANCEIRO` | 19 | Operação financeira |
| `ESTOQUE` | 20 | Operação de estoque |
| `RH` | 7 | Recursos humanos |
| `VENDEDOR` | 5 | Vendas e atendimento |
| `DIRETORIA` | 2 | Cria e exclui os níveis abaixo do seu |
| `GERENTE` | 0 | Criado, sem nenhuma permissão |
| `GESTOR` | 0 | Criado, sem nenhuma permissão |

**DIRETORIA com 2 permissões e GERENTE com 0 é defeito, não escolha.** Uma
diretora entra no sistema e não pode fazer quase nada. A matriz por nível ainda
não foi montada.

## Isolamento entre empresas

Só o **SUPERUSER** vê e altera dados de todas as empresas. Todos os outros
só veem os dados da empresa deles. A empresa vem dentro do token e a
`EmpresaTenantIdentifierResolver` aplica o filtro em toda query de entidade que
estende `TenantEntity` (84 entidades), com a anotação `@TenantId`.

Prova: com o token da empresa 1, o banco tem 179 produtos da empresa 1 e 1 da
empresa 5, e a API devolve só os da empresa 1. No log, 38.810 queries com
`empresa_id`.

## `SUPERADMIN` não existe

A palavra `SUPERADMIN` aparece em vários `@PreAuthorize`
(`hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')`), mas **não há um perfil
`SUPERADMIN` no banco**. A parte do SUPERADMIN não casa com ninguém. Não causa
erro, porque `hasAnyRole` passa se **qualquer um** dos nomes casar — mas
confundir quem lê.

## Quem tem o quê

| Usuário | Perfis |
|---|---|
| `euripedes` | **SUPERUSER e ADMIN** — os dois |
| `sysdba` | SUPERUSER |
| `postgres` | ADMIN |
| `diretora.helena` | DIRETORIA |

O `euripedes` tem os dois perfis, então passa nos guards que aceitam ADMIN.

## O SUPERUSER sendo barrado

**57 endpoints** têm `@PreAuthorize("hasRole('ADMIN')")`, sem o SUPERUSER. Como
a herança de perfil é decorativa — nenhum código percorre `perfil_pai_id`, e o
SQL de authorities lê só o perfil direto do usuário — um usuário que é **só
SUPERUSER** é **negado** nesses 57 endpoints.

O `sysdba` está exatamente nesse caso: SUPERUSER, sem ADMIN. Ele é barrado em
57 telas que o `euripedes`, que tem os dois, usa sem problema.

É a contradição da regra: o SUPERUSER pode tudo, mas há 57 telas em que só o
ADMIN passa.

**O conserto é trocar `hasRole('ADMIN')` por
`hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')`, que é o que
`UsuarioAdminController` já usa.** Os 15 guards que já aceitam SUPERUSER estão
corretos.

## O nome antigo não pode mais aparecer

**BRASIL-SAAS é o nome. `brasilcloud` não existe mais — em nenhum lugar.**

Isso vale para código, configuração, script, documento, nome de arquivo e nome
de pasta. Em 27/09/2026 foram limpas as últimas ~4.000 ocorrências, que estavam
em: chaves de `localStorage` do React (`brasilcloud_token`,
`brasilcloud_empresa_id`, `brasilcloud_user`, `brasilcloud_refresh_token`),
variáveis de ambiente (`BRASILCLOUD_*`), upstreams do nginx, o
`docker-compose`, os workflows do CI, os dumps de SQL, o `README` e 94
documentos.

Regras de nome, para não voltar a errar:

| coisa | nome | exemplo |
|---|---|---|
| banco de dados | `brasil-saas` | `-d brasil-saas` |
| schema do Postgres | `brasil_saas` | `currentSchema=brasil_saas` |
| banco do Mongo | `brasil_saas` | `27017/brasil_saas` |
| variável de ambiente | `BRASIL_SAAS_*` | `BRASIL_SAAS_MDFE_CERT_PASS` |
| pasta de sistema | `/etc/brasil-saas` | `/etc/brasil-saas/pki` |
| unit do systemd | `brasil_saas-*` | `brasil_saas-erp.service` |
| chave de `localStorage` | `brasil-saas_*` | `brasil-saas_token` |
| container / serviço | `brasil-saas-*` | `brasil-saas-erp` |

**A pasta do repositório ainda se chama `BrasilCloudERP`** (450 ocorrências) —
essa é a única que ficou, porque a renomeação do git é do dono.

## Criar e remover usuário

Não existia rota para isso, e a tela também não tinha o botão ligado. Resolvido
em 27/09/2026 — o detalhe está em `RETOMADA-2026-09-27.md`.

    POST   /api/superadmin/usuarios             cria
    PUT    /api/superadmin/usuarios/{id}        altera
    PUT    /api/superadmin/usuarios/{id}/senha  troca a senha
    DELETE /api/superadmin/usuarios/{id}        desativa
    GET    /api/superadmin/usuarios/perfis-disponiveis

`DELETE` desativa, não apaga: usuário tem histórico em nota, venda e auditoria.

Duas travas: não se remove a si mesmo, e não se desativa o último SUPERUSER
ativo. Sem elas, o sistema fica sem ninguém capaz de administrar.

O SUPERUSER escolhe a empresa do usuário no corpo. O ADMIN não: só cria na
empresa dele, e empresa diferente é recusada com mensagem.
