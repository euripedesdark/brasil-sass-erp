> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Mapa de cobertura Frontend x Backend — estado atual

**Data:** 30/09/2026  
**Branch:** main  
**Objetivo:** impedir que uma tela chame endpoint inexistente e impedir que capacidade de backend fique invisível por erro de inventário.

## Estado verificado

| Item | Estado atual |
|---|---:|
| Controllers Java no source | **90** |
| Arquivos JS/JSX/TS/TSX no source React | **155** |
| Services React | **56** |
| Migrations Flyway | **108** |
| Baseline anterior de endpoints | **466** |
| Endpoints adicionados desde o baseline | **12 confirmados** |
| Baseline matemático após as novas telas/rotas | **478** |

Os 12 endpoints confirmados pertencem a dois controllers que já possuem frontend:

### Comissão — 7 endpoints

`/api/financeiro/comissoes`

- GET listagem
- GET resumo
- POST `/{id}/pagar`
- GET `/regras`
- POST `/regras`
- PUT `/regras/{id}`
- DELETE `/regras/{id}`

A tela `Comissoes.jsx` e `RegrasComissao.jsx` já consomem essa API.

### Caixa — 5 endpoints

`/api/financeiro/caixas`

- GET listagem
- GET `/{id}`
- POST
- PUT `/{id}`
- DELETE `/{id}`

`CaixaService.js` já usa esse contrato. O antigo caminho `/api/caixa` foi abandonado.

## Correção executada: tela Fiscal

A auditoria encontrou um problema mais grave que um endpoint "sem tela": `Fiscal.jsx` estava chamando rotas que não existem no contrato atual:

- `/api/fiscal/notas`
- `/api/fiscal/notas/{id}/cancelar`
- `/api/fiscal/notas/pdf/{id}`
- `/api/fiscal/nfe/emitir`

A correção feita nesta execução foi:

1. criado GET tenant-safe `/api/fiscal/nfse`;
2. `NfseRepository` recebeu consulta por empresa e `deleted_at IS NULL`;
3. `Fiscal.jsx` passou a listar NFS-e pelo endpoint real;
4. cancelamento passou para `/api/fiscal/nfse/{id}/cancelar`;
5. PDF passou para `/api/fiscal/nfse/{id}/pdf`;
6. a opção NF-e foi retirada da emissão desta tela, porque **não existe controller de NF-e e o NFeServiceImpl ainda devolve protocolo simulado**.

Isso evita que o usuário clique em "emitir NF-e" e receba 404 ou, pior, uma falsa confirmação.

## NF-e: não mascarar como funcionalidade pronta

O backend contém a interface `NFeService`, mas a implementação atual:

- depende da flag SEFAZ;
- exige certificado;
- ainda possui TODO para montagem/envio do XML;
- devolve `PROTOCOLO-SIMULADO-...`;
- não possui `NFeController`.

Portanto **não foi criado endpoint falso apenas para aumentar a cobertura**. A emissão real deve ser implementada antes de voltar para a UI.

## Situação dos "65 órfãos"

O número **65** pertence ao levantamento de 29/09. Ele não deve ser usado como estado atual sem nova execução do detector.

Só com as mudanças confirmadas nesta rodada:

- 7 endpoints de comissão deixaram de ser órfãos;
- 5 endpoints de caixa deixaram de ser órfãos;
- a tela fiscal deixou de chamar quatro contratos inexistentes;
- foi acrescentado um endpoint real de listagem NFS-e para completar o contrato da tela.

Assim, o baseline antigo de 65 já não pode ser tratado como a fila atual.

## Regra para os próximos levantamentos

Um endpoint só será classificado como "sem tela" quando:

1. o controller existir no source atual;
2. a rota HTTP for extraída do controller;
3. o frontend source atual não possuir chamada equivalente;
4. aliases/base URLs forem resolvidos;
5. endpoints deliberadamente internos, callbacks ou integração externa forem separados;
6. o inverso também for verificado: toda chamada do frontend deve encontrar um endpoint real e compatível.

**Não criar tela só porque existe endpoint. Não criar endpoint só porque existe tela.**

O contrato real é sempre:

`Tela → Service React → HTTP → Controller → Service Java → persistência/integração`

## Arquivos corrigidos nesta rodada

- `src/main/java/br/com/brasil_saas/fiscal/repository/NfseRepository.java`
- `src/main/java/br/com/brasil_saas/fiscal/controller/NfseController.java`
- `src/main/resources/static/react/src/components/Fiscal.jsx`
- `docs/auditorias/MAPA_COBERTURA_FRONTEND.md` marcado como histórico
- `docs/RELATORIO-ENDPOINTS-ORFAOS.md` marcado como baseline histórico

