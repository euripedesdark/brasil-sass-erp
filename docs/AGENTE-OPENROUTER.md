# Usar o agente BrasilCloud com OpenRouter (alternativa ao GitHub Copilot)

Este repositório inclui um modo personalizado do **Roo Code** em `.roomodes` e regras específicas em `.roo/rules-brasilcloud-erp/`. Ele orienta o agente a trabalhar conforme as convenções do BrasilCloud ERP.

## 1. Instalar a extensão

1. Abra o projeto no VS Code.
2. Instale a extensão **Roo Code** pela área de Extensões do VS Code.
3. Abra o painel Roo Code e entre nas configurações do provedor de modelo.

## 2. Configurar OpenRouter

1. Selecione **OpenRouter** como provedor.
2. Cole a sua chave da OpenRouter no campo de API key da extensão.
3. Escolha um modelo disponível na sua conta OpenRouter. Para testar sem custo, procure um modelo gratuito disponível no catálogo; disponibilidade, limites e roteamento podem mudar.
4. Salve as configurações e faça uma solicitação pequena para confirmar a conexão.

**Importante sobre o segredo do GitHub:** o secret `OPENROUTER_API_KEY` cadastrado em GitHub Actions é usado por workflows e não pode ser lido de volta para preencher automaticamente o VS Code. O GitHub não revela o valor de um secret existente. Para usar a mesma chave localmente, informe-a diretamente nas configurações do Roo Code. Não a coloque em arquivos do repositório, `.env` versionado, prompts, logs ou commits. Se a chave já foi exposta em conversa ou log, revogue-a e crie outra.

## 3. Selecionar o modo personalizado

1. Recarregue a janela do VS Code depois de abrir o repositório.
2. No Roo Code, abra o seletor de modo e selecione **BrasilCloud ERP** (slug `brasilcloud-erp`).
3. Faça uma tarefa pequena primeiro e confira o diff antes de aceitar alterações.

## 4. Como trabalhar com segurança

- O agente pode ler e editar o projeto e executar comandos de desenvolvimento.
- Revise os comandos e diffs antes de aceitar operações que alterem dados, infraestrutura ou serviços.
- Não autorize deploy, merge em `main` ou alterações destrutivas de banco sem revisar o plano.
- A configuração do modo define instruções e permissões do agente; ela não inclui nem armazena a chave OpenRouter.
- O uso do OpenRouter depende do modelo escolhido, do saldo/política da conta e dos limites do provedor. Uma resposta HTTP 402 no workflow anterior significa que a solicitação foi recusada por condições de cobrança, créditos ou acesso ao modelo; não é prova de que o agente está funcionando corretamente.

## 5. Limite importante

Este é um agente de desenvolvimento dentro do VS Code, com contexto e permissões da extensão Roo Code. Não é uma cópia do GitHub Copilot nem herda automaticamente as suas extensões, histórico, autenticação ou contexto. A chave do OpenRouter autentica o acesso ao modelo; ela não concede acesso ao GitHub por si só.
