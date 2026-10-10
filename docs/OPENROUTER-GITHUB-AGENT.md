# Agente OpenRouter no GitHub

O workflow `.github/workflows/openrouter-issue-agent.yml` permite solicitar uma alteração em uma issue ou PR sem depender do agente hospedado do Copilot.

## Como chamar

Em uma issue ou pull request do repositório, um OWNER, MEMBER ou COLLABORATOR comenta:

```text
/brasilcloud-agent Corrija o erro descrito aqui, adicione um teste e explique a causa raiz.
```

O workflow usa `OPENROUTER_API_KEY` (Settings → Secrets and variables → Actions) e a variável opcional `OPENROUTER_MODEL`. Sem a variável, usa `openrouter/free`.

O agente lê a solicitação e um conjunto limitado de arquivos da branch base, solicita uma proposta ao OpenRouter, valida os caminhos e o patch, cria uma branch `agent/issue-NUMERO`, faz commit e abre uma PR. Não faz merge nem deploy. A CI normal da PR deve validar o resultado; uma pessoa deve revisar antes de aprovar.

## Limites de segurança

- O workflow executa o script confiável da branch padrão e **não executa código da PR que o acionou**.
- O conteúdo da issue, comentários, diff e arquivos é tratado como entrada não confiável.
- Patches que alteram workflows, scripts em `.github/`, configuração de produção, segredos, certificados, dependências/build e arquivos de deploy são bloqueados.
- O agente não roda builds ou testes neste job. A PR criada deve passar pela CI regular.
- O conteúdo relevante do repositório e da solicitação é enviado ao provedor OpenRouter para inferência. Não inclua segredos, dados de clientes ou informações de produção nas issues.
- `openrouter/free` é um roteador de modelos gratuitos: modelo efetivo, disponibilidade, limites e suporte a saída estruturada podem variar. Uma resposta inválida faz o job falhar sem aplicar patch.

Este é um agente personalizado implementado com GitHub Actions; não é um agente nativo do GitHub.com nem substitui o Copilot no editor.
