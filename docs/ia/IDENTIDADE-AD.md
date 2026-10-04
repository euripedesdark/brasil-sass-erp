> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Identidade no ERP: AD como mestre, sem ponto único de falha

> Escrito em 28/09/2026. Consolida o que foi decidido com o dono e medido no
> servidor `dc-erp.srvcloud.cloud`. Complementa `REGRA-ACESSO-ERP.md`, que
> tem a anatomia dos grupos; este tem o motivo de cada decisão e o que foi
> testado de fato.

## A regra do dono, em uma frase

**A identidade nasce no AD. O Postgres guarda dado e papel. O banco continua
funcionando se o AD cair.**

A segunda parte é a que mudou o desenho, e vem de uma pergunta que o dono fez
no meio da conversa: *"por que o kerberos e o ad sao mestres nisso"*.

## Por que o AD é mestre de identidade

Um lugar só onde nasce a pessoa. Se usuário, senha e grupo vivessem no
Postgres, existiriam dois lugares — a conta no AD e a linha em
`bc_core_usuario` — e eles divergiriam. A pessoa entraria no Windows, alguém
esqueceria o ERP, e o login falharia sem causa aparente. O dono pediu para
"criar vários usuários e grupos para o banco" sem esforço; isso só é possível
com uma fonte única.

## O que o AD NÃO é mestre

Nem dos dados, nem da disponibilidade.

### O erro que essa resposta impediu

O desenho inicial-era: o ERP conecta no Postgres falando **GSSAPI** com o AD.
Isso faria do AD **ponto único de falha do banco**. Com o AD fora, o ERP não
abriria conexão — não só o login, o sistema inteiro. O AD viraria mestre do
banco, que é o contrário do desejado.

Corrigido: **a conexão do ERP com o Postgres é local, com certificado**, como
estava. GSSAPI fica só para o `psql` e para scripts, que é onde a conta do
Windows faz sentido — porque é uma pessoa. Um serviço não depende de um
domínio para fazer o que já consegue fazer localmente.

| | mestre | se o AD cair |
|---|---|---|
| quem entra | AD, via Kerberos | ninguém entra |
| qual empresa | AD, `ERP_EMPRESA_<cnpj>` | ninguém entra |
| quais módulos | AD, `ERP_MODULO_*` | ninguém entra |
| **os dados** | **Postgres local** | **continua rodando** |
| **ERP → banco** | **certificado local** | **continua funcionando** |
| seu `psql` | GSSAPI, conta do Windows | precisa do caminho local |

## Três caminhos de emergência

| conta | onde | como entra | por que existe |
|---|---|---|---|
| `root` | SO | senha | se o SO quebrar |
| `postgres` | Postgres, superuser | **senha** | se o AD cair e precisar de alguém no banco |
| `Administrator` | AD | senha | se o ERP for reconstruído e a identidade precisar ser recriada |

**`postgres` nunca usa certificado, sempre senha.** Regra do dono. A divisão
que isso desenha é boa: o papel `sa`, que o **aplicativo** usa, fica com
certificado de cliente — é conexão de máquina, não de pessoa, e não deve ter
senha guardada em arquivo. E o superuser, que é pessoa, fica só com senha, na
cabeça do dono e num cofre, nunca no `.env`, nunca em script, nunca no git.

```sql
local   all   postgres   peer              -- acesso sem senha, pelo proprio SO
local   all   postgres   scram-sha-256
hostssl "brasil-saas"  postgres  127.0.0.1/32  scram-sha-256
```

O `peer` fica, que é o que garante acesso mesmo com senha esquecida.

A senha do superuser precisa existir em **dois lugares que não sejam o
repositório**. Se estiver só num deles, o dia em que for preciso é o dia em
que não está.

## Como a autenticação vai funcionar: Spring Security

Não com código próprio. `AuthenticationProvider` é a abstração para isso.

```java
// 1. local — nunca desliga, nunca consulta o AD
@Component
class LocalAuthenticationProvider implements AuthenticationProvider {
    public Authentication authenticate(Authentication auth) {
        // senha, pelo nome da conta local
        // devolve null se não for conta local
    }
}

// 2. AD — valida o tíquete SPNEGO
@Component
class AdKerberosAuthenticationProvider implements AuthenticationProvider { ... }

// 3. a ordem É a regra de emergência
@Bean
ProviderManager providerManager(LocalAuthenticationProvider local,
                                 AdKerberosAuthenticationProvider ad) {
    return new ProviderManager(List.of(local, ad));
}
```

A ordem faz o trabalho: local primeiro, AD depois. Se o `LocalAuthenticationProvider`
devolve `null`, o `ProviderManager` passa ao próximo. Se o AD está fora, o
local já atendeu antes.

**O erro de implementação mais provável:** o `LocalAuthenticationProvider` não
pode lançar exceção quando o usuário não existe localmente. Se lançar, o
`ProviderManager` para e nunca chega no provedor do AD. Tem que devolver
`null`. Quem erra isso ganha o sintoma "só funciona um dos dois".

Depois de autenticado, o `UserDetailsService` lê os grupos no AD e monta as
autoridades a partir da **tabela de mapeamento** — que é o que torna a
identidade *dado* e não código:

```java
Set<String> grupos = ldapGruposDoUsuario(upn);   // com OID transitivo
for (IdentityGroupMapping m : repository.findAll()) {
    if (grupos.contains(m.getAdGroup())) {
        authorities.add(m.getRole());
        if (m.getModulo() != null) modulos.add(m.getModulo());
        if (m.getCnpj()   != null) empresa   = resolveEmpresa(m.getCnpj());
    }
}
```

Trocar provedor, apontar para outro AD ou mudar o mapeamento deixa de ser
recompilar e subir jar.

## Quem valida a identidade: o app, nao o nginx

Esta e' a pergunta que decide a seguranca do desenho inteiro: **como o app
sabe quem entrou?**

### Hoje, sem Kerberos

O app sabe porque emite o proprio JWT. Grava o `username` dentro e assina com
o segredo do servidor. Quem nao tiver o segredo nao gera um token valido. A
identidade e' criptograficamente provada e o app nao confia em nada externo.

### O caminho que foi descartado, e por que

Primeiro foi proposto que o **nginx** validasse o tiquete e mandasse
`X-Remote-User: euripedes` no header, com o app lendo o header. Isso foi
descartado: o app nao saberia quem entrou, ele *acreditaria* num texto que
chegou pela rede. Afirmacao nao e prova. E a cadeia inteira passaria a
depender de uma coisa nao verificada — que o nginx validou mesmo o tiquete, e
nao so aceitou o header.

### O caminho certo

O **proprio app** valida o tiquete, com o keytab, pelo
`SunJaasKerberosTicketValidator`. A identidade passa a ser provada por
criptografia, e o nginx so repassa o token — ele nao participa da decisao.

E' o que o `setup-kerberos-auth.sh` do Astral ja faz: `SpnegoEntryPoint`,
`SpnegoAuthenticationProcessingFilter` e `SunJaasKerberosTicketValidator`
dentro do Java, com o keytab dele. Nao e' `mod_auth_gervasp` no nginx.

| | quem valida | o app confia em |
|---|---|---|
| header | nginx | num texto |
| **SPNEGO no app** | **o proprio app, com keytab** | **em nada** |

O `8080` fechado para `127.0.0.1` continua valendo por outro motivo: com o
app validando, quem chega direto na 8080 sem tiquete e' barrado pela propria
cadeia, mas fechar a porta e' o que impede de tentar.

## A consulta tem que ser transitiva

Com grupos aninhados, o `memberOf` do usuário **não mostra** o grupo
externo. O LDAP devolve só a associação direta. A consulta precisa do OID do
casamento transitivo:

```
(memberOf:1.2.840.113556.1.4.1941:CN=GRP_ERP_ADMIN,CN=Users,DC=srvcloud,DC=cloud)
```

`1.2.840.113556.1.4.1941` é o `LDAP_MATCHING_RULE_IN_CHAIN`. Sem ele, a regra
"tem que estar nos dois grupos" não vale para ninguém, e o sintoma engana:
ninguém entra e parece defeito do Samba, quando é consulta rasa demais.

Medido no AD: o `euripedes` tem quatro `memberOf`, todos **diretos**. Se
amanhã alguém entrar em `GRP_ERP_ADMIN` sem estar em `GRP_ERP_ACESSO`
diretamente, o `memberOf` dele não vai mostrar o acesso — porque herdado não
aparece.

## A direção do aninhamento

`GRP_ERP_ADMIN` está **dentro** de `GRP_ERP_ACESSO`. Então:

- quem está em `GRP_ERP_ADMIN` **herda** `GRP_ERP_ACESSO`
- quem está só em `GRP_ERP_ACESSO` **não tem** admin

Isso implementa a regra dos dois grupos ao mesmo tempo com uma associação só.
Escrevi o contrário numa primeira versão do documento, e o primeiro erro de
quem implementa é supor que aninhamento funciona nos dois sentidos. Não
funciona.

## Papel por empresa, e a regra do dono

> *"Ele só não pode repetir funções, tipo ser admin em 2 empresas ou diretor
> em 2 empresas."*

| | pode? |
|---|---|
| ADMIN na 00000000000191 + DIRETORIA na 00000000000192 | **sim** |
| ADMIN na 00000000000191 + ADMIN na 00000000000192 | **não** |
| DIRETORIA numa + DIRETORIA na outra | **não** |

O banco garante isso, com **recusa que explica o motivo**, como o dono pediu.

### A migration V105, e o furo que o teste pegou

A primeira versão punha `UNIQUE (usuario_id, perfil_id)`, achando que "admin
na empresa 1" e "admin na empresa 5" seriam o mesmo registro. **Não são**: o
perfil é dado de tenant, então cada empresa tem a **sua própria linha** em
`bc_core_perfil`, com nome igual e id diferente. O banco aceitou admin nas duas
empresas — exatamente o que a regra proíbe. A constraint estava
silenciosamente errada.

Corrigido: `UNIQUE (usuario_id, perfil_nome)`, com o nome desnormalizado na
tabela. Não dá para fazer `UNIQUE` sobre subquery em índice — o Postgres
recusa. Dois níveis de garantia: a `UNIQUE` barra mesmo sem o trigger, e o
trigger dá a mensagem que explica. Se um falhar, o outro segura.

A mensagem que o usuário recebe:

> Não é possível dar o papel ADMIN a euripedes: esta pessoa já é ADMIN na
> empresa "...". Uma pessoa pode ter papéis diferentes em empresas diferentes,
> mas não o mesmo papel duas vezes. Remova o vínculo existente antes de criar
> este.

### Testes que rodaram

| teste | resultado |
|---|---|
| A — ADMIN na 1 e ADMIN na 5, no INSERT | recusou, com a mensagem |
| B — ADMIN na 1 e DIRETORIA na 5 | aceitou |
| C — UPDATE para ADMIN que já tem | recusou |
| D — trigger desligado | a `UNIQUE` segurou sozinha |
| E — outra pessoa com o mesmo ADMIN | aceitou (é por pessoa) |
| F — mesmo grupo duas vezes | recusou |

Cenário real, com o `euripedes`:

| papel | empresa | CNPJ |
|---|---|---|
| ADMIN | 1 | 00000000000191 |
| SUPERUSER | 1 | 00000000000191 |
| DIRETORIA | 5 | 00000000000192 |
| GERENTE | 5 | 00000000000192 |

Tentativa de ADMIN na empresa 5: **recusada**.

### Perfis por empresa: o que estava faltando

Os 10 perfis existiam **só na empresa 1**. Sem perfil na empresa 5, ninguém
podia ter cargo lá — não por regra, por ausência. Criados os 10 na empresa 5,
copiando nome, descrição e nível, com o `GERENTE` ligado à `DIRETORIA` como na
matriz. Sem isso a regra existia mas não havia como exercitá-la.

## Herança de filial vem do banco

`bc_core_empresa_vinculo` já tem `empresa_id`, `empresa_vinculada_id` e
`tipo`. Se a filial herda da matriz, o ERP acha a matriz pelo vínculo e o
admin da matriz vale na filial. Fazer isso por aninhamento de grupo duplicaria
uma informação que o banco já tem, e as duas divergiriam.

## Domain admin e as outras empresas

Já é o comportamento do filtro de tenant e não precisa de código novo: sem o
`empresa_id` da outra empresa no token, ele não vê. Para mexer na outra
empresa, entra por `psql` como `postgres` ou `sa`. É o que o dono descreveu, e
é o que o sistema já faz.

## O que o Postgres deixa de ser

`bc_core_perfil` e `bc_core_usuario_perfil` deixam de ser consultados para
autorização. As tabelas **continuam existindo**: `bc_core_empresa` dá razão
social, CNPJ e endereço do tenant, e `bc_core_usuario` guarda o espelho do
usuário. O que sai é o uso para autorização, não as tabelas.

Isso resolve de quebra o `hasPermission('IA','READ')` que estava quebrado —
a permissão passa a ser o nome do grupo, e não existe formato incompatível
entre o guard e o que está gravado.

## O que falta

- **Keytab e o certificado do DC.** O realm é `SRVCLOUD.CLOUD` e o DC existe
  em `dc-erp.srvcloud.cloud`, mas o keytab do ERP ainda não foi gerado.
- **`bc_core_identity_config` e `bc_core_identity_group`.** A tabela de
  mapeamento que faz a identidade virar dado. Ainda não existem.
- **A troca de empresa no login.** `bc_core_usuario` tem um `empresa_id`
  só; com vínculos por empresa, a pessoa escolhe em qual está trabalhando e o
  token passa a carregar essa. O `EmpresaTenantIdentifierResolver` já filtra
  pelo valor do token, então é trocar o valor.
- **O `8080` fechado para `127.0.0.1`.** O header de identidade vem do nginx;
  quem conectar direto na 8080 poderia escrever o nome de qualquer um.
- **`felipe-ti` e `srvcloud` não existem** nem no AD nem no banco. Nunca foram
  gravados nos dois lados.
- **O notebook erp segue sem rede**, desde que a configuração de rede dele foi
  alterada. Nada foi executado nele depois disso. O dump de transferência
  (167 tabelas) está no notebook, em `/tmp/opencode/dump_transferencia/`.

## Dependência de ecossistema: BrasilCloud Auth Service

A autenticação corporativa do ERP passa pelo BrasilCloud Auth Service
(autenticação centralizada):

- Repo: https://github.com/euripedesdark/auth-service (licença AGPLv3)
- Requisitos: LDAP, LDAPS, Active Directory
- Linux: Samba Active Directory — guia: https://medium.com/meetcyber/full-guide-deploying-samba-active-directory-on-fedora-and-rocky-with-hybrid-dns-and-gpo-support-30f47fab1e90
- Windows: Windows Server Active Directory com LDAPS habilitado

O ERP pode ser avaliado sem o ambiente corporativo completo; em produção,
usa-se o Auth Service integrado ao diretório LDAP/LDAPS.
