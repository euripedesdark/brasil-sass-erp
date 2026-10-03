# Arquitetura — Brasil SaaS ERP

## Visão geral

O Brasil SaaS ERP é um monólito Spring Boot com frontend React servido pela própria aplicação. PostgreSQL é o banco relacional principal.

## Segurança e autenticação

A autenticação possui duas fontes de identidade:

1. **Usuário ERP** — username + senha validada com BCrypt contra brasil_saas.bc_core_usuario.
2. **PostgreSQL SUPERUSER** — username + senha validados diretamente no PostgreSQL. A role só pode substituir o BCrypt quando pg_roles.rolsuper=true.

A role técnica usada pelo datasource da aplicação é independente das credenciais usadas no login de um SUPERUSER.

### Fluxo

    POST /api/auth/login
             |
             v
    Busca usuário ERP
             |
             v
        tenta BCrypt
          /       \
        OK       falha
         |         |
         v         v
        JWT    PostgreSQL
               username/password
                    |
                rolsuper=true?
                 /       \
               sim       não
                |          |
                v          v
          reutiliza ou   INVALID_CREDENTIALS
          provisiona
          usuário ERP
                |
                v
          ADMIN + SUPERADMIN
                |
                v
          gera JWT/refresh

### Usuários ERP

Usuários comuns, como GERENTE, DIRETORIA e USUARIO, são autenticados por BCrypt. Perfil e permissão são mecanismos de autorização e não substituem a credencial.

### PostgreSQL SUPERUSER

Qualquer role PostgreSQL autenticada com rolsuper=true pode entrar no ERP usando sua própria senha PostgreSQL.

Se não existir um usuário ERP correspondente:

- o sistema cria a representação ERP;
- garante o perfil ADMIN;
- cria uma senha BCrypt aleatória somente para satisfazer o modelo;
- nunca copia a senha PostgreSQL para o ERP.

O JWT recebe ROLE_ADMIN e ROLE_SUPERADMIN.

### Separação de responsabilidades

    PostgreSQL
       |
       +-- identidade/credencial da role
       |
       +-- rolsuper
       |
       +-- SSL/pg_hba.conf

    Brasil SaaS ERP
       |
       +-- usuário
       +-- empresa/tenant
       +-- perfil
       +-- permissões
       +-- JWT

O PostgreSQL determina a identidade da role e, para SUPERUSER, autoriza o bypass do BCrypt. O ERP continua responsável por empresa, perfil, permissões e emissão do JWT.

## SSL e conexão

A aplicação usa certificado da role técnica sa para sua conexão normal ao PostgreSQL.

Na autenticação de um usuário PostgreSQL, PostgresRoleAuthenticationService abre uma conexão temporária usando:

- username informado;
- password informada;
- SSL obrigatório;
- sslmode=require.

Os parâmetros sslcert, sslkey e sslrootcert da conta técnica sa são removidos dessa conexão. O certificado da role técnica não é reutilizado e a autenticação atual não exige certificado de cliente.

O hardening com mTLS/certificado por role poderá ser aplicado posteriormente.

## JWT e autorização

Após autenticar, o sistema gera access token e refresh token. As authorities são compostas pelos perfis e pelas permissões efetivas, incluindo herança de perfil.

Para SUPERUSER, ROLE_ADMIN e ROLE_SUPERADMIN são adicionadas explicitamente.

## Componentes principais

- AuthServiceImpl — orquestra o login, BCrypt, PostgreSQL SUPERUSER, provisionamento e authorities.
- PostgresRoleAuthenticationService — autentica a role PostgreSQL e verifica rolsuper.
- UsuarioRepository — busca usuários ERP com perfis/permissões.
- Perfil — representa perfil e hierarquia.
- SecurityConfig — libera /api/auth/** e protege as demais APIs.
- frontend AuthService.js — envia credenciais e mantém os tokens.

Documentação detalhada: docs/autenticacao.md
