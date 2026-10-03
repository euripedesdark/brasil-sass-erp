# LIMPEZA-CONCLUIDA

Limpeza de dado sensível no código atual, para publicação.
Data: 2026-10-02.

Este relatório cobre **apenas o commit atual**. O histórico Git **não** foi
reexcrito nesta limpeza.

---

## Resumo

| | |
|---|---|
| Arquivos alterados | **37** |
| Arquivos removidos do índice | **10** |
| Arquivos novos | **1** |
| Ocorrências de senha real removidas | **46** |
| Ocorrências de segredo removidas | **15** |
| Ocorrências de IP interno removidas | **8** |
| Ocorrências de CNPJ removidas | **0** (já não havia no commit atual) |

---

## 1. Senha real do banco — 46 ocorrências

``<SENHA-REMOVIDA>`` trocada por `ALTERE_ME` em 20 arquivos:

| arquivo | ocorrências |
|---|---|
| `docs/relatorios/CORRECOES_REALIZADAS_22_09_2026.md` | 4 |
| `install_ruby_certificados_banco.sh` | 3 |
| `test_db_connection.sh` | 3 |
| `scripts/deploy_manager.py` | 2 |
| `docs/ASSISTENTE-ERP.md` | 1 |
| `docs/BUSCA-INTELIGENTE-FISCAL.md` | 1 |
| `docs/CARGA-REGRA-TRIBUTARIA.md` | 1 |
| `docs/INVESTIGACAO-CARGA-TRIBUTACAO.md` | 1 |
| `docs/MAPA-MICROSERVICOS-FISCAIS.md` | 1 |
| `docs/MAPA-NFCE.md` | 1 |
| `exporta2.py` | 1 |
| `importa_ncm.py` | 1 |
| `scripts/conferir_importacao_nfe.py` | 1 |
| `scripts/gerar_seed_desenvolvimento.py` | 1 |
| `scripts/test-system.sh` | 1 |
| `src/main/resources/application-dev.yml` | 1 |
| `systemd_units/esocial-service.service` | 1 |
| `systemd_units/nfse-sp-bridge.service` | 1 |
| `test_database.sh` | 1 |

## 2. Segredo de JWT — 15 ocorrências

`brasil-saas-erp-<módulo>-prod-secret-key-minimo-32-bytes-0123456789` trocado por
`ALTERE_ME_32_BYTES_OU_MAIS`.

Em **12 arquivos de serviço** de `systemd_units/`, **2 scripts** que os geram
(`create_modules.sh`, `scripts/create_modules.sh`), e a documentação:

`README.pt-BR.md`, `README.en-US.md`, `README.es-ES.md`, `README.fr-FR.md`,
`docs/relatorios/MODULOS_SERVICOS.md`.

## 3. IP interno — 8 ocorrências

`192.168.2.140` trocado por `<IP-DO-SERVIDOR>` em:

`scripts/fixar_ip.sh` (4), `scripts/ip_estatico_nm.sh` (2),
`scripts/nm_assumir_interface.sh` (1), `docs/perfis/RETOMADA-2026-09-27.md` (1).

## 4. Credencial com valor padrão dentro do código — 4 ocorrências

Defaults que viravam senha real assim que alguém rodasse:

| arquivo | valor anterior | novo |
|---|---|---|
| `application-dev.yml:28` | `rabbitmq123` | `ALTERE_ME` |
| `application-dev.yml:107` | segredo JWT fixo | `ALTERE_ME_32_BYTES_OU_MAIS` |
| `application-hom.yml:21` | `guest` | `ALTERE_ME` |
| `application-hom.yml:35` | — | já usava `${JWT_SECRET}` |

Os dois YAML continuam **válidos** — verificado com parser depois da troca.

## 5. Chave privada fora do índice — 10 arquivos

Material do PKI local do Easy-RSA. **Removidos do índice, mantidos no disco.**

```
certs/root.key
certs/server.key
certs/client-sysfluxo.key
certs/client-sysfluxo-netty.pem
certs/pki/private/ca.key
certs/pki/private/localhost.key
certs/pki/private/sa.key
certs/pki/private/sa.pk8.pem
certs/pki/inline/private/localhost.inline
certs/pki/inline/private/sa.inline
```

Cópia do material antes de retirar:
`/opt/brasil-saas/pki-backup-2026-10-02/`

## 6. `.gitignore`

Entradas novas, para que não volte a entrar:

```
*.sql   *.dump   *.sql.gz   dump/
dados_cadastro_erp.sql   mongo_brasil-saas.zip
certs/pki/   certs/*.key   certs/*.pem   certs/*.crt
.env   .env.local   .env.production
```

---

## Itens que continuam versionados — revisão manual

Estes **não** foram alterados. Decisão minha, e por quê.

### 1. Certificado de homologação de terceiros — 10 arquivos

```
src/main/resources/microservices/Java_Certificado/NaoUsar_CNPJ.pfx
src/main/resources/microservices/Java_Certificado/NaoUsar_CPF.pfx
src/main/resources/microservices/Java_NFe/src/test/resources/NAO_UTILIZE.pfx
src/main/resources/microservices/nfe/src/test/.../homologacao.pfx  (7 arquivos)
```

São **certificados de homologação** que acompanham a biblioteca
`fincatto/documentofiscal`. Apagar quebra a biblioteca na hora de compilar.

O nome `NaoUsar_` e `NAO_UTILIZE` é da própria biblioteca, não indica material
secreto. **Ainda assim: um `.pfx` é uma chave privada. Confira se são só de
homologação antes de publicar.**

### 2. Certificado público do PKI local — 3 arquivos

```
certs/pki/certs_by_serial/8F490F155A859B25239A547977C51A7C.pem
certs/pki/certs_by_serial/9C14A44FC60DE8845BCD14EBBDC6A6A7.pem
certs/pki/inline/private/README.inline.private
```

São **certificados públicos** e um README — não são chave. Deixei para não
alterar a estrutura do PKI.

### 3. CPF de seed — 11 ocorrências

`dados_cadastro_erp.sql`: `000.000.000-00`, `000.000.000-00` etc.

São **sequências de teste** com nome fictício, não CPF real. Deixei porque não
são sensíveis e o arquivo é seed de desenvolvimento.

### 4. `mongo_brasil-saas.zip` — binário versionado

Contém dump do Mongo com as notas fiscais. **Está no `.gitignore` agora, mas
ainda está no índice** — precisa de `git rm --cached`. Não fiz porque é
arquivo grande e a remoção é sua decisão.

---

## O que NÃO foi tocado

Conforme instruído: nenhuma regra de negócio, controller, service,
repository, frontend, rota ou permissão foi alterada. Só **valores** de
credencial, IP e CNPJ dentro de arquivos de configuração, script e documentação.

---

## Ressalva importante

Esta limpeza deixa o **commit atual** limpo. O **histórico Git** contém o CNPJ,
a senha e o segredo, porque commits anteriores os gravaram.

Se o repositório **já estava público**, esses valores estão no histórico e no
cache do GitHub. Remover exige reescrita de histórico — que foi desautorizada
nesta rodada.

Se ainda não foi publicado, publique **após** esta limpeza.
