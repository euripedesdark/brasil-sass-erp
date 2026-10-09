# Stripe per company

The Stripe integration is **multi-company**. Stripe credentials belong to the tenant in `brasil_saas.bc_core_empresa`; there is no longer a global `STRIPE_SECRET_KEY` shared by every company.

## Who can configure it

Only these authorities may configure Stripe:

- `ROLE_DIRETORIA`
- `ROLE_GESTOR`
- `ROLE_SUPERUSER`

The backend is the final authority. Hiding the section in React does not replace `@PreAuthorize`.

## Company settings

The **Configure Company** screen provides:

- Stripe Secret API Key;
- Webhook Signing Secret;
- enable/disable Stripe;
- connection test.

Both credentials are encrypted using AES-GCM. The encryption master key is never stored in the database or frontend.

The server must define:

```bash
export APP_SECURITY_SECRET_ENCRYPTION_KEY='a-long-random-master-key'
```

Do not commit this variable to Git.

## Test

After saving the Secret API Key:

1. Click **Test Stripe connection**.
2. The backend calls `GET /v1/account` using that company's key.
3. The Stripe account ID is stored in `bc_core_empresa.stripe_account_id`.
4. The screen displays only the account ID and a masked key.

The secret key is never returned to React.

## Webhook

Each company has its own endpoint:

```
POST /api/financeiro/stripe/webhook/{empresaId}
```

The signing secret is resolved using the URL's `empresaId`. The event also carries `empresaId` in metadata, and the backend rejects events when the values do not match.

Configure these ERP events in Stripe:

- `checkout.session.completed`
- `checkout.session.async_payment_succeeded`
- `checkout.session.async_payment_failed`
- `invoice.paid`
- `invoice.payment_failed`
- `payment_intent.payment_failed`

## Flow

```
Company
  |
  +-- Secret API Key (encrypted)
  +-- Webhook Secret (encrypted)
  +-- Stripe Account ID
  +-- Stripe enabled?
          |
          v
Backend resolves empresa_id
          |
          v
StripeClient(key for that company)
          |
          +--> Checkout / Invoice
          |
          +--> Webhook /webhook/{empresaId}
                    |
                    +--> validate signature
                    +--> validate metadata.empresaId
                    +--> settle receivable for the same tenant
```

This prevents one company's Stripe credentials from being used to charge or query another company.

## Production

Before enabling Stripe for a company:

- set `APP_SECURITY_SECRET_ENCRYPTION_KEY`;
- register that company's own Stripe Secret API Key;
- register the Webhook Signing Secret for that endpoint;
- save the settings;
- run the connection test;
- only then enable Stripe for the company.

The Stripe key previously exposed in chat must not be reused. Generate or rotate a new credential before registering it in the ERP.

## Payment methods

The current design does not require a physical card terminal.

- **Card:** Stripe-hosted Checkout; customers can pay on their own phone through a link or QR code.
- **PIX:** offered by Checkout when enabled and eligible for the company's Stripe account.
- **Boleto:** offered through the billing/Invoice flow, subject to account availability and payment-method settings.
- **Confirmation:** the ERP marks a receivable as paid only after confirmation through the webhook; the Checkout return URL does not settle it by itself.
- **Invoice:** the backend finalizes the Invoice when needed to ensure the Hosted Invoice Page exists before returning its link to the frontend.

The ERP does not store card numbers, CVVs, or raw card data.

## NFC / Tap to Pay — future

**NFC/Tap to Pay is not part of the current web module.** It is reserved for the future **BrasilCloud ERP App module**, where a compatible phone may act as a payment terminal while reusing the same financial integration and webhook reconciliation.

The App module must reuse the finance settlement/reconciliation engine and must not introduce a second settlement rule.
