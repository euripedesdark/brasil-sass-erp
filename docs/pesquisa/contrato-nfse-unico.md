---
id: 2026-09-26-contrato-nfse-unico
status: confirmado
data: 2026-09-26
---

# O contrato único da NFS-e de São Paulo

As duas implementações do web service falam o mesmo idioma. Antes não, e a
diferença custou duas notas órfãs.

## O problema

| | formato |
|---|---|
| API Java, 4568 | `{"sucesso":true,"numero_nfse":"26","codigo_verificacao":"CHGEIPFB", ...}` |
| bridge Ruby, 4569 | `{"error":{"success":true,"numero_nfse":"29","codigo_verificacao":"VFGXSERH", ...}}` |

Duas divergências, ambas suficientes para quebrar:

1. **O Ruby embrulha tudo em `error`.** O ERP lia o primeiro nível, não achava
   nada, e entendia como recusa.
2. **O Ruby escreve o sucesso em inglês**, `success` e não `sucesso`.

O ERP lia só o formato da API Java. Com o fallback ligado, a prefeitura
**emitia a nota** e o ERP respondia *"A prefeitura não confirmou a emissão"*,
marcava `FALHA_EMISSAO` e **não guardava** número, código de verificação nem
chave — que são exatamente o que permite cancelar.

Aconteceu com as notas **29** e **30**, em 26/09/2026. As duas existem na
prefeitura. Já foram canceladas.

O código ainda dizia à pessoa *"Nenhuma nota foi emitida — pode tentar de novo"*.
Ela reemitia, e a prefeitura criava a segunda. Duas notas válidas para o mesmo
serviço, descobertas quando o cliente apontasse.

## Por que não bastava consertar o leitor

A primeira versão deste conserto fazia o ERP aceitar os dois formatos. Está
errado, e o motivo é o que importa:

> Leitor que conhece dois formatos é o mesmo bug com o acoplamento escondido.

O problema nunca foi o ERP não conhecer o formato do Ruby. É haver **dois
formatos**. Acionar o ERP para os dois mantém os dois, e o próximo emissor — ou
um campo novo no Ruby — volta a quebrar o ERP sem ninguém ver. Consertei a
leitura primeiro e o usuário apontou isso antes de eu testar.

O contrato tem que ser **do emissor**, e o leitor passa a ser o contrato.

## O contrato

Uma classe: `RespostaNfsePadrao.java`, na API Java. E o **mesmo método com o
mesmo nome** nas duas implementações:

```
contrato_nfse( sucesso, inscricao, numero, verificacao, chave_nacional, alertas, xml, erro )
```

Em Java, `NfseSpController.contrato_nfse(...)`. Em Ruby,
`contrato_nfse(sucesso:, inscricao:, ...)` em `nfse-sp-bridge/app.rb`.

Mesmo nome e mesma assinatura de propósito. Duas implementações do mesmo
contrato com dois métodos diferentes é a mesma armadilha das notas 29 e 30: um
lado muda o nome do campo e o outro não percebe, porque ninguém compara os
dois. Com o nome igual, revisar o contrato é uma **diff**.

### Chaves

Oito, sempre, no primeiro nível:

```json
{
  "sucesso": true,
  "chave_nfse": "2130033",
  "numero_nfse": "26",
  "codigo_verificacao": "CHGEIPFB",
  "chave_nota_nacional": "35503081200000000000191000000000002626097518490221",
  "alertas": [],
  "erro": "",
  "xml_assinado": "<PedidoEnvioRPS ...>"
}
```

### As cinco regras

1. **O sucesso é `sucesso`**, em português, e é a única forma de dizer que saiu.
   Nenhuma outra chave significa sucesso.
2. **Nada é embrulhado.** O objeto devolvido é o contrato, no primeiro nível.
   Sem `error`, sem `data`, sem `result`.
3. **Recusa vem com `sucesso: false`** e `erro` preenchido com texto. O status
   HTTP pode ser 4xx, mas o ERP **não olha o status** para decidir: olha
   `sucesso`.
4. **Campo ausente é string vazia**, nunca `null` e nunca omitido. O ERP lê
   direto sem checar, e um campo novo não vira `NullPointerException`.
5. **Só existe "não deu para saber" com HTTP 5xx ou corpo ilegível.** Resposta
   bem formada com `sucesso: false` é recusa, e recusa significa que nada foi
   emitido — é seguro reemitir. É essa distinção que impede duplicidade.

### `chave_nfse` não é chave

É a inscrição municipal. O nome vem da prefeitura e não foi mudado porque
mudar o nome do outro lado da prefeitura é pedir para quebrar. O
identificador que localiza a nota em qualquer ambiente é
`chave_nota_nacional`.

## O leitor no ERP: três estados

`RespostaNfse.java`, no serviço de emissão. E o que importa nele:

```java
Map<String, Object> resposta = RespostaNfse.ler(bruto);
Boolean confirmado = RespostaNfse.confirmado(resposta);
```

`confirmado` tem **três** valores:

| valor | significado | ação |
|---|---|---|
| `true` | a prefeitura confirmou | grava e segue |
| `false` | a prefeitura recusou | grava a recusa, **reemitir é seguro** |
| `null` | **não deu para saber** | manda conferir, **reemitir NÃO é seguro** |

O terceiro estado é a correção de raiz. Antes, resposta não reconhecida virava
`false`, e o ERP dizia "não emitiu" — mentira que induz à duplicidade.

O texto que o ERP passa ao usuário quando não sabe:

> A prefeitura respondeu em formato desconhecido, então não dá para afirmar se
> a nota foi emitida. O que veio: numero=? codigo_verificacao=? chave=?
> **CONFIRA NA PREFEITURA antes de emitir de novo** — emitir duas vezes cria
> nota duplicada.

Quando há identificadores na resposta, eles entram nessa mensagem, que é o
material para a pessoa cancelar.

### `ler` recusa formato desconhecido de propósito

```java
if (bruto.get("error") instanceof Map) {
    return null;   // embrulho é de outro contrato: recusa
}
if (!bruto.containsKey("sucesso")) {
    return null;
}
```

Aceitar o embrulho aqui seria voltar a dois formatos. Não aceito: o serviço
recusa com "formato desconhecido" em vez de emitir errado. **Falhar alto é
melhor que adivinhar.**

## Testado

| teste | resultado |
|---|---|
| as duas `/status` lado a lado | mesmas 8 chaves, `sucesso` nas duas |
| emissão com **só o Ruby de pé** | nota **31** `EMITIDA`, número, verificação, chave, XML arquivado |
| cancelamento pelo fallback | `CANCELADA` |
| nota 29 (`VFGXSERH`) | cancelada na prefeitura |
| nota 30 (`3JVV8HKP`) | cancelada na prefeitura |

As 5 `FALHA_EMISSAO` restantes não têm número, então a prefeitura não emitiu
nenhuma delas. Nenhuma nota órfã sobrando.

## O que NÃO está commitado

Tudo. `installbase.sh`, `docs/infra/RESTAURAR.md`, os documentos, o `Nfse.jsx`, e nesta
rodada:

| arquivo | o quê |
|---|---|
| `RespostaNfse.java` | leitor, três estados, no ERP |
| `RespostaNfsePadrao.java` | o contrato, na API Java |
| `NfseSpController.java` | as 4 rotas pelo `contrato_nfse` |
| `app.rb` | as 4 rotas pelo `contrato_nfse` |
| `nfse-watchdog/` | o watchdog e a unit |

## Onde o motivo da recusa fica gravado

O retorno de cada chamada fica em `bc_fis_nfse_retorno`, com o JSON bruto no
MongoDB. Sem esse registro, a recusa vivia no toast e no log do servidor — e as
notas 29 e 30 são a prova do que isso custa: existem na prefeitura, sem número
e sem código de verificação no ERP, e o motivo do erro real era uma linha de
log.

Ver `docs/pesquisa/o-que-sobrevive-a-tela-de-erro.md`.

## Dois erros meus, ambos do compilador

1. Escrevi sintaxe de keyword do Ruby em Java:
   `contrato_nfse(sucesso=true, inscricao=...)` — não existe em Java.
2. Substituí um método e deixei seis linhas órfãs do anterior.

Nenhum dos dois apareceu em teste, apareceram em compilação. A segunda só
apareceu porque li o arquivo depois de editar.

## O que continua aberto

- **O watchdog** cobre a detecção, mas não o contrato: se o Ruby voltar a falar
  outro idioma, ele detecta e troca, e o problema reaparece. O contrato é a
  correção; o watchdog é a rede de proteção.
- **Sem teste automatizado do contrato** comparando as duas implementações.
  Existe um teste do failover (`testar_failover.rb`) e um do CNPJ
  (`CnpjAlfanumericoTest`), mas nada que force as duas APIs a falar igual. A
  regressão das notas 29 e 30 foi encontro de linguagem, e ela volta a existir
  no próximo commit.
- **Notas 29 e 30** ficaram sem número no ERP para sempre. Estão canceladas na
  prefeitura, mas o registro fiscal local mostra `FALHA_EMISSAO` sem
  identificadores. Corrigir o histórico é decisão de quem opera, e involves
  saber que o número existiu.
