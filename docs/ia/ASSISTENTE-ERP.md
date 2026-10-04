> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

---
id: 2026-09-29-assistente-erp
status: backend pronto
data: 2026-09-29
---

# ASSISTENTE ERP

> Um especialista que responde com o ERP, e não com o modelo.
> A tela é "Assistente ERP", não "Chat" — o que responde não é o modelo.

---

## 1. A ORDEM DAS FONTES É O DESENHO

```
pergunta
  ↓
busca fiscal        ← obrigatório para classificação
  ↓
dados do ERP        ← o produto, com o NCM que está gravado
  ↓
documentação interna
  ↓
montagem do contexto
  ↓
OpenRouter          ← por último
  ↓
resposta explicada
```

**O modelo é a última fonte.** A consequência é que a resposta sobrevive ao modelo
estar fora, e que o modelo não tem como errar o código.

Isso não é teoria. Medido nesta base: um modelo gratuito do OpenRouter
respondeu *"o NCM de cerveja é 2203"* quando o código é `22030000`. Com a busca
fiscal antes, a resposta sai com o código certo e o motivo da correspondência.

## 2. VERIFICADO NO ERP NO AR

```
Pergunta: "Qual NCM devo usar para cerveja?"
status: ok | fontes: fiscal,documentacao | 2926 ms

  Depende do tipo:
  - Cerveja de malte → 22030000
  - Cerveja sem álcool → 22029100
  - Borras/desperdícios → 23033000
  Se o produto for cerveja comum de malte, use 22030000.

  ---
  • 22030000 — Cervejas de malte.
    22030000 | Cervejas de malte.
    motivo: palavra-chave: cerveja
    origem: vocabulário curado  (22030000)
```

O modelo escreveu a frase. O código, o motivo e a origem são do ERP.

**Diagnóstico de produto** (a pergunta que mais importa na operação):

```
Pergunta: "Por que o produto Detergente Concentrado não emite nota?"

  produto: Detergente Concentrado 5L
  detalhe: ncm=3402.20, cfop=5102, cest=(vazio)
  motivo:  NCM com 7 dígitos; a coluna é varchar(8) e o NCM oficial tem 8
  origem:  cadastro de produto | produto 10296
```

O diagnóstico é determinístico e diz **qual** dos três problemas é o caso:
sem classificação, formato errado, ou código que não existe. Cada um pede uma
correção diferente, e dizer qual deles é o caso é metade da resposta.

---

## 3. A RESPOSTA TRAZ O MOTIVO, E ISSO É OBRIGATÓRIO

```json
{
  "tabela": "ncm", "codigo": "22030000", "descricao": "Cervejas de malte.",
  "pontuacao": 90, "motivo": "palavra-chave: cerveja"
}
```

A diferença entre `22030000` e `22030000, motivo: palavra-chave cerveja, origem:
vocabulário curado` é a diferença entre um palpite e uma resposta em que a
pessoa confia. Toda fonte devolve motivo e origem, mesmo quando só tem um.

---

## 4. O MODELO NÃO ESTÁ PRESO EM LUGAR NENHUM

| Onde | O quê |
|---|---|
| `.env` → `SPRING_AI_OPENAI_CHAT_MODEL` | o modelo |
| `bc_ia_config.default_model` | modelo por empresa |
| `AssistenteService.modeloDe()` | lê da empresa, cai no padrão |

Trocar de modelo é um `UPDATE`. Nenhuma linha de código muda.

**Modelo medido.** Do free tier do OpenRouter, 20 modelos têm `prompt=0` e
`completion=0`. Muitos devolvem 429, e o `openrouter/free` cai num que devolve
`content=null` com o texto em `reasoning`. O que respondeu conteúdo de verdade foi
o `inclusionai/ling-3.0-flash-sante:free`.

---

## 5. AUDITORIA

`bc_ia_assistente_auditoria` guarda pergunta, contexto em `jsonb`, fontes, modelo,
resposta do modelo **separada** da resposta final, status, erro, duração e tokens.

O campo `resposta_modelo` é separado de propósito: a resposta final pode ser
montada sem o modelo, e nesse caso ele vem vazio — o que é informação por si.

| | |
|---|---|
| Perguntas | 14 |
| Com modelo | 2 |
| Sem modelo | 12 |
| Média | 868 ms |
| Com modelo | 4.852 ms |

As 12 sem modelo são o **custo de não acoplar**: enquanto eu consertava a
integração, a resposta do ERP saía normalmente, com código e motivo. Nenhuma
dessas 12 pessoas ficou sem resposta.

---

## 6. A INTEGRAÇÃO COM O OPENROUTER NUNCA FOI EXERCITADA

Três defeitos, todos invisíveis porque `chat.enabled` estava `false`:

| # | Defeito | Sintoma | Causa |
|---|---|---|---|
| 1 | **`httpcore5` desatualizado** | `NoClassDefFoundError: org/apache/hc/core5/io/IOFunction` | O pom fixava core 5.3.4 com client 5.6.4. O par que o próprio client declara é **5.4.3** — `IOFunction` só existe a partir do core 5.4 |
| 2 | **`brotli4j` com nativo não extraível** | `UnsatisfiedLinkError: DecoderJNI.nativeCreate` | Vem do **openpdf**, não do httpclient5. Em fat jar do Spring Boot o brotli4j não consegue extrair o `.so` de dentro do jar aninhado. Excluído: o brotli não é usado nem para PDF nem para o OpenRouter, que aceita gzip |
| 3 | **base-url com `/v1` a mais** | `404 Not Found` | O Spring AI 1.0.0-M3 usa `https://api.openai.com` como padrão e acrescenta `/v1/chat/completions` sozinho. Com `/v1` no fim, a chamada saía como `/api/v1/v1/chat/completions` |

Nenhum dos três aparecia em teste, porque a integração nunca havia sido
chamada. O primeiro sintoma real só apareceu quando o modelo foi ligado de
verdade.

**O `application.yml` é do `root` e não foi tocado.** Toda a configuração fica no
`.env`, que tem precedência sobre o yml no Spring Boot:

```bash
SPRING_AI_OPENAI_API_KEY=...      # fora do git: .env está no .gitignore
SPRING_AI_OPENAI_BASE_URL=https://openrouter.ai/api
SPRING_AI_OPENAI_CHAT_ENABLED=true
SPRING_AI_OPENAI_CHAT_MODEL=inclusionai/ling-3.0-flash-sante:free
```

---

## 7. COLISÃO DE MIGRATION COM OUTRA IA

Durante o trabalho, outra IA criou `V108__auth_source.sql`. Como o meu V108
**já estava aplicado no banco**, o Flyway recusou subir com
`Found more than one migration with version 108` — e derrubou o ERP num loop de
restart.

A outra IA renumerou para `V111`, e o ERP voltou. Vale registrar porque o número
de migration é espaço global: duas IAs no mesmo repositório precisam combinar o
próximo número, e o jeito é o mesmo do `ia-tarefa` — quem trabalha, declara.

---

## 8. OS ARQUIVOS

| Arquivo | Papel |
|---|---|
| `ia/assistente/AssistenteService.java` | orquestra: contexto, modelo, auditoria |
| `ia/assistente/ContextoErpService.java` | as três fontes, na ordem |
| `ia/assistente/TermoDeBusca.java` | extrai os termos da pergunta |
| `ia/assistente/BuscaDocumentacao.java` | busca em `docs/` |
| `ia/assistente/Achado.java` | um achado, com motivo e origem |
| `ia/assistente/AssistenteAuditoria.java` + repository | a auditoria |
| `ia/controller/AssistenteController.java` | `POST /api/ia/assistente` e a auditoria |
| `V110__assistente_erp_auditoria.sql` | tabela de auditoria e modelo default |

Endpoint: `POST /api/ia/assistente` com `{"pergunta": "..."}`, permissão `IA_READ`
— a mesma dos outros controllers de IA, porque o módulo inteiro usa essa e não
existe permissão `ia:*` no cadastro.

## 9. O QUE FALTA

**A tela.** O endpoint está pronto e testado, e é o que produto, serviço e
tributação vão consumir. Falta o componente "Assistente ERP", que precisa mostrar
o código, o motivo e a origem como campos, e não dentro de um texto.

## 10. COMO VERIFICAR

```bash
T=$(curl -s -X POST http://127.0.0.1:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"username":"euripedes","password":"ALTERE_ME"}' | grep -oE '"accessToken":"[^"]+"' | cut -d'"' -f4)

curl -s -X POST http://127.0.0.1:8080/api/ia/assistente -H "Authorization: Bearer $T" \
     -H 'X-Empresa-Id: 1' -H 'Content-Type: application/json' \
     -d '{"pergunta":"Qual NCM devo usar para cerveja?"}'

sudo -u postgres psql -d brasil-saas -c \
  "select status, modelo, duracao_ms from brasil_saas.bc_ia_assistente_auditoria order by id desc limit 5;"
```

