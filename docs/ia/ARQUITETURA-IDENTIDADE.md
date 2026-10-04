> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

---
id: 2026-09-30-arquitetura-identidade
status: decisao fechada pelo dono
data: 2026-09-30
---

# ARQUITETURA DE IDENTIDADE: Auth Service independente

> Decisão fechada em 30/09/2026. Este documento existe para que ninguém precise
> reconstruir o desenho a partir de conversa, e para que o código possa ser
> conferido contra ele.

---

## 1. A DECISÃO

**Auth Service NÃO faz parte do Astral.** É produto independente.

| | |
|---|---|
| Repo | `brasil-saas-auth` |
| Versionamento | próprio |
| Deploy | próprio |
| Systemd | próprio |
| Portas | **8100 / 8101** |
| OpenAPI | próprio |

**ERP, Astral, DBM e Proxy são apenas consumidores do contrato de identidade.**

## 2. O CONTRATO OFICIAL

```json
{
  "identityId": "...",
  "username": "...",
  "provider": "...",
  "groups": [...]
}
```

Usar **exclusivamente** este contrato. Não criar contratos alternativos.

## 3. O QUE A DECISÃO PROÍBE

| Proibido | Por quê |
|---|---|
| Dependência arquitetural de "Auth dentro do Astral" | Auth Service é produto separado |
| Módulos de identidade internos aos produtos | cada um seria um segundo Auth Service |
| Duplicar providers AD / Postgres / Linux / Cert nos consumidores | o Auth Service é o único que autentica |
| Contratos alternativos | dois contratos, duas verdades |
| Busca de identidade nos consumidores | quem responde "quem é" é o Auth Service |

## 4. MODO

```yaml
auth.mode=legacy   # o que existe hoje
auth.mode=ad       # o fluxo novo
```

O legacy continua existindo **atrás** de `auth.mode=legacy`.

## 5. O QUE EU VERIFIQUEI NO QUE CONSTRUÍ

Auditoria do que entreguei nesta sessão, contra a decisão:

| O que construí | Depende de auth no Astral? | Cria contrato alternativo? |
|---|---|---|
| `AssistenteController` | **não** | **não** |
| `bc_ia_assistente_auditoria` | **não** | **não** |
| `ContextoErpService`, `BuscaFiscalService` | não | não |
| Migrations V108–V113 | não | não |

O `AssistenteController` lê `Authentication.getName()` do **próprio** security
context do Spring. Isso não é contrato de identidade: é a pergunta que todo
consumidor faz ao seu próprio container ("quem está autenticado nesta requisição"),
e o Spring responde. Nenhum `Provider`, nenhum `LdapContext`, nenhum repositório
de identidade foi criado.

## 6. UM ACHADO QUE A DECISÃO EXPÕS

Sob o contrato novo, **`username` é rótulo, não chave.**

A tabela `bc_core_auth_source` — que é a materializedização do roteamento de
hoje — tem `username` como chave primária e `source` como valor:

```
astral     | POSTGRES
euripedes  | AD
postgres   | POSTGRES
root       | LINUX
sa         | POSTGRES
```

O mesmo nome pode entrar por providers diferentes, e o nome pode mudar de valor.
O `identityId` do contrato é justamente a âncora estável que resolve os dois.

**Por isso a V113 existe.** A V112 já tinha trocado o id do banco pelo nome, com o
raciocínio certo — id de banco morre com a troca do esquema de autenticação. Mas
o raciocínio parou no meio do caminho: gravar só o username numa auditoria é
gravar o rótulo sem a chave.

```
identity_id VARCHAR(128)   -- NULL enquanto o IdentityClient não existir
usuario_nome VARCHAR(256)  -- o rótulo, legível
```

**A coluna fica NULL de propósito.** Preencher com o username seria dizer que o
rótulo é a chave — que é exatamente o que a V112 deixou de fazer.

## 7. O QUE NÃO FIZ, E POR QUÊ

| Não fiz | Por quê |
|---|---|
| Não criei `IdentityClient` | o serviço não existe ainda. Inventar um cliente para um contrato não publicado seria **criar o contrato alternativo que a decisão proíbe** |
| Não criei busca de identidade | quem responde "quem é" é o Auth Service, não o ERP |
| Não mexi em `bc_core_auth_source` nem em `AuthServiceImpl` | são da V111 e de outra decisão; trabalho de outra IA |
| Não toquei em `AdProperties` nem `IdentidadeProperties` | `IdentidadeProperties` resolve domínio/UPN, que é **complementar** ao contrato, não concorrente |

## 8. O QUE FALTA, E DE QUEM

| | |
|---|---|
| `IdentityClient` no ERP | quando o `brasil-saas-auth` existir |
| `auth.mode` no ERP | não existe nenhuma ocorrência hoje: nem `auth.mode`, nem `IdentityClient` |
| Migrar `bc_core_auth_source` para o contrato | quando o serviço responder `identityId` e `provider` |
| Ligar `identity_id` na auditoria | mesma hora do `IdentityClient` |

## 9. COMO VERIFICAR

```bash
cd ~/BrasilCloudERP

# IdentityClient e auth.mode existem?
grep -rliE "IdentityClient|auth\.mode" --include=*.java --include=*.yml src/main/ | head

# a coluna está no lugar, e o que a auditoria tem hoje?
sudo -u postgres psql -d brasil-saas -c "
  select column_name, data_type
    from information_schema.columns
   where table_schema='brasil_saas' and table_name='bc_ia_assistente_auditoria'
     and column_name in ('usuario_id','usuario_nome','identity_id');"

sudo -u postgres psql -d brasil-saas -c "
  select usuario_nome, identity_id, status
    from brasil_saas.bc_ia_assistente_auditoria order by id desc limit 5;"
```

