---
id: 2026-09-26-o-servico-em-producao
status: confirmado
data: 2026-09-26
---

# O serviço de NFS-e em produção

Uma API só. A de São Paulo capital, na porta 4567, que é a que o ERP espera.

## Arranjo

```
  ERP  ──POST /api/fiscal/nfse/emitir──▶  RestClient
                                              │
                    brasil-saas.fiscal.nfse.url
                    default: http://127.0.0.1:4567/api/nfse-sp
                                              │
                                              ▼
                              nfse-sp-api  (Java, 4567)
                                              │  SOAP + XMLDSig + mTLS
                                              ▼
                    https://nfews.prefeitura.sp.gov.br/lotenfe.asmx
```

**Um processo só, uma porta só.** Não há proxy entre o ERP e a API, e não há
segunda implementação.

## A tabela do serviço

| serviço | papel | estado |
|---|---|---|
| `nfse-sp-api` | **a NFS-e**, porta 4567 | `active`, `enabled` |
| `nfse-sp-bridge` | implementação Ruby | `inactive`, `disabled` |
| `nginx` | roteava entre as duas | `inactive`, `disabled` |
| `nfse-watchdog` | trocava de implementação | `inactive`, `disabled` |

Os quatro continuam no disco e nas units, fora do boot. Religar é
`systemctl enable --now <nome>`.

## A unit

`/etc/systemd/system/nfse-sp-api.service`. Cópia em
`nfse-sp-api.service.bak-4568`, que é a versão na 4568.

O `ExecStart` tem **aspas no caminho**, e isso não é detalhe:

```ini
ExecStart="/home/.../java" -jar "/home/.../GIT Repos/.../nfse-sp-api.jar" --server.port=4567
```

O repositório está em `.../GIT Repos/...`. Sem as aspas o systemd corta o
argumento em `GIT` e o bash recebe um caminho que não existe — a unit entra em
crash-loop com `status=127`, sem mensagem útil.

Já aconteceu duas vezes: em 26/09 01:10, nos três serviços, e às 15:03 de hoje,
quando escrevi a unit do watchdog sem as aspas. A segunda só apareceu porque
confirmei o log; o `NRestarts` estava subindo e eu não tinha olhado.

## Verificação

```bash
# a API está de pé e falando o contrato
curl -s http://127.0.0.1:4567/api/nfse-sp/status
```

Responde em **2,5 ms** e mostra:

```json
{"sucesso":true,"certificado_configurado":true,"senha_configurada":true,
 "leiaute_xsd":1,"cnpj_remetente":"00000000000191", ...}
```

`certificado_configurado` e `senha_configurada` são os que interessam: sem
eles a nota não sai, e sem o certificado o serviço sobe parecendo saudável.

### O `/status` é a única checagem válida

**Não use `consulta-cnpj` para verificar se o serviço está de pé.** Ele vai na
prefeitura por SOAP:

```java
// NfseSpService
String resposta = client.enviarPara(builder.enveloparConsultaCnpj(assinado),
                                    NfseSpOperacoes.CONSULTA_CNPJ);
```

Medido: `consulta-cnpj` leva **1,27 s** porque sai para a rede; `/status` leva
**2,5 ms** porque não sai.

A consequência de usar o errado foi concreta: entre 16:16 e 16:28 de
26/09/2026 um watchdog baseado em `consulta-cnpj` parou a API Java **três
vezes** enquanto o ERP emitia por ela sem problema. E o desenho era pior ainda
— se a prefeitura caísse, o watchdog derrubaria a única implementação que
existe. A falha da dependência externa removia a única defesa.

> Regra: **health check não pode ter modo de falha que dispara quando outra
> coisa está quebrada.** Só o `/status` respeita isso.

## Configuração

Fora do repositório, em `/etc/brasil-saas/nfse-sp.env`, permissão `600`:

| variável | o quê |
|---|---|
| `NFSE_SP_ENABLED` | liga e desliga a emissão |
| `NFSE_SP_CNPJ` | CNPJ remetente |
| `NFSE_SP_XSD_VERSION` | versão do leiaute |
| `NFSE_SP_CERT_PATH` | caminho do `.pfx` |
| `NFSE_SP_CERT_PASS` | senha do certificado |

O certificado fica em `OneDrive/Nova pasta/Documentos/certificado/`, com
permissão `600`. **A senha não vai para o repositório** — está só no env, e num
`.md` versionado ela vaza no primeiro `push`.

## Contrato

Uma resposta, sempre com as mesmas oito chaves no primeiro nível:
`sucesso`, `chave_nfse`, `numero_nfse`, `codigo_verificacao`,
`chave_nota_nacional`, `alertas`, `erro`, `xml_assinado`. Definido em
`RespostaNfsePadrao.java` e montado por `contrato_nfse`.

Ver `docs/pesquisa/contrato-nfse-unico.md`.

## Quando a API cair

Não há troca automática, por decisão: um watchdog que dá `systemctl stop` em
serviço de produção derruba emissão que estava funcionando, e foi o que fez.

O caminho hoje:

1. A emissão falha com erro de conexão, e o ERP mostra o que houve
2. O motivo fica gravado em `bc_fis_nfse_retorno`, com o JSON bruto no Mongo —
   sobrevive ao fechamento da tela
3. Se a prefeitura tiver saido e o ERP não souber, a resposta vai para
   `/api/fiscal/nfse/retornos/para-conferir`, e a pessoa confere antes de
   reemitir

Religar o Ruby, se for a decisão:

```bash
sudo systemctl enable --now nfse-sp-bridge   # 4569
```

Só que aí volta a haver duas implementações, e o contrato único precisa ser
respeitado pelas duas — é o que produziu as notas 29 e 30 órfãs quando não foi.

## Notas

Todas as notas que existem na prefeitura estão canceladas. A única linha
`EMITIDA` no banco é a `id=4`, com número `999`: registro do falso positivo de
teste, que nunca chegou à prefeitura e por isso não tem código de verificação.
