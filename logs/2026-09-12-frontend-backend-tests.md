# Testes Frontend -> Backend

Data: 2026-09-12
Branch: appmod/java-upgrade-20260912215509
Runtime: Oracle JDK 25 / Maven 3.9.11 / Node.js 22.22.2

## Backend

- `mvn test -q`: PASS.
- `mvn clean verify -Djacoco.skip=false -q`: iniciado com o código atual; sem erro de compilacao/teste observado.
- Testes Java existentes: nenhum arquivo em `src/test` foi encontrado.

## Smoke HTTP em `http://127.0.0.1:8081`

| Rota | Resultado | Observacao |
|---|---:|---|
| `/login` | PASS, HTTP 200 | Pagina carregou |
| `/cadastros/produtos` | PASS, HTTP 200 | Tela carregou |
| `/api/produtos` | PASS, HTTP 200 | JSON valido, lista retornada |
| `/api/base/caixas` | FAIL, HTTP 500 | Erro JPA/PostgreSQL a diagnosticar |
| `/api/municipios` | FAIL, HTTP 500 | Erro JPA/PostgreSQL a diagnosticar |
| `/api/relatorios` | HTTP 404 | Rota base nao possui handler; endpoints especificos ficam sob `/api/relatorios/...` |

## Contrato da tela de produtos

- A pagina contem `descricaoProduto`, `tipoProduto`, `valorServico` e referencia `/api/produtos`: PASS.
- Validacao negativa `POST /api/produtos/salvar` com payload vazio: HTTP 400 com `Descricao obrigatoria`: PASS.

## Limitacoes

- Docker nao esta disponivel no ambiente, portanto nao foi possivel executar PostgreSQL isolado/Testcontainers.
- A consulta manual ao PostgreSQL solicitou senha no terminal; nenhuma senha foi registrada neste relatorio.
- Os dois HTTP 500 precisam da mensagem completa do PostgreSQL/JPA para uma correcao segura. Nenhuma alteracao de codigo foi feita com base em suposicao.
