> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Estratégia de cobertura de endpoints — 2026-10-02

## Problema
O backend possui capacidade que pode ficar invisível para o usuário quando existe endpoint sem uma tela ou ação de negócio correspondente.
O erro não deve ser resolvido criando uma tela genérica para cada URL.

## Regra de classificação
Todo endpoint deve cair em uma destas categorias:

### A. Funcionalidade de negócio
Deve possuir tela própria ou ação dentro do fluxo de negócio correspondente.
Exemplos: cadastrar cliente, receber compra, reservar estoque, processar folha.

### B. Operação técnica/interna
Não precisa de tela de usuário.
Exemplos: callbacks, autenticação, health/metadata, endpoints usados exclusivamente por integrações e operações internas de infraestrutura.

### C. Integração/Documento fiscal
A interface deve existir no módulo funcional, mas a operação técnica pode continuar encapsulada no service.
Exemplo: autorização fiscal é uma ação da tela do documento, não uma tela chamada por verbo HTTP.

### D. Endpoint ainda sem UX
Entra no backlog de cobertura.
A implementação deve ser feita no módulo que possui o contexto de negócio, não em um CRUD universal.

## Ferramenta criada
Foi criada a rota administrativa `/admin/endpoints`.

Componente: `src/main/resources/static/react/src/components/admin/EndpointCoverage.jsx`.

Ela consulta o OpenAPI real em `/v3/api-docs` e apresenta método HTTP, caminho, módulo/tag, operação, vínculo de tela conhecido, backlog de endpoints ainda sem vínculo, filtro por método, filtro por cobertura, busca e acesso ao Swagger.

## Por que isso é melhor que uma tela genérica
Uma tela universal de API permitiria ao usuário montar POST/PUT/DELETE sem entender o processo de negócio. Isso pioraria a UX e abriria espaço para operações fora de ordem.

O catálogo é somente o mecanismo de **controle de cobertura**.

A solução definitiva é:
`Endpoint → Service → fluxo de negócio → tela/ação → auditoria`

## Próximo ciclo
1. separar endpoints técnicos dos funcionais;
2. para cada endpoint funcional sem vínculo, localizar o processo correto;
3. reaproveitar service/controller existentes;
4. criar a tela ou ação no fluxo existente;
5. adicionar o vínculo no catálogo;
6. executar build/smoke test;
7. atualizar o mapa de cobertura.

Assim o número de endpoints sem tela deixa de ser uma lista estática em Markdown e passa a ser uma verificação baseada no contrato OpenAPI publicado pelo próprio sistema.