# Brasil SaaS ERP — Microservices/Fiscal API Inventory

## Repository verification — 2026-09-24

The directory `src/main/resources/microservices/` is present in the `main` branch and is tracked by Git.

### Integration components and intended use

| Component | Present | Build descriptor | Intended role |
|---|---|---|---|
| `Java_NFe` | Yes | Maven | NF-e |
| `Java_CTe` | Yes | Maven | CT-e |
| `Java_MDFe` | Yes | Maven | MDF-e |
| `Java-Efd-Icms` | Yes | Maven | EFD ICMS/IPI |
| `Java-Efd-Contribuicoes` | Yes | Maven | EFD Contribuições |
| `Java_Certificado` | Yes | Maven | Certificados digitais |
| `Java_Pdf_Signature` | Yes | Maven | Assinatura de PDF |
| `nfe` | Yes | Maven | Comunicação NF-e; no ERP, destinada à entrada de nota de produto |
| `nfse` | Yes | Maven | NFS-e nacional |
| `nfse_prefeitura_sp` | Yes | Project files | Integração antiga/funcional da Prefeitura de São Paulo; contrato deve seguir o PDF atualizado da prefeitura |
| `NFSe-SaoPaulo-SP` | Yes | Project files | Referência/instruções atualizadas para NFS-e de São Paulo |
| `nfse-sp-bridge` | Yes | Ruby | Bridge da NFS-e de São Paulo |
| `esocial` | Yes | Maven | eSocial; integração a ser utilizada pelo módulo de RH |
| `spring-ai` | Yes | Maven multi-module | Código-fonte/base do Spring AI; não confundir com a implementação de IA runtime do ERP |
| `EchoAvatar` | Yes | Project files | Componente de apoio |

### Regra fiscal definida para o ERP

A pasta `nfe` não representa um módulo genérico de vendas. A regra funcional definida para o produto é:

**Entrada de nota de produto → módulo Fiscal → Entrada de NF → utilização da integração NF-e quando aplicável.**

NFS-e nacional e NFS-e São Paulo permanecem separadas da entrada de NF-e de produto.

### Spring AI — o que é usado pelo ERP

O repositório contém uma cópia extensa do projeto Spring AI em:

`src/main/resources/microservices/spring-ai/`

Essa árvore **não é o runtime direto da IA do ERP**.

A implementação efetivamente usada pelo aplicativo principal está em:

- `src/main/java/br/com/brasil_saas/ia/config/ChatClientConfig.java`
- `src/main/java/br/com/brasil_saas/ia/service/impl/ChatServiceImpl.java`
- `src/main/java/br/com/brasil_saas/ia/controller/ChatController.java`

O `pom.xml` principal utiliza:

`org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M3`

O fluxo atual é:

`React IA → /api/ia/chat → ChatController → ChatServiceImpl → Spring AI ChatClient → OpenRouter`

A chave `OPENROUTER_API_KEY` permanece exclusivamente no backend.

O `ChatClientConfig` possui fallback local quando não existe um `ChatModel` real, evitando que o contexto da aplicação falhe em ambientes sem o provedor configurado.

### Separação do código-fonte incorporado

O `pom.xml` raiz exclui:

```
src/main/resources/microservices/**
```

Isso impede que as cópias desses projetos sejam empacotadas acidentalmente no JAR principal.

Portanto, **presença no repositório não significa integração runtime automática**. Cada componente deve ter uma ligação explícita com o backend, biblioteca, serviço ou processo de build correspondente antes de ser considerado funcional no ERP.

## eSocial e RH

Existe uma implementação Java de domínio no backend principal, incluindo a entidade:

`src/main/java/br/com/brasil_saas/fiscal/model/Esocial.java`

e também a árvore externa:

`src/main/resources/microservices/esocial/`

O próximo passo funcional é ligar a capacidade de eSocial às operações do módulo de RH, sem copiar diretamente a árvore de microservices para o runtime do ERP.

## CI/CD

A validação dos componentes deve:

1. verificar se os diretórios obrigatórios existem;
2. validar seus descritores de build;
3. impedir que `microservices/**` seja empacotado no JAR principal;
4. validar separadamente integrações fiscais quando forem ativadas;
5. evitar declarar uma integração como funcional apenas porque seus arquivos estão presentes no Git.

O script existente `scripts/verify-microservices.sh` deve continuar sendo a verificação estrutural mínima.

## Relação com frontend

A existência de uma API, biblioteca ou integração não implica automaticamente uma tela React.

A cobertura funcional deve ser rastreada como:

**Backend/API → Service JS → Componente React → Rota → Menu → Build → Teste funcional.**

Quando uma função do backend não tiver interface, ela deve ser registrada como **cobertura parcial/sem tela**, e não simplesmente omitida do inventário.

Isso é especialmente importante para:

- IA;
- BI;
- Fiscal;
- Produção;
- eSocial/RH;
- integrações NF-e/CT-e/MDF-e/EFD;
- NFS-e nacional e São Paulo.

## Autenticação — referência

A autenticação alterada em 24/09/2026 está documentada em `docs/autenticacao.md`, `docs/arquitetura.md` e na auditoria Frontend x Backend.

O modelo atual é:

1. usuário ERP → BCrypt;
2. PostgreSQL SUPERUSER → autenticação direta da role;
3. somente `rolsuper=true` pode substituir o BCrypt;
4. SUPERUSER inexistente no cadastro ERP pode ser provisionado;
5. SUPERUSER recebe `ROLE_ADMIN` e `ROLE_SUPERADMIN`;
6. senha PostgreSQL nunca é armazenada no ERP;
7. a conexão técnica `sa` continua separada da autenticação do usuário;
8. para autenticação da role PostgreSQL, certificados da conta técnica são removidos da URL e `sslmode=require` é forçado;
9. JWT continua sendo emitido após autenticação;
10. refresh token não pode ser usado como Bearer token de API.

O hardening com certificado de cliente por role permanece como etapa posterior.

## Frontend — erro de build registrado em 24/09/2026

Foi observado localmente:

```
Could not resolve "../contexts/AuthContext" from "src/components/fiscal/EntradaNota.jsx"
```

A versão atual de `main` já removeu essa dependência desnecessária de `EntradaNota.jsx`. O arquivo atual importa apenas:

```
../../services/EntradaNotaService
```

Portanto, se esse erro aparecer no checkout local, o primeiro passo é sincronizar o repositório antes de alterar o código:

```bash
git pull --ff-only origin main
cd src/main/resources/static/react
npm run build
```

Não deve ser feita alteração no backend para contornar esse erro: trata-se de resolução de módulo do frontend.

## Status

- Inventário de integrações: **atualizado em 24/09/2026**
- Spring AI runtime do ERP: **integrado**
- Spring AI em `microservices/`: **fonte/base separada, não runtime direto**
- NF-e entrada de produto: **regra funcional registrada**
- NFS-e nacional: **registrada**
- NFS-e São Paulo: **registrada**
- eSocial: **registrado para evolução do RH**
- Autenticação ERP + PostgreSQL SUPERUSER + JWT: **documentada**
- Build error de `EntradaNota.jsx`: **corrigido no main**
- Cobertura frontend/backend: **deve continuar sendo tratada por matriz funcional**
