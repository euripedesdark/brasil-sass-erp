# boleto_cnab_api — integração com o Financeiro do Brasil SaaS ERP

**Fonte:** https://github.com/akretion/boleto_cnab_api
**Cópia local:** commit `9969a8d5100e39fb24ee34c9d70d96a5df3296c2` (2026-09-08)
**Licença:** AGPL-3.0 (ver `LICENSE` no diretório)

Serviço em **Ruby** (Grape + Puma + BRCobranca). É a única peça do stack que
não é Java — por isso vive em `docker-compose.boleto.yml` separado. O ERP fala
com ela por HTTP, então derrubá-la não derruba o ERP.

---

## O que ela resolve

| Operação | Endpoint | Uso no ERP |
|---|---|---|
| Validar dados de boleto | `GET /api/boleto/validate` | valida antes de gravar |
| Gerar `nosso_numero` | `GET /api/boleto/nosso_numero` | vínculo título ↔ banco |
| Imprimir boleto | `GET /api/boleto?bank=&type=pdf&data=<json>` | PDF/PNG/JPG/TIF |
| Imprimir lote | `POST /api/boleto/multi` | carnê, folder |
| **Gerar remessa** | `POST /api/remessa` | `type=cnab240` ou `cnab400` |
| **Ler retorno** | `POST /api/retorno` | baixa de títulos em lote |

Remessa e retorno são o motivo principal: sem eles, cada banco é um caso
específico. Com eles, o ERP fala um formato só e a biblioteca resolve.

---

## Dependências

**Na máquina que roda o ERP: nada.** Tudo roda dentro do container.

Dentro do container (já na imagem oficial): Ruby, Bundler, Ghostscript
(rghost precisa dele para gerar PDF).

Se for rodar **sem** container, precisa de Ruby + Bundler + Ghostscript:

```bash
sudo apt install -y ruby-full bundler ghostscript
cd src/main/resources/microservices/boleto-cnab-api
bundle install
bundle exec puma -p 9292        # ou: rackup config.ru
```

Atenção: `brcobranca` vem de git e `rghost` está pinado em `0.9.8` de
propósito — a `0.9.9` publicada está quebrada (rghost#75).

---

## Subir

```bash
# junto com o stack
docker compose -f docker-compose.yml -f docker-compose.boleto.yml up -d

# só ela
docker compose -f docker-compose.boleto.yml up -d
```

Porta: `9292`. Doc interativa: `http://localhost:9292/docs`.

### Autenticação

A API aceita `API_KEYS` (lista separada por vírgula). O ERP manda
`X-Api-Key`. Vazia = sem autenticação, aceitável só em desenvolvimento.

```bash
export BOLETO_CNAB_API_KEYS="chave-empresa-a,chave-empresa-b"
```

---

## Como o ERP consome

Configuração (fora do `application.yml`, para não acoplar o ERP à existência
do serviço):

| Variável | Padrão | Papel |
|---|---|---|
| `brasil-saas_BOLETO_CNAB_URL` | `http://localhost:9292` | base da API |
| `brasil-saas_BOLETO_CNAB_API_KEY` | vazio | valor do `X-Api-Key` |
| `brasil-saas_BOLETO_CNAB_TIMEOUT` | `30000` | ms |

Cliente: `financeiro/service/BoletoCnabClient.java`
Serviço: `financeiro/service/BoletoService.java`
Controller: `financeiro/controller/BoletoController.java`

**O ERP trata a API como opcional.** Se ela estiver fora, o módulo de
financeiro continua funcionando: só a geração de boleto/remessa/retorno
responde 503 com mensagem clara. Não há dependência dura de inicialização.

---

## Gravidade dos documentos

Remessa gerada e retorno processado são arquivos de negócio, então seguem o
mesmo caminho de `staging` dos documentos: gravados em disco antes de ir ao
MongoDB e apagados do staging só depois da confirmação.

Tabelas: `bc_fin_boleto`, `bc_fin_remessa`, `bc_fin_retorno_bancario`
(migration `V90__financeiro_boleto_cnab.sql`).

---

## Formatos de entrada

`bank` aceita: `itau`, `caixa`, `santander`, `bradesco`, `bb`, `banco_do_brasil`.

Remessa: `data` é um **arquivo** JSON (multipart) com a lista de pagamentos.
Retorno: `data` é o **arquivo .RET/.TXT** do banco.

O campo `data` do `GET /api/boleto` é JSON **em string** (não arquivo), o que
difere do `POST /multi`.
