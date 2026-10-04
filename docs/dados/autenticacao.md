> **Atualização 24/09/2026:** documentado o fluxo atual de autenticação por BCrypt ou PostgreSQL SUPERUSER, o isolamento da conexão técnica `sa`, a remoção de `sslcert`/`sslkey`/`sslrootcert` na conexão temporária e a rejeição de refresh token como credencial Bearer. O hardening por certificado de cliente permanece planejado para uma etapa posterior.\n\n# Autenticação e Autorização — Brasil SaaS ERP

## Objetivo

O Brasil SaaS ERP possui dois mecanismos de identidade no login:

1. **Usuário ERP**: autenticação pela senha armazenada como hash BCrypt em brasil_saas.bc_core_usuario.senha_hash.
2. **PostgreSQL SUPERUSER**: autenticação diretamente contra o PostgreSQL usando a própria credencial da role. Uma role PostgreSQL só pode substituir o BCrypt do ERP quando rolsuper=true.

A senha da role PostgreSQL **nunca é copiada nem armazenada no cadastro do ERP**.

## Fluxo de login

Endpoint:

POST /api/auth/login

Payload:

    {
      "username": "usuario",
      "password": "senha"
    }

O backend executa deliberadamente nesta ordem:

    POST /api/auth/login
             |
             v
    Busca usuário ERP por username
             |
             v
    Usuário ativo?
             |
             v
    Tenta BCrypt
       /        \
     OK        falha
      |          |
      v          v
    JWT       tenta role PostgreSQL
                 |
            autentica + rolsuper=true?
              /          \
            sim          não
             |             |
             v             v
        reutiliza ou   INVALID_CREDENTIALS
        provisiona
        usuário ERP
             |
             v
       garante ADMIN
             |
             v
      ADMIN + SUPERADMIN
             |
             v
    Gera access JWT
    Gera refresh JWT
             |
             v
          HTTP 200

A senha PostgreSQL só é usada no segundo caminho. Um usuário ERP com BCrypt válido não precisa abrir uma conexão PostgreSQL adicional durante o login.

## Regra de precedência

### Usuário ERP normal

Um usuário como **carlos** utiliza:

    username
       |
       v
    bc_core_usuario
       |
       v
    senha_hash (BCrypt)
       |
       v
    perfil GERENTE / DIRETORIA / USUARIO / etc.
       |
       v
    permissões próprias + herdadas
       |
       v
    JWT

A existência de uma role PostgreSQL comum **não concede automaticamente acesso ao ERP**.

Portanto, criar uma role PostgreSQL chamada carlos não substitui a senha BCrypt do usuário ERP carlos.

### PostgreSQL SUPERUSER

Para uma role com:

    rolsuper = true

a própria senha PostgreSQL pode ser usada no login do ERP.

Exemplo:

    euripedes
       |
       +--> PostgreSQL autentica username/password
       |
       +--> pg_roles.rolsuper = true
       |
       +--> usuário ERP "euripedes" existe?
              |
              +-- não --> cria automaticamente
              |
              +-- sim --> reutiliza
       |
       +--> garante perfil ADMIN
       |
       +--> ROLE_ADMIN
       +--> ROLE_SUPERADMIN
       |
       +--> JWT

Isso vale para qualquer role PostgreSQL que seja realmente SUPERUSER, e não somente para a role postgres.

## Provisionamento automático de SUPERUSER

Quando um PostgreSQL SUPERUSER autentica pela primeira vez e ainda não existe no ERP, o sistema cria a representação ERP.

O provisionamento:

- usa a primeira empresa disponível;
- cria o perfil ADMIN se necessário;
- cria o usuário ERP com o mesmo username da role PostgreSQL;
- marca o usuário como ativo;
- associa o perfil ADMIN;
- gera uma senha BCrypt aleatória apenas para manter a entidade compatível com o modelo de dados;
- **não usa nem persiste a senha PostgreSQL**.

A autenticação desse usuário continua dependendo da role PostgreSQL.

## Autoridades do SUPERUSER

O JWT de um PostgreSQL SUPERUSER recebe:

- ROLE_ADMIN
- ROLE_SUPERADMIN
- permissões efetivas do perfil ERP

ROLE_ADMIN é mantida explicitamente porque existem endpoints protegidos com hasRole("ADMIN").

ROLE_SUPERADMIN identifica a origem administrativa elevada da autenticação PostgreSQL.

## Perfis ERP

Os perfis ERP continuam independentes da senha:

| Perfil | Origem da autenticação | Autoridade |
|---|---|---|
| ADMIN | BCrypt ou PostgreSQL SUPERUSER | ROLE_ADMIN |
| SUPERADMIN | PostgreSQL SUPERUSER | ROLE_SUPERADMIN + ROLE_ADMIN |
| DIRETORIA | BCrypt | ROLE_DIRETORIA |
| GERENTE | BCrypt | ROLE_GERENTE |
| USUARIO | BCrypt | ROLE_USUARIO |

O perfil determina autorização. A credencial determina autenticação.

## Permissões herdadas

Depois da autenticação, o sistema coleta:

1. permissões do perfil do usuário;
2. permissões dos perfis-pai;
3. toda a cadeia de herança;
4. remove duplicidades.

As permissões são adicionadas às authorities do JWT.

Um usuário com GERENTE, por exemplo, não ganha acesso apenas por existir no PostgreSQL. Ele precisa ter uma credencial ERP válida e o perfil correspondente.

## Senhas

### Senhas ERP

São verificadas com:

    passwordEncoder.matches(password, usuario.getSenhaHash())

O sistema não deve armazenar senha em texto puro.

### Senhas PostgreSQL

A senha fornecida no login é enviada diretamente para uma conexão PostgreSQL temporária usando:

- username da role;
- password informada;
- SSL obrigatório;
- sem certificado de cliente da role técnica;

O certificado de cliente da conta técnica `sa` não é reutilizado nessa autenticação e a conexão não depende do arquivo `~/.postgresql/root.crt`.

A senha PostgreSQL não é gravada na tabela de usuários ERP.

## Conexão técnica da aplicação

A aplicação usa a role técnica configurada no datasource para a conexão normal JPA/Hibernate.

Essa identidade é diferente da identidade usada no login de um PostgreSQL SUPERUSER.

Conceitualmente:

    Aplicação
       |
       +--> datasource técnico (sa + certificado)
       |       |
       |       +--> JPA / Hibernate / Flyway
       |
       +--> login PostgreSQL SUPERUSER
               |
               +--> conexão temporária
               +--> username/password informados pelo usuário
               +--> sslmode=require
               +--> consulta pg_roles

## SSL

A autenticação PostgreSQL usa SSL obrigatório, mas nesta fase não exige certificado de cliente para a role informada.

Para impedir que a credencial/certificado da conta técnica da aplicação seja reutilizada, o serviço remove da URL JDBC de autenticação:

- sslcert
- sslkey
- sslrootcert

e força:

    sslmode=require

Assim, a conexão temporária usa apenas o username/password informados no login e um canal SSL. O certificado de cliente da role técnica `sa` não participa da autenticação do SUPERUSER.

**Hardening planejado:** posteriormente poderá ser aplicado mTLS/certificado de cliente por role, mas isso não faz parte do fluxo atual.

## JWT

Após a autenticação:

- accessToken é emitido;
- refreshToken é emitido;
- o frontend armazena os tokens;
- o Axios/fetch envia Authorization: Bearer <accessToken>;
- o backend valida o JWT nas chamadas protegidas;
- o refreshToken é usado somente em `POST /api/auth/refresh`;
- refreshToken não é aceito como credencial Bearer nas APIs protegidas.

A autenticação é stateless.

## Erros de autenticação

### INVALID_CREDENTIALS

Indica que:

- o usuário ERP não existe; ou
- a senha BCrypt não confere; e
- a identidade PostgreSQL não autenticou como SUPERUSER.

HTTP:

    422 Unprocessable Entity

### USER_INACTIVE

O usuário ERP existe, mas está marcado como inativo.

### Erro PostgreSQL de certificado

Mensagens como:

    connection requires a valid client certificate

são problemas da política de autenticação do PostgreSQL (pg_hba.conf) para aquela role/conexão. Isso não significa que o perfil ERP esteja errado.

## Diagnóstico

Para verificar se uma role é SUPERUSER:

    SELECT rolname, rolsuper, rolcanlogin
    FROM pg_roles
    WHERE rolname = 'euripedes';

Para testar a própria autenticação PostgreSQL:

    psql -U euripedes -h localhost -d brasil_saas -W

Nunca registrar senhas nos logs ou nos arquivos Markdown.

## Sessão no frontend

Depois do login, o frontend persiste `accessToken`, `refreshToken` e os dados básicos do usuário no `localStorage`.

Isso permite restaurar uma sessão já existente após recarregar a página. Para forçar uma nova autenticação durante testes, é necessário executar logout ou limpar:

    localStorage.removeItem('brasil-saas_token')
    localStorage.removeItem('brasil-saas_refresh_token')
    localStorage.removeItem('brasil-saas_user')

Isso é comportamento de sessão do frontend e não altera a regra de autenticação do backend.

## Arquivos envolvidos

- src/main/java/br/com/brasil_saas/core/service/impl/AuthServiceImpl.java
  - orquestra o login;
  - valida BCrypt;
  - reconhece PostgreSQL SUPERUSER;
  - provisiona o usuário ERP;
  - monta authorities.

- src/main/java/br/com/brasil_saas/shared/security/PostgresRoleAuthenticationService.java
  - abre a conexão PostgreSQL temporária;
  - autentica username/password;
  - verifica rolsuper.

- src/main/java/br/com/brasil_saas/core/repository/UsuarioRepository.java
  - localiza usuários ERP e carrega perfis/permissões.

- src/main/java/br/com/brasil_saas/core/model/Usuario.java
  - representa o usuário ERP.

- src/main/java/br/com/brasil_saas/core/model/Perfil.java
  - representa perfis e herança de permissões.

- src/main/java/br/com/brasil_saas/shared/security/SecurityConfig.java
  - libera /api/auth/**;
  - exige JWT nas demais APIs protegidas.

- src/main/resources/static/react/src/services/AuthService.js
  - envia as credenciais;
  - armazena access/refresh token;
  - configura o Bearer token para as chamadas posteriores.

## Princípio de segurança

A regra central é:

> **Perfil não autentica usuário. Credencial autentica usuário; perfil autoriza o que ele pode fazer.**

E para PostgreSQL:

> **Somente uma role PostgreSQL autenticada e marcada como SUPERUSER pode substituir a senha BCrypt do ERP e receber SUPERADMIN.**

Essa separação impede que uma role PostgreSQL comum conceda acesso administrativo ao ERP apenas por existir no banco.
