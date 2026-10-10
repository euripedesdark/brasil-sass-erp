# Configurar o OpenRouter no agente do VS Code

O arquivo `.github/agents/my-agent.agent.md` define as instruções do agente; ele **não instala nem configura sozinho um provedor de modelo**. Para que o agente use o OpenRouter, o modelo precisa existir no seletor de modelos do VS Code.

## Configuração única no VS Code

1. Abra o repositório `brasil-saas-erp` no VS Code e entre no Chat/Agents.
2. Abra o seletor de modelo → **Manage Language Models** → **Add Models** → **Custom Endpoint**.
3. Use o nome de provedor `OpenRouter` e informe sua chave de API no campo seguro apresentado pelo VS Code. Não cole a chave em arquivos do repositório, `settings.json`, terminal compartilhado ou logs.
4. Selecione a API **Chat Completions**.
5. No arquivo `chatLanguageModels.json` aberto pelo VS Code, configure o modelo com estes valores. Mantenha a chave como variável de entrada/segredo; se o assistente gerar outro identificador para ela, preserve o identificador gerado:

```json
{
  "name": "OpenRouter",
  "vendor": "customendpoint",
  "apiKey": "${input:openrouterApiKey}",
  "apiType": "chat-completions",
  "models": [
    {
      "id": "openrouter/free",
      "name": "openrouter/free",
      "url": "https://openrouter.ai/api/v1/chat/completions",
      "toolCalling": true,
      "maxInputTokens": 128000,
      "maxOutputTokens": 8192
    }
  ]
}
```

Não substitua a propriedade `apiKey` por uma chave literal. A variável `${input:openrouterApiKey}` deve ser resolvida pelo fluxo de segredo do VS Code; se o editor gerar outro nome de variável, mantenha o nome gerado.

6. Salve e, se necessário, reinicie/recarregue o VS Code. No seletor de modelos, confirme que `openrouter/free` aparece.
7. Abra o agente **BrasilCloud ERP Engineer**. O campo `model: openrouter/free` precisa corresponder ao nome/modelo disponível no seletor.
8. Faça uma pergunta curta e confira o modelo efetivamente usado ao passar o mouse sobre a resposta, quando essa informação estiver disponível no harness. Se o modelo não aparecer ou a chamada falhar, veja o erro de autenticação/endpoint no VS Code; o texto do perfil sozinho não prova que o OpenRouter foi chamado.

## O que isso não configura

- O segredo `OPENROUTER_API_KEY` dos GitHub Actions é independente e não é importado pelo VS Code.
- Esta configuração serve ao agente no VS Code (`target: vscode`). Ela não habilita automação de issues/PRs no GitHub.
- `openrouter/free` é um roteador gratuito: os modelos disponíveis, limites, suporte a ferramentas e disponibilidade podem variar.

Referência oficial: [AI language models in VS Code — Custom Endpoint](https://code.visualstudio.com/docs/agent-customization/language-models).
