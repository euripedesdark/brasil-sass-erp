---
id: 2026-09-26-o-que-sobrevive-a-tela-de-erro
status: confirmado
data: 2026-09-26
---

# O que sobrevive ao fechamento da tela de erro

A recusa da prefeitura vivia no toast e no log do servidor. Fechar a tela e
rotacionar o log significava perder a causa e recomeçar a correção do zero.

Este registro é o que responde "o que a prefeitura respondeu?" depois que a
tela já foi fechada.

## O buraco

`marcarFalha` gravava **só o status**:

```java
private void marcarFalha(Long nfseId, String motivo) {
    nfseRepository.findById(nfseId).ifPresent(n -> {
        n.setStatus("FALHA_EMISSAO");
        nfseRepository.save(n);
    });
}
```

O `motivo` ia para o `log.error` e sumia. O registro fiscal ficava
`FALHA_EMISSAO` com nenhum motivo — o pior estado possível: sabe-se que falhou,
não sabe-se por quê.

E não havia como recuperar depois. A prefeitura de São Paulo **não tem
consulta por chave** para este caso.

### O que isso custou

Em 26/09/2026, com o fallback ligado, o bridge Ruby devolveu a resposta
embrulhada em `error` e o ERP leu `sucesso` no nível de cima — não achou, e
respondeu *"A prefeitura não confirmou"*. As notas **29** e **30** foram
emitidas de verdade.

O motivo do erro real — a divergência de contrato entre as duas implementações —
**era uma linha de log**. Sem número e sem código de verificação gravados,
cancelar as duas exigia ir na prefeitura de mão. Foi o que aconteceu.

## Onde fica

| parte | onde | por quê |
|---|---|---|
| `bc_fis_nfse_retorno` | Postgres | o que se consulta: operação, sucesso, status, código, mensagem, quando |
| corpo JSON bruto | MongoDB | o campo como veio, inclusive o que o ERP ainda não sabe ler |

O Postgres guarda o ponteiro (`documento_id`), o Mongo guarda o corpo. Mesmo
arranjo do XML e do PDF.

### Uma linha por chamada, não uma coluna na nota

Uma nota conversa com a prefeitura mais de uma vez: emissão e, depois,
cancelamento. Guardar o cancelamento no mesmo lugar da emissão apagaria a
emissão — que é justamente o registro que serve de prova.

Por isso `tipoEntidade` é `nfse_retorno` e `entidadeId` é o **id do próprio
registro de retorno**, que é único: o `GenericoDocumentoService` (que apaga o
anterior do mesmo `empresa/tipo/entidade`) nunca tem o que apagar.

## `sucesso` é triestado

| valor | significa | reemitir |
|---|---|---|
| `true` | a prefeitura aceitou | — |
| `false` | recusou, com mensagem | **seguro** — nada saiu |
| `null` | **não deu para saber** | **perigoso** |

A prefeitura recusa com HTTP 422 e um corpo. Houve resposta, e a resposta foi
não — isso é `false`. Falta de transporte, ou resposta fora do contrato, não
tem como saber: `null`.

Tratar o terceiro como `false` é o que produz duplicidade. Uma tela que
mostrasse "não emitiu" nesse caso mandaria a pessoa reemitir, e a prefeitura
criaria a segunda nota.

Há índice parcial para os dois lados:

```sql
WHERE sucesso IS FALSE   -- "o que a prefeitura recusou"
WHERE sucesso IS NULL    -- "o que precisa de conferência"
```

## Endereços

```
GET /api/fiscal/nfse/{id}/retornos                  a conversa de uma nota
GET /api/fiscal/nfse/{id}/retornos/{retornoId}/bruto   o JSON como veio
GET /api/fiscal/nfse/retornos/recusas               as recusas da empresa
GET /api/fiscal/nfse/retornos/para-conferir         os casos indecisos
```

Todos exigem `ADMIN` ou `SUPERUSER` e filtram por empresa do token. Um retorno
de outra empresa não é legível por id adivinhado.

## Testado com recusa real

Emitido com `codigoServico: "ZZZ999"`, que a prefeitura recusa. O que a
pessoa viu na tela:

```
A prefeitura recusou a emissao: "erro":"A prefeitura recusou o RPS:
[1001] XML não compatível com Schema.The 'CodigoServico' element is invalid
- The value 'ZZZ999' is invalid according to its datatype
'http://www.prefeitura.sp.gov.br/nfe/tipos:tpCodigoServico' - The Pattern
constraint failed." Nenhuma nota foi emitida — pode tentar de novo..."
```

Depois, com a tela fechada, o mesmo motivo está na tabela:

```
id=1  nfse_id=13  operacao=EMISSAO  sucesso=f  http=422  codigo=1001
duracao=266ms  tem_json=t  created_at=2026-09-26 16:23:33
```

O código `1001` foi extraído da mensagem, que mede centenas de caracteres e muda
conforme o campo que falhou. O código não muda, e cabe numa coluna que dá para
filtrar.

## Onde a gravação acontece

**Antes** de qualquer tradução para exceção, em todos os caminhos: emissão
aceita, recusada, formato desconhecido, erro de transporte, e o mesmo nos quatro
caminhos do cancelamento.

Se a gravação falhar, a emissão **não é desfeita**. A nota existe na
prefeitura; reportar erro levaria o usuário a repetir e criar duplicidade. A
ordem é: linha primeiro (para ter id), arquivo depois. Se o arquivo falhar,
sobra a linha com `documento_id` nulo — o que é honesto e consultável. O inverso,
ponteiro sem linha, seria um registro que promete um corpo e não tem.

`registrar` roda em `REQUIRES_NEW` porque a recusa precisa ser gravada mesmo
quando a operação está em rollback — e é no caminho da exceção que ela mais
importa.

## A brecha de tenant que apareceu no caminho

`NfseController` usava `findById(id)` sem filtro de empresa em quatro rotas:
`xml`, `pdf`, `cancelar` e as novas de retorno. Os ids são sequenciais e
previsíveis, então um ADMIN de uma empresa que adivinhasse o id da nota de
outra lia o retorno da prefeitura, baixava o XML e cancelava a nota.

Corrigido com `findByIdAndEmpresaIdAndDeletedAtIsNull`, e a empresa vem sempre
do token. Testado: token da empresa 1 contra nota da empresa 2 devolve **404**
nas quatro rotas.

404 e não 403 de propósito — a resposta não diz se a nota existe em outra
empresa.

## O que não está commitado

Tudo. `installbase.sh`, `docs/infra/RESTAURAR.md`, os documentos, o `Nfse.jsx`, e nesta
rodada:

| arquivo | o quê |
|---|---|
| `V98__nfse_retorno_prefeitura.sql` | a tabela, com o porquê de cada coluna |
| `NfseRetorno.java` | a entidade, com o triestado documentado |
| `NfseRetornoRepository.java` | as três consultas |
| `NfseRetornoService.java` | grava antes de lançar exceção; JSON no Mongo |
| `NfseEmissaoService.java` | liga a gravação; o serviço passa a ter 2 estados |
| `NfseController.java` | 4 endpoints + o tenant |
| `NfseRepository.java` | o finder por empresa |

## O que continua aberto

**A tela.** Os endpoints existem e os dados estão lá, mas não há aba no
`Nfse.jsx` que mostre a conversa. A pessoa ainda precisa chamar a API à mão.
Falta a parte que o usuário dita pelo usuario: dita pelo usuário: "como se fosse uma pessoa olhando pro erro
na tela antes de fechar a tela" — só o lado do servidor está feito.

**Teste automatizado do registro.** O comportamento existe e foi testado à mão,
com recusa real. Não há teste que garanta que a gravação acontece antes da
exceção, nem que o triestado sobrevive ao round-trip.
