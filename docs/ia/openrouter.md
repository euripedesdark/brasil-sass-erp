> 🤖 **Documento de trabalho gerado por IA** — este arquivo é um registro
> de sessão de desenvolvimento com assistente IA. Não é documentação oficial
> do sistema. Atualizado em 04/10/2026.
>
> Para documentação oficial, veja [`README.md`](../README.md) e
> [`docs/INDICE.md`](INDICE.md).

# OpenRouter e Spring AI — Brasil SaaS ERP

## Arquitetura

O ERP utiliza o Spring AI `ChatClient` no backend. A integração é feita pelo starter OpenAI-compatible do Spring AI e o provedor configurado é o OpenRouter.

Fluxo:

    React IA
       |
       | POST /api/ia/chat
       v
    ChatController
       |
       v
    ChatServiceImpl
       |
       v
    Spring AI ChatClient
       |
       | OpenAI-compatible Chat Completions
       v
    https://openrouter.ai/api/v1/chat/completions
       |
       v
    model = openrouter/auto

O token nunca deve ser enviado ao navegador nem armazenado no Git.

## Configuração

A aplicação usa a variável de ambiente `OPENROUTER_API_KEY`.

    export OPENROUTER_API_KEY='sua-chave-aqui'

A configuração versionada contém somente a referência à variável:

    spring:
      ai:
        openai:
          base-url: https://openrouter.ai/api/v1
          api-key: ${OPENROUTER_API_KEY:}
          chat:
            enabled: true
            options:
              model: openrouter/auto

O fallback `ChatClientConfig` continua existindo para ambientes sem um `ChatModel` real.

## Endpoint utilizado pelo Spring AI

O caminho usado pelo `ChatClient` é:

    POST https://openrouter.ai/api/v1/chat/completions

Com autenticação:

    Authorization: Bearer $OPENROUTER_API_KEY

O OpenRouter é tratado aqui como um provedor compatível com a API de chat da OpenAI. Isso permite manter `ChatClient` e `ChatServiceImpl` independentes do fornecedor.

## Outros endpoints OpenRouter

O OpenRouter também disponibiliza:

- `/api/v1/responses`
- `/api/v1/messages`

Esses formatos não são o caminho atual do `ChatClient` do ERP.

Não devemos criar chamadas paralelas no frontend. Caso o ERP precise de recursos específicos desses formatos, a implementação deve ser adicionada no backend como um adapter/serviço OpenRouter, mantendo a chave exclusivamente no servidor.

## Segurança da chave

A chave fornecida durante a configuração não deve ser colocada em `application.yml`, JavaScript, Markdown ou qualquer outro arquivo versionado.

Como uma chave real foi exposta durante a configuração, ela deve ser considerada comprometida e revogada/rotacionada no painel do OpenRouter antes de uso em produção.

## Teste

Depois de exportar a variável:

    export OPENROUTER_API_KEY='...'

iniciar o backend normalmente.

A tela `/ia` usa `POST /api/ia/chat` e o backend encaminha a solicitação pelo Spring AI.

## Observação sobre o modelo

`openrouter/auto` delega a seleção do modelo ao OpenRouter. O ERP não deve assumir características específicas de um modelo individual enquanto esse modo estiver configurado.