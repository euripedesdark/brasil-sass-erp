# Auditoria de Autenticacao, Spring AI e Cobertura Frontend x Backend

**Atualizacao:** 24/09/2026  
**Repositorio:** BRASIL-SAAS-ERP  
**Objetivo:** registrar a autenticacao implementada recentemente e substituir as estimativas antigas por uma regra objetiva de cobertura funcional.

## 1. Autenticacao

O ERP possui dois caminhos de autenticacao.

### Usuario ERP normal

- Consulta o usuario em `brasil-saas.bc_core_usuario`.
- Valida a senha com BCrypt contra `senha_hash`.
- Exige usuario ativo.
- Carrega perfis e permissoes efetivas.
- Emite `accessToken` e `refreshToken` JWT.

### PostgreSQL SUPERUSER

Quando a validacao BCrypt falha, o backend tenta autenticar as credenciais diretamente no PostgreSQL.

Regras:

1. A conexao de autenticacao usa SSL.
2. A conexao remove `sslcert`, `sslkey` e `sslrootcert` herdados da conexao tecnica `sa`.
3. O `sslmode` e forcado para `require`.
4. Acesso especial somente quando a role PostgreSQL autenticada possui `rolsuper=true`.
5. Role PostgreSQL comum nao recebe privilegio administrativo.
6. Se o SUPERUSER ainda nao existir em `bc_core_usuario`, o sistema o provisiona.
7. O usuario provisionado recebe perfil ADMIN e autoridades `ROLE_ADMIN` e `ROLE_SUPERADMIN`.
8. A senha PostgreSQL nunca e armazenada no ERP.
9. O usuario provisionado recebe somente um hash BCrypt aleatorio para satisfazer o modelo de dados; futuras autenticacoes continuam usando PostgreSQL.

### JWT

- `/api/auth/login` e `/api/auth/refresh` permanecem publicos.
- Refresh token nao pode ser usado como Bearer token em APIs.
- O filtro JWT rejeita tokens com claim `type=refresh`.
- A aplicacao continua stateless.

### Hardening futuro

O proximo endurecimento previsto e autenticar o SUPERUSER PostgreSQL com certificado/PKI. O certificado tecnico da conexao `sa` nao deve ser reutilizado para representar a identidade do usuario.

## 2. Spring AI

Existe uma copia extensa do Spring AI em:

`src/main/resources/microservices/spring-ai/`

Esse diretorio e codigo-fonte incorporado ao repositorio e nao e empacotado como recurso da aplicacao porque o `pom.xml` exclui `microservices/**`.

A aplicacao principal, entretanto, **usa Spring AI de verdade**:

- Dependencia Maven: `org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M3`.
- `ChatClientConfig.java` cria/configura o `ChatClient`.
- `ChatServiceImpl.java` injeta `ChatClient` e executa `chatClient.prompt().user(...).call().content()`.
- O frontend possui `components/ia/IA.jsx` e `services/IaService.js`.

Na configuracao atual, o chat OpenAI esta desabilitado. Portanto existe integracao real no codigo, mas o provedor nao esta ativo neste ambiente.

## 3. Cobertura Backend x Frontend

A documentacao anterior afirmava 100% de frontend em alguns pontos, mas os proprios mapas de cobertura listavam lacunas. A regra atual passa a ser:

`Controller -> endpoint -> Service React -> componente React -> rota -> menu`

Um controller existente nao significa automaticamente que a funcionalidade esta disponivel para o usuario.

### Backend

A documentacao do projeto registra 45 controllers proprios no monolito, distribuidos entre:

- Core
- Cadastro
- Financeiro
- Fiscal
- RH
- Vendas
- Compras
- Estoque
- Servicos
- Producao

Tambem existem modulos de IA e BI, alem das bibliotecas/microservicos especializados.

### Frontend

O `App.jsx` atual ja possui rotas para:

- Administracao
- Perfil
- Cadastro de pessoas, produtos, categorias, clientes, fornecedores, marcas, servicos, transportadoras, unidades e municipios
- Vendas
- Compras
- Estoque
- Financeiro e seus submodulos
- Fiscal: NCM, CFOP, CEST, ISSQN, entrada de NF, impostos e SEFAZ
- RH: funcionarios, cargos e folha
- Producao
- Servicos e ordens de servico
- BI
- IA
- Relatorios

Isso demonstra que o frontend atual e maior que os mapas antigos indicavam. Porem, a existencia da rota nao prova que a tela esteja 100% integrada ao endpoint correspondente.

## 4. Problemas encontrados na auditoria

### Imports relativos incorretos

Componentes em subdiretorios como:

- `components/cadastro`
- `components/fiscal`
- `components/rh`
- `components/core`

estavam usando caminhos como:

`../contexts/AuthContext`

e

`../services/...`

Quando esses arquivos estao dois niveis abaixo de `src`, o caminho correto e:

`../../contexts/AuthContext`

e

`../../services/...`

Esses erros explicam diretamente falhas de build como:

`Could not resolve "../contexts/AuthContext" from "src/components/fiscal/EntradaNota.jsx"`

Os imports incorretos foram corrigidos no repositorio.

## 5. Menu

O menu lateral continua baseado em PrimeReact, conforme a arquitetura documentada do projeto.

Para eliminar interferencia do tema visual, o `PanelMenu` foi colocado em modo `unstyled` e o CSS do ERP passou a controlar explicitamente:

- cor do texto;
- visibilidade;
- opacidade;
- tamanho;
- peso da fonte;
- contraste;
- hover;
- fundo da sidebar.

A sidebar tambem passou a usar fundo mais opaco para manter contraste consistente com a imagem de fundo.

## 6. Regra para os proximos modulos

Nenhum modulo deve ser considerado concluido apenas porque existe:

- um Controller;
- um Service Java;
- um componente React;
- ou uma rota.

A conclusao funcional exige o caminho completo:

**Banco -> Service Java -> Controller REST -> Service React -> Componente React -> Rota -> Menu**

E, para operacoes de escrita:

**Frontend -> API -> autorizacao -> Service -> Repository -> banco**

Essa matriz sera usada para completar o frontend sem quebrar o backend existente.
