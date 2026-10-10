# Agente OpenRouter no GitHub

O workflow `.github/workflows/openrouter-issue-agent.yml` permite solicitar uma alteração em uma issue ou PR sem depender do agente hospedado do Copilot.

## Como chamar

Em uma issue ou pull request do repositório, um OWNER, MEMBER ou COLLABORATOR comenta:

```text
/brasilcloud-agent Corrija o erro descrito aqui, adicione um teste e explique a causa raiz.
```

O workflow usa `OPENROUTER_API_KEY`, `AGENT_GITHUB_TOKEN` (Settings → Secrets and variables → Actions) e a variável opcional `OPENROUTER_MODEL`. Sem a variável de modelo, usa `openrouter/free`. Crie um fine-grained PAT com acesso ao repositório e permissões Contents, Issues e Pull requests (read/write), além de Actions (read) para `AGENT_GITHUB_TOKEN`; ele permite que as PRs criadas pelo agente disparem a CI normal. Não use nem compartilhe a chave OpenRouter como token do GitHub.

O agente lê a solicitação e um conjunto limitado de arquivos da branch base, solicita uma proposta ao OpenRouter, valida os caminhos e o patch, cria uma branch `agent/issue-NUMERO-RUN_ID`, faz commit e abre uma PR. Não faz merge nem deploy. A CI normal da PR deve validar o resultado; uma pessoa deve revisar antes de aprovar.

## Limites de segurança

- O workflow executa o script confiável da branch padrão e **não executa código da PR que o acionou**.
- O conteúdo da issue, comentários, diff e arquivos é tratado como entrada não confiável.
- Patches que alteram workflows, scripts em `.github/`, configuração de produção, segredos, certificados, dependências/build e arquivos de deploy são bloqueados.
- O agente não roda builds ou testes neste job. A PR criada deve passar pela CI regular.
- O conteúdo relevante do repositório e da solicitação é enviado ao provedor OpenRouter para inferência. Não inclua segredos, dados de clientes ou informações de produção nas issues.
- `openrouter/free` é um roteador de modelos gratuitos: modelo efetivo, disponibilidade, limites e suporte a saída estruturada podem variar. Uma resposta inválida faz o job falhar sem aplicar patch.

Este é um agente personalizado implementado com GitHub Actions; não é um agente nativo do GitHub.com nem substitui o Copilot no editor.


## Correção automática de CI

Além do comando manual, o workflow `.github/workflows/openrouter-ci-auto-repair.yml` observa falhas de CI associadas a pull requests. Quando encontra uma falha elegível, recolhe logs, consulta o OpenRouter e pode criar uma PR de correção empilhada sobre a branch que falhou. Se não encontrar uma correção segura, comenta que não conseguiu propor um patch.

### Limites deliberados

- Atua apenas em PRs originadas no mesmo repositório; PRs de forks são ignoradas.
- Não faz merge, deploy ou execução do patch gerado.
- Não altera workflows, arquivos `.github/`, dependências/build, configuração de produção, segredos ou certificados.
- Só abre uma proposta por vez para cada branch de origem; não cria outra enquanto já houver uma PR de reparo aberta.
- A PR gerada precisa passar pela CI normal e ser revisada antes de merge.
- O gatilho `workflow_run` só passa a funcionar depois que este workflow for integrado à branch padrão.
- Requer `OPENROUTER_API_KEY` e `AGENT_GITHUB_TOKEN`. O token do GitHub deve ter Contents, Issues e Pull requests com leitura/escrita e Actions com leitura. Restrinja-o ao repositório.
- Falhas de deploy em `push` na `main` não são reparadas automaticamente por este fluxo; ele se limita a falhas de workflows de PR.

Isso é automação de correção assistida por IA, não garantia de correção: algumas falhas dependem de serviços externos, infraestrutura, banco, secrets ou decisões arquiteturais e precisam de investigação humana.
