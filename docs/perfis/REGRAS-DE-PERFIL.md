# Matriz de permissao por funcao

> Reescrito em 05/10/2026. A versao de 27/09/2026 neste mesmo arquivo esta
> errada e foi substituida: ela tratava `bc_core_perfil_permissao` como a
> autoridade. Hoje a autoridade e o codigo. Este arquivo e a regra; se ele e o
> codigo divergirem, o codigo esta errado.

## Por que foi reescrito

As regras foram escritas quando o sistema nao tinha 22 modulos de negocio. O
resultado medido era:

```
DIRETORIA  2 de 252 permissoes
GERENTE    0 de 252
GESTOR     0 de 252
ADMIN      252 (perfil ativo=f, em desuso)
SUPERUSER  252
```

Diretoria com 2 e GERENTE com 0 era defeito, nao escolha. Alem disso o caminho
real de decisao nunca foi a lista de 252: sao ~450 metodos com
`@PreAuthorize("hasAuthority('modulo:recurso:acao')")` nos 22 modulos, e 93
linhas com `hasAnyRole(...)` concentradas em BI, producao, financeiro, fiscal e
core. Duas fontes de verdade, nenhuma delas fechada.

## A regra

Quem manda e o codigo. O banco guarda o rotulo da funcao e nada mais.

### 1. SUPERUSER - acima de tudo

| onde | o que e |
|---|---|
| AD | membro de `Administrators` **ou** `Domain Admins` |
| Postgres | role com `rolsuper=true` |
| ERP | atalho: pula toda checagem de permissao |

O SUPERUSER **nao tem lista de permissao**. Nenhuma permissao do ERP se aplica a
ele. Ele nao precisa de `bc_core_perfil_permissao` para nada, e por isso nao e
barrado por permissao nova que nascer depois.

A implementacao e um filtro que roda **antes** do `hasAuthority`. Os ~450
metodos nao sao tocados.

AD -> auth-service -> ERP: o grupo ja vem no login hoje
(`groups: ["GRP_DIRETORIA","GRP_GESTOR","GRP_GERENTE"]` no teste com `dark`).
O que falta e o ERP reconhecer `Administrators`/`Domain Admins` e marcar a
sessao.

### 2. FUNCAO - herda do SUPERUSER que a liberou

O grupo do AD, entregue pelo auth-service, define a funcao. O alcance vem do
codigo.

| funcao (role) | grupo no AD | OU | nivel | mexe em |
|---|---|---|---|---|
| `SUPERUSER` | `Administrators` / `Domain Admins` | - | 0 | tudo, sem checagem |
| `DIRETORIA` | `GRP_DIRETORIA` | `OU=Diretoria` | 1 | **todos os modulos** |
| `GERENTE` | `GRP_GERENTE` | `OU=Gestao` | 2 | **todos os modulos** |
| `GESTOR` | `GRP_GESTOR` | `OU=Gestao` | 2 | **todos os modulos** |
| `FINANCEIRO` | `GRP_FINANCEIRO` | `OU=Financeiro` | 3 | financeiro, contabilidade, fiscal, cadastro, vendas |
| `RH` | `GRP_RH` | `OU=RH` | 4 | rh, producao, financeiro (folha) |
| `VENDEDOR` | `GRP_VENDEDOR` | `OU=Vendas` | 5 | vendas, crm, cadastro (cliente), estoque (consulta) |
| `ESTOQUE` | `GRP_ESTOQUE` | `OU=Estoque` | 6 | estoque, wms, producao, compras, cadastro (produto) |
| `TECNOLOGIA` | `GRP_SA` | `OU=Tecnologia` | 3 | ia, integracoes, projetos, qualidade, dms |
| `CONSULTA` | - | - | 7 | somente leitura, sem gravar |
| `USUARIO` | - | - | 100 | nada ate ser vinculado |

DIRETORIA, GERENTE e GESTOR tem o **mesmo** alcance de dados. O que os diferencia
nao e dado: e quem pode editar quem, pela ordem do nivel.

### 3. O que sai

`bc_core_perfil_permissao` sai do caminho de decisao. As 252 linhas continuam no
banco como historico, mas nao decidem nada.

`ADMIN` deixa de existir como perfil. Hoje ele e `ativo=f`, e ha 57 endpoints que
exigem `hasRole('ADMIN')` sem o SUPERUSER. Com o filtro do superuser na frente,
esses 57 guards passam a pedir so `SUPERUSER`. O perfil `ADMIN` sai do banco.

`ERP_MODULO_*` e `ERP_EMPRESA_*` saem do codigo e do AD.

## Administracao do sistema

Isto e privativo do SUPERUSER, e nao e "mexer em dados":

| o que | onde | por que e restrito |
|---|---|---|
| SQL arbitrario | `GerenciadorSqlController` | executa qualquer comando no banco |
| criar/remover usuario | `SuperAdminController`, `UsuarioAdminController` | muda quem entra |
| criar/desativar empresa | `SuperAdminController` | cria tenant |
| armazenamento / config global | `ArmazenamentoAdminController`, `AIConfigController`, `PromptTemplateController` | muda o comportamento do sistema todo |
| certificado digital | `CertificadoDigitalController` | chave privada da empresa |
| relatorio agendado / BI / KPI | `bi/*` | leitura massiva cross-empresa |

Os demais `hasAnyRole` (comissao, caixa, apontamento de producao, NFe, NFS-e,
saldo de estoque) deixam de usar role e passam a ser decisao de funcao: quem
precisa disso e quem tem a funcao que cobre o modulo.

## Isolamento entre empresas

Superuser acessa todas as empresas. Todos os outros so a empresa deles. A empresa
vem do token e `EmpresaTenantIdentifierResolver` filtra as 84 `TenantEntity`.

A origem da empresa deixa de ser o grupo `ERP_EMPRESA_<cnpj>`. Passa a ser:

1. o vinculo no banco (`bc_core_usuario.empresa_id`), ou
2. o primeiro acesso (`POST /api/core/minha-empresa`), que ja esta escrito e
   nunca rodou porque o login morria antes com 409 `DATA_INTEGRITY`.

## Estado medido do AD (05/10/2026)

| grupo | membros humanos |
|---|---|
| `Administrators` | `Administrator` (builtin) |
| `Domain Admins` | `Administrator` (builtin), `Administrador SRVCLOUD Conta` |
| `GRP_DIRETORIA` | dark, marcos, saas |
| `GRP_GERENTE` | dark, marcos |
| `GRP_GESTOR` | dark, maria |
| `GRP_RH` | jose, julio |
| `GRP_FINANCEIRO` | debora, jose |
| `GRP_VENDEDOR` | sergio |
| `GRP_ESTOQUE` | felipe, jose |
| `GRP_SA` | - |

O `euripedes` **nao** esta em `Administrators` nem em `Domain Admins`. Para a
regra ter alguem de teste, ele entra nos dois.

## Estado medido do Postgres

```
rolsuper=true  -> postgres
rolsuper=false -> sa, euripedes, root, sysadmins, astral, astral_admin
```

Todas passam a `rolsuper=true`. `root`, `sysadmins`, `astral`, `astral_admin`
sao residuo do workflow de multi-banco que foi abandonado.

## Grupos ERP_* no AD: o que acontece

12 grupos `ERP_MODULO_*`, cada um com **um unico membro: `marcos`**. Eles sao
os 12 que o codigo usava para decidir gravacao (`ModuloAcessoService.java:126`).

Apagar e seguro: `marcos` esta direto em `GRP_DIRETORIA` e `GRP_GERENTE`, entao
continua com acesso aos 12 modulos pela regra nova. Nenhum outro usuario e
membro direto de qualquer `ERP_MODULO_*`.

2 grupos `ERP_EMPRESA_*`:

| grupo | membros |
|---|---|
| `ERP_EMPRESA_65527264000143` | debora, felipe, sergio, jose, euripedes, Administrador SRVCLOUD Conta |
| `ERP_EMPRESA_11222333000181` | vazio |

Nenhum dos dois CNPJs existe em `bc_core_empresa` (que tem 7 empresas). Esse e o
gelo do 409 `DATA_INTEGRITY`: `empresa_id=-1` gravado na tabela.

## Divida tecnica

| camada | arquivo | o que faz |
|---|---|---|
| filtro do superuser | novo, antes do `hasAuthority` | marca a sessao, isenta de permissao |
| filtro de empresa | `ExigeEmpresaFilter` | ja existe, libera o primeiro acesso |
| traducao grupo -> funcao | `AuthServiceImpl.provisionOrUpdateIdentity` | hoje so procura `ERP_EMPRESA_`; vira a unica fonte |
| mapa de modulos | `ModuloAcessoFilter` | hoje filtra por `/api/<modulo>`; passa a ter a tabela funcao -> modulos |
| heranca | `bc_core_perfil.hierarquia_nivel` | ja existe, vira a unica autoridade de escopo |

O `CustomUserDetailsService.java:131` (mapa de 9 grupos -> `ROLE_*`) e o
`AuthServiceImpl.java:407` (`ROLE_<perfil do banco>`) hoje traduzem grupo em role
em dois lugares que nao se encontram. Passam a ser um so.

## Nomes

`BRASIL-SAAS` e o nome. `brasilcloud` nao existe mais.
