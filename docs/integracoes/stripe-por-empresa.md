# Stripe por empresa

A integração Stripe é **multiempresa**. A credencial da Stripe pertence ao tenant em
`brasil_saas.bc_core_empresa`; não existe mais uma `STRIPE_SECRET_KEY` global usada
para todas as empresas.

## Quem pode configurar

Somente authorities:

- `ROLE_DIRETORIA`
- `ROLE_GESTOR`
- `ROLE_SUPERUSER`

O backend é a autoridade final. Esconder a seção no React não substitui o
`@PreAuthorize`.

## O que fica no cadastro da empresa

A tela **Configurar Empresa** possui:

- Secret API Key da Stripe;
- Webhook Signing Secret;
- habilitação/desabilitação da Stripe;
- teste de conexão.

As duas credenciais são armazenadas cifradas com AES-GCM. A chave mestra da
cifragem nunca fica no banco nem no frontend.

No servidor deve existir:

```bash
export APP_SECURITY_SECRET_ENCRYPTION_KEY='uma-chave-mestra-longa-e-aleatoria'
```

Não coloque essa variável no Git.

## Teste

Depois de salvar a Secret API Key:

1. clicar **Testar conexão Stripe**;
2. o backend chama `GET /v1/account` usando a chave daquela empresa;
3. o ID da conta retornado pela Stripe é armazenado em
   `bc_core_empresa.stripe_account_id`;
4. a tela mostra somente o ID e a chave mascarada.

A chave secreta nunca é devolvida para o React.

## Webhook

Cada empresa possui seu próprio endpoint:

```
POST /api/financeiro/stripe/webhook/{empresaId}
```

O signing secret é resolvido pelo `empresaId` da URL. O evento também carrega
`empresaId` em metadata e o backend rejeita se os dois não coincidirem.

Configure na Stripe os eventos usados pelo ERP:

- `checkout.session.completed`
- `checkout.session.async_payment_succeeded`
- `checkout.session.async_payment_failed`
- `invoice.paid`
- `invoice.payment_failed`
- `payment_intent.payment_failed`

## Fluxo

```
Empresa
  |
  +-- Secret API Key (cifrada)
  +-- Webhook Secret (cifrado)
  +-- Stripe Account ID
  +-- Stripe habilitada?
          |
          v
Backend resolve empresa_id
          |
          v
StripeClient(chave daquela empresa)
          |
          +--> Checkout / Invoice
          |
          +--> Webhook /webhook/{empresaId}
                    |
                    +--> valida assinatura
                    +--> valida metadata.empresaId
                    +--> baixa título do mesmo tenant
```

Isso evita que a credencial Stripe de uma empresa seja usada para cobrar ou
consultar outra empresa.

## Produção

Antes de ativar uma empresa:

- definir `APP_SECURITY_SECRET_ENCRYPTION_KEY`;
- cadastrar a Secret API Key da própria conta Stripe;
- cadastrar o Webhook Signing Secret correspondente àquele endpoint;
- salvar;
- executar o teste de conexão;
- só então habilitar Stripe para a empresa.

A chave Stripe exposta anteriormente no chat não deve ser reutilizada; gere/rotacione
uma nova credencial antes de cadastrá-la no ERP.


## Formas de pagamento

O desenho atual não exige máquina de cartão.

- **Cartão:** Checkout hospedado pela Stripe; o cliente pode pagar pelo próprio celular através do link/QR Code.
- **PIX:** disponibilizado pelo Checkout conforme os métodos de pagamento habilitados/elegíveis na conta Stripe da empresa.
- **Boleto:** disponibilizado pelo fluxo de cobrança/Invoice conforme disponibilidade da conta e configuração de métodos de pagamento.
- **Confirmação:** o ERP considera o título pago somente após confirmação recebida pelo webhook; a URL de retorno do Checkout não realiza a baixa sozinha.
- **Invoice:** o backend finaliza a Invoice quando necessário para garantir a geração da Hosted Invoice Page antes de devolver o link ao frontend.

Não armazenamos número de cartão, CVV ou dados brutos de cartão no ERP.

## NFC / Tap to Pay — futuro

**NFC/Tap to Pay não faz parte do módulo web atual.** Fica reservado para o futuro **módulo App do BrasilCloud ERP**, onde o celular compatível poderá atuar como terminal de pagamento, mantendo a mesma integração financeira e reconciliação por webhook.

O módulo App deverá reutilizar o motor de baixa/conciliação do financeiro; não deverá criar uma segunda regra de liquidação.
