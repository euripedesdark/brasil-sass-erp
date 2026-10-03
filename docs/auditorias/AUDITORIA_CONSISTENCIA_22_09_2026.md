# Auditoria Geral de Consistência — Brasil SaaS ERP

**Data:** 22/09/2026  
**Branch:** main

## Escopo

Varredura da árvore Git completa, documentação Markdown, código principal, módulos independentes, frontend React, configurações, workflows e referências de credenciais.

## Achados críticos corrigidos nesta rodada

### 1. Imports quebrados no frontend

Os componentes de Compras, Estoque, Vendas e Serviços usavam caminhos relativos `../services` e `../contexts`, mas os diretórios reais ficam em `src/services` e `src/contexts`.

Corrigido para:

- `../../services`
- `../../contexts`

### 2. Credenciais hardcoded

Foi encontrada uma senha literal de banco/certificado em múltiplos arquivos. O código ativo principal foi alterado para usar variáveis de ambiente.

Corrigido nesta rodada:

- `src/main/resources/application.properties`
- `src/main/java/br/com/brasil_saas/fiscal/service/impl/NFeServiceImpl.java`
- `modules/fiscal/src/main/java/br/com/brasil_saas/fiscal/service/impl/NFeServiceImpl.java`

**Importante:** ainda existem ocorrências históricas, scripts, unit files e documentação contendo a senha antiga. Essas ocorrências precisam ser eliminadas antes de considerar a higiene de segredos concluída.

### 3. NF-e ainda possui implementação simulada

A implementação atual de `NFeServiceImpl` contém:

- `PROTOCOLO-SIMULADO`
- `CANCELAMENTO-SIMULADO`
- `SITUACAO-SIMULADA`

e TODOs para chamadas reais à biblioteca SEFAZ.

Portanto a documentação não deve afirmar que a emissão NF-e está integralmente funcional até essa integração ser efetivamente executada e testada.

### 4. Documentação conflitante

Foram encontradas afirmações antigas de que:

- Compras/Estoque/Vendas/Serviços eram placeholders;
- CI/CD não existia;
- frontend tinha 25 componentes;
- testes tinham <1% de cobertura;
- determinadas estruturas ainda estavam vazias.

Essas informações ficaram desatualizadas em relação ao estado atual do repositório.

### 5. Documentação estrutural vazia

`docs/arquitetura.md` e `docs/modelo-dados.md` ainda são esqueletos antigos e não representam a arquitetura/modelo atual.

## Inventário atual verificado

Frontend React:

- **49 arquivos JSX**
- **39 serviços JS**
- **29 arquivos CSS**
- Compras, Estoque, Vendas e Serviços possuem implementações reais no código atual, não apenas placeholders.

Microservices:

- `src/main/resources/microservices/` contém os componentes fiscais/eSocial e demais fontes auxiliares.
- O root POM mantém essa árvore fora do empacotamento direto do JAR principal.
- Foi criado workflow próprio de validação dessa árvore.

## Pontos que permanecem para a próxima etapa

1. Remover credenciais reais de todos os arquivos versionados.
2. Rotacionar qualquer credencial que tenha sido utilizada fora do repositório.
3. Implementar ou marcar explicitamente como não disponível a integração real de NF-e antes de qualquer status "100%".
4. Comparar `src/main/java` e `modules/` para detectar duplicação/divergência de classes.
5. Validar todos os endpoints documentados contra controllers reais.
6. Atualizar a documentação principal para refletir o estado real.
7. Completar `docs/arquitetura.md` e `docs/modelo-dados.md`.
8. Executar build/testes e corrigir os erros reais do GitHub Actions.

## Regra adotada

A partir desta auditoria, documentação de "completo", "100%" ou "funcional" só deve ser mantida quando houver implementação correspondente no código e validação automatizada ou evidência operacional compatível.
