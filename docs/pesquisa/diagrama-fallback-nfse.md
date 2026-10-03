# Fallback da NFS-e de São Paulo

Diagrama de como a falha é detectada e resolvida. Duas camadas, porque elas
pegam defeitos diferentes.

> **Atualizado em 26/09/2026:** o caso "que só o watchdog pega" **foi corrigido
> pela raiz**, e não pelo watchdog. As duas implementações passaram a falar o
> mesmo contrato, em `contrato_nfse`. Ver `docs/pesquisa/contrato-nfse-unico.md`. O diagrama
> abaixo mostra o problema como ele era, e o que ele virou.

## As duas camadas, e por que duas

```
   ┌─────────────────────────────────────────────────────────────────┐
   │  ERP — NfseEmissaoService                                     │
   │  POST /api/fiscal/nfse/emitir                                  │
   └──────────────────────────────┬──────────────────────────────────┘
                                  │ HTTP (RestClient, chunked)
                                  ▼
   ┌─────────────────────────────────────────────────────────────────┐
   │  nginx — porta 4567                                            │
   │  upstream brasil-saas_nfse { 4568; 4569; }                      │
   │  proxy_next_upstream error timeout http_502/503/504             │
   └──────────────────────────────┬──────────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    ▼                           ▼
        ┌───────────────────┐       ┌───────────────────┐
        │ nfse-sp-api  4568 │       │ nfse-sp-bridge 4569│
        │ Java — primária   │       │ Ruby — fallback   │
        └─────────┬─────────┘       └─────────┬─────────┘
                  │                           │
                  └─────────────┬─────────────┘
                                ▼
              ┌──────────────────────────────┐
              │ nfse-watchdog.service        │
              │ checa o CONTRATO, não a porta│
              └──────────────────────────────┘
```

**nginx** troca quando a conexão falha: porta fechada, timeout, 502/503/504.
Resposta em menos de 1 segundo.

**watchdog** troca quando a conexão funciona mas a implementação não serve:
responde 200 com o contrato errado. O nginx não pega isso, porque 200 é sucesso
para ele.

## O caso que só o watchdog pega

É o que aconteceu com a nota 29.

```
  API Java cai
        │
        ▼
  nginx troca para o Ruby ──── resposta 200 ────►  ERP
                                                    │
                        o Ruby devolve              │
                        { "error": {                │
                            "success": true,       │
                            "numero_nfse": "29"    │
                        } }                        │
                                                    │
                        o ERP lê "sucesso"  ───────┤ não acha: está dentro de "error"
                                                    │
                                                    ▼
                                        HTTP 422 "não confirmou"
                                        a NOTA 29 EXISTE
                                        o ERP marcou FALHA_EMISSAO
                                                    │
                                                    ▼
                                 a pessoa reemite ──► DUPLICATA
```

O nginx viu sucesso em todos os saltos. O que quebrou foi o ERP ler.

## O que o watchdog checa

Não é se a porta está aberta. É se a implementação **responde o que o ERP
precisa**. A pergunta é feita por `consulta-cnpj`, que não emite nada e não
consome número:

```
  POST /api/nfse-sp/consulta-cnpj  { "cnpj": "00000000000191" }
        │
        ├── resposta tem "inscricao_municipal"?
        │
        └── tem "sucesso" ou "success" valendo true?
                    │
        ┌───────────┴───────────┐
        ▼ sim                   ▼ nao
   implementação             implementação
      saudável               fora do contrato
```

Os dois nomes são aceitos porque as implementações discordam nisso, e é
justamente o que a nota 29 expôs:

| implementação | campo do sucesso |
|---|---|
| `nfse-sp-api` (Java) | `sucesso` |
| `nfse-sp-bridge` (Ruby) | `success` |

Aceitar os dois é o que permite ao watchdog ser justo com as duas em vez de
privilegiar uma só porque foi escrita primeiro.

## Os dois caminhos do watchdog

O watchdog distingue se a primária está **no ar quebrada** ou **fora do ar**,
porque a ação é diferente:

```
        primária não passa na checagem
                    │
        ┌───────────┴────────────┐
        │                        │
   NÃO está no ar           ESTÁ no ar
   (serviço parado)         (responde 200, contrato errado)
        │                        │
        ▼                        ▼
   RECUPERAR                 TROCAR
   systemctl start           systemctl stop
   esperar ficar saudável   |     |
   │                        │    |
   ▼                        │    ▼
 recuperou?                │  subir o fallback
   │ sim ──► normal         │  esperar ficar saudável  ◄── o sleep
   │ não                    │    │
   ▼                        │    ▼
 ISOLAR (quarentena)  ◄─────┘  fallback saudável
   │
   ▼
 subir o fallback + esperar
```

**Isolar** é o que impede restart storm. A primária quebrada fica em quarentena
por 5 minutos antes de ser tentada de novo, senão o watchdog alterna entre as
duas indefinidamente — que é o que o primeiro teste mostrou, com "Trocando" a
cada 30 segundos.

## O sleep depois de subir

O `esperar_saudavel` não é opcional. Sem ele, o primeiro RPS depois da troca cai
enquanto a JVM ainda está subindo, e o resultado é timeout em vez de nota
emitida. Medido: a JVM da API Java leva cerca de 6 segundos para ficar
saudável.

```
   para a primária
        │
        ▼
   subir o fallback
        │
        ▼
   ┌─── espera_saudavel (até 90s) ───┐
   │  a cada 5s: a resposta tem o     │
   │  contrato certo?                  │
   └──┬──────────────────────────┬────┘
      │ sim                      │ esgotou o tempo
      ▼                          ▼
  "FALLBACK saudavel"      "FALLBACK NAO ficou
                             saudavel em 90s"
```

O segundo desfecho importa tanto quanto o primeiro: quando **nenhuma**
implementação está saudável, o log diz isso. Foi o que a nota 29 custou —
ninguém conseguia dizer qual das duas estava respondendo.

## Linha do tempo de uma troca

Tempos medidos em 26/09/2026, com `INTERVALO=15s` e `ESPERA_PORTA=5s`:

```
   t+0s     a API Java para
   t+0s     nginx já atende pelo Ruby        <- camada 1, sub-segundo
   t+15s    watchdog nota a falha
   t+30s    watchdog age: para a primária
   t+30s    watchdog sobe o fallback
   t+30s+   espera_saudavel: Ruby responde o contrato certo
   t+31s    "FALLBACK saudavel apos a troca"  ← camada 2, registrada
```

O nginx já atendeu em `t+0s`. O watchdog não substitui o nginx: ele registra o
que aconteceu, garante que a primária volte sozinha, e cobre o caso de
contrato errado que o nginx não vê.

## Recuperação automática

O nginx **não** traz a primária de volta — ele é passivo, só troca para frente.
O watchdog traz, porque em cada ciclo pergunta se a primária voltou a passar na
checagem:

```
   primária em quarentena
        │
        ▼
   fim da quarentena (5 min)
        │
        ▼
   systemctl start primária
        │
        ▼
   esperar_saudavel
        │
   ┌────┴────┐
   ▼ sim      ▼ não
 voltou     continua isolado
   │
   ▼
 nginx volta a usar a primária no próximo RPS
```

Medido: com a primária parada e depois religada, o watchdog a recuperou em
6 segundos e registrou `PRIMARIA recuperada`, sem troca de implementação.

## Estados que o log registra

O log é a única forma de saber o que aconteceu, e ele existe porque o crash-loop
de 8,6 horas foi invisível. Cada linha tem o motivo:

| mensagem | significa |
|---|---|
| `PRIMARIA esta fora do ar. Tentando recuperar.` | serviço parado, vai tentar subir |
| `PRIMARIA recuperada.` | voltou, sem troca |
| `PRIMARIA no ar e nao responde. Trocando.` | 200 com contrato errado |
| `FALLBACK saudavel apos a troca.` | o Ruby passou na checagem |
| `FALLBACK NAO ficou saudavel em 90s.` | nenhuma implementação serve |
| `FALLBACK tambem nao respondeu (2x).` | as duas estão fora |

`/var/log/brasil_saas/nfse-watchdog.log`.

## O que ainda não está resolvido

O watchdog **detectava** o contrato errado e trocava de implementação. Ele não
**corrigia** o ERP ler — e essa era a pendência registrada aqui.

**Corrigido em 26/09/2026, pela raiz.** Em vez de ensinar o ERP a aceitar os dois
formatos, as duas implementações passaram a falar o mesmo contrato, em
`contrato_nfse`, e o ERP lê só esse. O terceiro estado do `confirmado` — o "não
deu para saber" — continua, e é ele que impede a duplicidade se algum dia um
emissor voltar a divergir.

```
   antes                                      depois
   ────────────────────────────────────       ─────────────────────────────
   Java:  {sucesso: ...}                      Java:  {sucesso: ...}
   Ruby:  {error:{success: ...}}               Ruby:  {sucesso: ...}
                    │                                    │
                    ▼                                    ▼
   ERP le "sucesso", nao acha               ERP le "sucesso", acha
   ↓                                       ↓
   diz "nao emitiu" e manda reemitir       grava e segue
   ↓                                       ↓
   DUPLICATA                              nada
```

O watchdog continua como rede de proteção, mas o contrato é a correção. Ver
`docs/pesquisa/contrato-nfse-unico.md`.

## O que ainda está aberto

**Sem teste que compare as duas implementações.** A regressão das notas 29 e 30
foi um encontro de linguagem, e ela volta a existir no próximo commit que mexer
em uma das duas sem mexer na outra. Existe `testar_failover.rb` e existe
`CnpjAlfanumericoTest`, mas nada que force as duas APIs a falar igual.
