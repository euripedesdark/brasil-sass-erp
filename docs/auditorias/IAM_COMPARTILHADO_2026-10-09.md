# IAM compartilhado e autenticação do ERP

O `auth-service` é um projeto independente, usado por vários sistemas. Ele roda ao lado do ERP, não dentro dele. A autenticação por senha do ERP passa por `POST /api/v1/identity/authenticate`: provider `AD` para Samba/AD e provider `POSTGRES` para banco. `DB` permanece como alias no ERP; `/login/database` força `POSTGRES`, e `/login/ad` força `AD`.

O ERP não compara senha local nem abre conexão PostgreSQL com a senha digitada. IAM indisponível encerra essa tentativa de login; não há fallback por senha. O datasource técnico do ERP continua usando sua configuração de TLS/certificado `sa`. As credenciais pessoais e a emissão dos certificados pertencem ao IAM. A conexão que valida senha no IAM não usa o certificado técnico `sa`, pois autenticação PostgreSQL por certificado não prova a senha digitada.

## Entrada direta e autorização

- `Administrators` e `Domain Admins`, devolvidos pelo IAM, concedem `ROLE_SUPERUSER`.
- `Domain Users` entra sem criação obrigatória de perfil OU. Esse grupo, por si só, não concede privilégio de administrador nem permissões operacionais novas.
- `POSTGRES_SUPERUSER` concede `ROLE_SUPERUSER`. O IAM só adiciona esse marcador após consultar `pg_roles.rolsuper` na conexão autenticada da pessoa. Nome `postgres` e hash de usuário ERP não provam esse privilégio.
- Esses grupos não exigem criação automática do perfil `USUARIO`. Perfis já cadastrados são preservados.
- Outros grupos reconhecidos mantêm o mapeamento de funções existente. Os marcadores `ERP_MODULO_*` recebidos do IAM chegam às authorities e habilitam escrita segundo a regra de módulo existente; não liberam módulos sozinhos.
- A resposta de login e as requisições JWT usam o mesmo tradutor de grupos. Provider e grupos são preservados na renovação. Usuário inativo não autentica por JWT nem renova sessão; access token não serve como refresh token.

Uma OU é uma unidade organizacional. `OU=RH,OU=BrasilCloud`, por exemplo, não equivale a `GRP_RH`. O IAM atual devolve os grupos `memberOf`; não foi inventado mapeamento de permissões a partir da mera existência de OUs. O ERP também não passa a emitir certificados, consultar LDAP ou administrar o Samba.

## Implantação e limites

Incorporar primeiro a alteração do IAM que adiciona `POSTGRES_SUPERUSER`, depois a alteração consumidora do ERP. O IAM antigo autentica `POSTGRES`, mas não confirma superusuário nesse contrato. Usuário novo sem empresa continua exigindo vínculo/cadastro de empresa; não se inventa CNPJ ou perfil administrador para um nome de login.

Conservar as cadeias de confiança adotadas no servidor: certificados dos usuários AD/Samba e certificado técnico `sa` conforme cada serviço. A URL LDAPS e o truststore da JVM do IAM precisam confiar no certificado real do DC. A URL HTTP do IAM é configurável por `BRASIL_SAAS_AUTH_SERVICE_URL`; o padrão localhost mantém os dois processos na mesma máquina. Conexão fora dessa máquina exige o endpoint HTTPS correspondente.

A validação inclui contrato HTTP local, regras de grupos/sessões, suíte Java e teste PostgreSQL 18 no CI do IAM. Esses testes não provam a cadeia de confiança nem a emissão/renovação dos certificados do servidor do usuário. O script de provisionamento Samba enviado foi lido, não executado ou alterado. SPNEGO legado conserva seu caminho de tíquete, sem alteração neste incremento. Grupos em JWT permanecem válidos até a expiração da sessão conforme a política atual; revogação imediata no AD exige um fluxo próprio de atualização da identidade.
