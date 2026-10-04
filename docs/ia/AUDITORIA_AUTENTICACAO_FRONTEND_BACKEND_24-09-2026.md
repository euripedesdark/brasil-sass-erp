> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../../README.md) e
> [`docs/INDICE.md`](../INDICE.md).

> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Auditoria de Autenticacao, Frontend/Backend e IA — 24/09/2026

## 1. Autenticacao — estado atual

A autenticacao do Brasil SaaS ERP possui dois caminhos, sem misturar identidade PostgreSQL com senha ERP:

1. **Usuario ERP normal**
   - Usuario localizado em `brasil_saas.bc_core_usuario`.
   - Senha validada com BCrypt.
   - Usuario precisa estar ativo.
   - Perfis e permissoes continuam sendo carregados pelo modelo ERP.

2. **PostgreSQL SUPERUSER**
   - O login tenta autenticar o mesmo `username/password` diretamente no PostgreSQL usando uma conexao JDBC separada.
   - A conexao de autenticacao nao reutiliza o certificado cliente da conexao tecnica `sa`.
   - As opcoes `sslcert`, `sslkey` e `sslrootcert` sao removidas e a conexao de autenticacao usa `sslmode=require`.
   - Depois da autenticacao, o sistema consulta `pg_roles` e somente aceita o acesso especial quando `rolsuper=true`.
   - Se o SUPERUSER ainda nao existir em `bc_core_usuario`, ele e provisionado automaticamente como usuario ERP SUPERADMIN.
   - O SUPERUSER recebe `ROLE_ADMIN` e `ROLE_SUPERADMIN`, permitindo acesso integral.
   - A senha PostgreSQL nunca e gravada no ERP. O usuario provisionado recebe apenas um hash BCrypt aleatorio para satisfazer o modelo da entidade.
   - A hardening posterior pode exigir certificado cliente tambem no login do SUPERUSER.

### JWT

- O access token e usado como credencial para as APIs.
- O refresh token possui tipo proprio e nao pode ser usado como Bearer token de API.
- O filtro JWT rejeita explicitamente refresh tokens.
- O refresh endpoint continua responsavel por emitir novos tokens.

### Banco tecnico

A aplicacao continua usando o usuario tecnico PostgreSQL `sa` para a conexao normal, com SSL e certificado cliente configurados em `application.yml`.

## 2. Erro de build reportado em EntradaNota

O erro local:

`Could not resolve "../contexts/AuthContext" from "src/components/fiscal/EntradaNota.jsx"`

e causado por um import relativo incorreto no arquivo que esta sendo compilado localmente.

A partir de `src/components/fiscal/EntradaNota.jsx`, o caminho correto e:

`../../contexts/AuthContext`

O arquivo presente na branch `main` ja usa esse caminho correto. Portanto, se o build local ainda mostra `../contexts/AuthContext`, o checkout local esta diferente da branch `main` ou possui alteracao local nao commitada.

Antes de descartar qualquer trabalho local, conferir:

```bash
git status --short
git diff -- src/main/resources/static/react/src/components/fiscal/EntradaNota.jsx
git log -1 --oneline
```

Se nao houver trabalho local a preservar, sincronizar a branch e reconstruir:

```bash
git pull --rebase origin main
cd src/main/resources/static/react
npm run build
cd ../../../../../..
mvn clean package -DskipTests
```

## 3. Frontend x Backend

A auditoria mostra que o problema nao e simplesmente falta de componentes React. Existe uma diferenca de profundidade entre o backend e o frontend.

O backend esta organizado por dominios, incluindo:

- cadastro
- vendas
- compras
- estoque
- financeiro
- fiscal
- producao
- servicos
- RH
- BI
- IA
- core/admin
- integracoes e portais

O frontend possui os principais dominios e dezenas de rotas, mas varias telas ainda funcionam como camada de apresentacao/CRUD enquanto o backend possui uma superficie de API maior e mais especializada.

### Rotas React atualmente mapeadas

O `App.jsx` possui rotas para:

- administracao
- perfil
- pessoas
- produtos
- categorias
- clientes
- fornecedores
- marcas
- servicos
- transportadoras
- unidades de medida
- municipios
- vendas
- compras
- estoque
- financeiro e seus submodulos
- fiscal e seus submodulos
- RH
- producao
- BI
- IA
- relatorios

Isso cobre uma parte importante da estrutura do backend, mas nao significa cobertura funcional 1:1 dos endpoints.

### Pontos que precisam de evolucao

1. O menu deve ser derivado do mapa real de modulos/rotas, e nao criado isoladamente.
2. Cada item de menu precisa apontar para uma rota React existente.
3. Cada rota precisa renderizar um componente real.
4. O componente precisa consumir o endpoint Spring correspondente.
5. O endpoint precisa respeitar a permissao ERP correspondente.
6. Operacoes CRUD, filtros, paginacao, detalhes, documentos e acoes especificas precisam ser expostas no frontend quando existirem no backend.
7. Endpoints existentes sem tela devem ser identificados como **backend sem UI**, e nao simplesmente esquecidos do menu.

## 4. Spring AI

O ERP utiliza Spring AI de verdade.

Fluxo atual:

`React IA -> /api/ia/chat -> ChatController -> ChatServiceImpl -> Spring AI ChatClient -> OpenRouter`

Configuracao:

- starter: `org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M3`
- provedor: OpenRouter compativel com API OpenAI
- base URL: `https://openrouter.ai/api/v1`
- chave: `OPENROUTER_API_KEY`
- modelo configurado: `openrouter/auto`

O diretorio `src/main/resources/microservices/spring-ai` e uma copia/base do projeto Spring AI e nao deve ser confundido com a implementacao da IA usada diretamente pelo ERP.

A implementacao do ERP esta em:

- `br.com.brasil_saas.ia.config.ChatClientConfig`
- `br.com.brasil_saas.ia.service.impl.ChatServiceImpl`
- `br.com.brasil_saas.ia.controller.ChatController`
- demais controllers de IA

O `ChatClientConfig` possui fallback para ambientes sem `ChatModel`; esse fallback nao realiza chamada externa.

## 5. Regra para a proxima etapa

Nao corrigir telas isoladamente sem comparar:

`Controller -> Service -> Endpoint -> React Service -> React Component -> Route -> Menu`

A proxima evolucao do frontend deve ser uma auditoria de cobertura por dominio, identificando:

- backend existente;
- endpoint;
- permissao;
- service React;
- componente React;
- rota;
- item de menu;
- status da implementacao.

O objetivo e transformar o frontend em uma camada completa do ERP, sem remover ou alterar funcionalidades existentes do backend.
