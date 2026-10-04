> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

---
id: 2026-09-29-failover-4567-unit-ausente
status: pendente
data: 2026-09-29
owner: decisao do dono
---

# PENDENCIA OPERACIONAL: a 4567 nao tem o proxy (unit ausente)

> Decisao do dono em 29/09/2026: **nao instalar agora.** Registrar primeiro.

## O QUE ESTA REGISTRADO AQUI

Duas frases, e a distincao entre elas e' o ponto todo:

> **A arquitetura correta e' a 4567. O problema atual e' unit ausente — nao
> codigo ausente.**

## AS DUAS FRASES, SEPARADAS

| | |
|---|---|
| **Arquitetura correta** | 4567 = camada de failover. 4568 = implementacao Java. 4569 = implementacao Ruby. A 4567 **nao e' mais uma API** |
| **Problema atual** | `brasil_saas-nfse-failover.service` nao esta instalada neste servidor |

## O QUE JA EXISTE (e nao falta)

| Camada | Onde | Estado |
|---|---|---|
| Codigo do proxy | `src/main/resources/microservices/nfse-failover/nfse-failover.rb` | existe |
| As tres portas no codigo | `PORTA=4567`, `java=4568`, `ruby=4569` | existe |
| README com o desenho | `nfse-failover/README.md` | existe |
| Bloco que instala a unit | `installbase.sh` linhas 2164-2196 | existe, idempotente, com verificacao |
| Decisao do dono | commit `1ae94a78` (27/09), mantida em `b974f00c` (28/09) | registrada |
| Implementacoes | `nfse-sp-api` (4568, java), `nfse-sp-bridge` (4569, ruby) | no ar, HTTP 200 |

## O QUE FALTA

| | |
|---|---|
| `brasil_saas-nfse-failover.service` | nao instalada |

**Por que:** o bloco que a cria chegou no instalador em 28/09 (`b974f00c`) e esta
maquina nao foi reinstalada desde entao. **Nao e' regressao. E' instalacao
incompleta.**

## MEDICAO

```
4567 -> HTTP 000   (ninguem escuta)
4568 -> HTTP 200   nfse-sp-api      (java, pid 173990)
4569 -> HTTP 200   nfse-sp-bridge   (ruby, pid 1101)

systemctl list-unit-files | grep -i nfse
  nfse-sp-api.service      enabled
  nfse-sp-bridge.service   enabled
  # brasil_saas-nfse-failover.service: NAO EXISTE

ls /var/log/brasil-saas/ | grep nfse-failover
  # nada
```

## POR QUE NAO INSTALHO AGORA

O dono e' explicito: nao instalar. Tres motivos, todos registrados:

1. **Servico de producao, systemd, infraestrutura compartilhada.** O mesmo
   servidor hospeda ERP, Astral, microsservicos, RabbitMQ e Redis.
2. **A 4567 e' do outro lado de uma decisao do dono** (27/09). Tocar em
   infraestructura de producao sem ordem direta, nao.
3. **A alternativa ja esta descartada por decisao** — ver abaixo.

## O QUE NAO E' A SOLUCAO

> **Trocar `4567 -> 4568` no ERP.**

Isso remove justamente o que a arquitetura foi criada para entregar. O sistema
funciona hoje e quebra quando a 4568 cair. Descartado pelo dono.

## COMO RESOLVER (quando a hora vier)

```bash
# O bloco e' idempotente e ja tem verificacao:
sed -n '2164,2196p' ~/BrasilCloudERP/installbase.sh
```

Criar a unit a partir desse bloco, sem reinstalar nada. Depois:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:4567/api/nfse-sp/status
# esperado: 200
```

E validar o failover de verdade: derrubar a 4568 e ver a 4567 responder
atendendo com `X-Backend: ruby`.

## IMPACTO ENQUANTO ISSO

A emissao de NFS-e pelo ERP esta quebrada, porque o default do codigo e' a 4567:

```java
// NfseEmissaoService.java:74
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}") String url,
```

Sem override em `application.yml`, no `.env` nem em `/etc/brasil-saas/nfse-sp.env`.

