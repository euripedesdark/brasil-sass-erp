package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.repository.PessoaRepository;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.BaixaRequest;
import br.com.brasil_saas.financeiro.dto.StripeDtos.CheckoutResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.InvoiceResponse;
import br.com.brasil_saas.financeiro.model.StripeCustomerMapping;
import br.com.brasil_saas.financeiro.model.StripePayment;
import br.com.brasil_saas.financeiro.model.StripeWebhookEvent;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.StripeCustomerMappingRepository;
import br.com.brasil_saas.financeiro.repository.StripePaymentRepository;
import br.com.brasil_saas.financeiro.repository.StripeWebhookEventRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.financeiro.service.StripeFinanceService;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeFinanceServiceImpl implements StripeFinanceService {
    private final TituloRepository tituloRepository;
    private final PessoaRepository pessoaRepository;
    private final StripeCustomerMappingRepository customerRepository;
    private final StripePaymentRepository paymentRepository;
    private final StripeWebhookEventRepository webhookRepository;
    private final br.com.brasil_saas.financeiro.service.TituloService tituloService;
    private final br.com.brasil_saas.core.service.EmpresaStripeService empresaStripeService;

    @Value("${stripe.success-url:http://localhost:8080/financeiro/stripe/sucesso?session_id={CHECKOUT_SESSION_ID}}")
    private String successUrl;
    @Value("${stripe.cancel-url:http://localhost:8080/financeiro/stripe/cancelado}")
    private String cancelUrl;
    @Value("${stripe.currency:brl}")
    private String currency;
    @Value("${stripe.automatic-tax:false}")
    private boolean automaticTax;
    @Value("${stripe.invoice-days-until-due:30}")
    private long invoiceDaysUntilDue;

    private StripeClient client(Long empresaId) {
        return new StripeClient(empresaStripeService.secretKey(empresaId));
    }

    @Override
    @Transactional
    public CheckoutResponse criarCheckout(Long empresaId, Long tituloId) {
        Titulo titulo = tituloRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(tituloId, empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));
        validarTituloReceber(titulo);

        StripePayment existente = paymentRepository
            .findFirstByEmpresaIdAndTituloIdAndStatusOrderByIdDesc(empresaId, tituloId, "OPEN")
            .orElse(null);
        if (existente != null && existente.getCheckoutUrl() != null) {
            return new CheckoutResponse(existente.getCheckoutSessionId(), existente.getCheckoutUrl(), existente.getStatus());
        }

        Pessoa pessoa = obterPessoa(empresaId, titulo.getPessoaId());
        String customerId = obterOuCriarCustomer(empresaId, pessoa);
        long amount = toMinorUnits(titulo.getValorSaldo());

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomer(customerId)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency)
                        .setUnitAmount(amount)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(titulo.getDescricao())
                            .build())
                        .build())
                    .build())
                .putMetadata("empresaId", String.valueOf(empresaId))
                .putMetadata("tituloId", String.valueOf(tituloId))
                .putMetadata("erp", "brasil-sass-erp");

            if (automaticTax) {
                builder.setAutomaticTax(SessionCreateParams.AutomaticTax.builder().setEnabled(true).build());
            }

            Session session = client(empresaId).v1().checkout().sessions().create(
                builder.build(),
                RequestOptions.builder()
                    .setIdempotencyKey("erp-checkout-" + empresaId + "-" + tituloId)
                    .build());

            StripePayment payment = new StripePayment();
            payment.setEmpresaId(empresaId);
            payment.setTituloId(tituloId);
            payment.setStripeCustomerId(customerId);
            payment.setCheckoutSessionId(session.getId());
            payment.setPaymentIntentId(session.getPaymentIntent());
            payment.setAmount(amount);
            payment.setCurrency(currency);
            payment.setStatus("OPEN");
            payment.setCheckoutUrl(session.getUrl());
            paymentRepository.save(payment);

            return new CheckoutResponse(session.getId(), session.getUrl(), payment.getStatus());
        } catch (StripeException e) {
            throw new IllegalStateException("Falha ao criar Checkout Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public InvoiceResponse criarInvoice(Long empresaId, Long tituloId) {
        Titulo titulo = tituloRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(tituloId, empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));
        validarTituloReceber(titulo);

        Pessoa pessoa = obterPessoa(empresaId, titulo.getPessoaId());
        String customerId = obterOuCriarCustomer(empresaId, pessoa);
        long amount = toMinorUnits(titulo.getValorSaldo());

        try {
            InvoiceItemCreateParams itemParams = InvoiceItemCreateParams.builder()
                .setCustomer(customerId)
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription(titulo.getDescricao())
                .putMetadata("empresaId", String.valueOf(empresaId))
                .putMetadata("tituloId", String.valueOf(tituloId))
                .build();

            client().v1().invoiceItems().create(itemParams,
                RequestOptions.builder()
                    .setIdempotencyKey("erp-invoice-item-" + empresaId + "-" + tituloId)
                    .build());

            InvoiceCreateParams.Builder invoiceBuilder = InvoiceCreateParams.builder()
                .setCustomer(customerId)
                .setCollectionMethod(InvoiceCreateParams.CollectionMethod.SEND_INVOICE)
                .setDaysUntilDue(invoiceDaysUntilDue)
                .setAutoAdvance(true)
                .setDescription("BrasilCloud ERP — " + titulo.getDescricao())
                .putMetadata("empresaId", String.valueOf(empresaId))
                .putMetadata("tituloId", String.valueOf(tituloId));

            if (automaticTax) {
                invoiceBuilder.setAutomaticTax(InvoiceCreateParams.AutomaticTax.builder().setEnabled(true).build());
            }

            Invoice invoice = client().v1().invoices().create(
                invoiceBuilder.build(),
                RequestOptions.builder()
                    .setIdempotencyKey("erp-invoice-" + empresaId + "-" + tituloId)
                    .build());

            StripePayment payment = new StripePayment();
            payment.setEmpresaId(empresaId);
            payment.setTituloId(tituloId);
            payment.setStripeCustomerId(customerId);
            payment.setInvoiceId(invoice.getId());
            payment.setAmount(amount);
            payment.setCurrency(currency);
            payment.setStatus("INVOICE_OPEN");
            paymentRepository.save(payment);

            return new InvoiceResponse(invoice.getId(), invoice.getHostedInvoiceUrl(), invoice.getStatus());
        } catch (StripeException e) {
            throw new IllegalStateException("Falha ao criar Invoice Stripe: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void processarWebhook(Long empresaId, String payload, String signature) {
        if (empresaId == null) throw new IllegalArgumentException("Empresa não informada no webhook Stripe");
        final Event event;
        try {
            event = Webhook.constructEvent(payload, signature, empresaStripeService.webhookSecret(empresaId));
        } catch (Exception e) {
            throw new IllegalArgumentException("Webhook Stripe inválido", e);
        }

        if (webhookRepository.findByStripeEventId(event.getId()).isPresent()) {
            return;
        }

        String type = event.getType();
        Long empresaId = null;

        try {
            if (type.startsWith("checkout.session.")) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session == null) return;

                Map<String, String> metadata = session.getMetadata() == null ? Map.of() : session.getMetadata();
                Long eventoEmpresaId = metadataLong(metadata, "empresaId");
                if (!empresaId.equals(eventoEmpresaId)) {
                    throw new IllegalArgumentException("Webhook Stripe não pertence à empresa da URL");
                }
                Long tituloId = metadataLong(metadata, "tituloId");

                if ("checkout.session.completed".equals(type)) {
                    if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                        liquidarCheckout(empresaId, tituloId, session);
                    } else {
                        atualizarCheckout(empresaId, session.getId(), "PENDING", session.getPaymentIntent());
                    }
                } else if ("checkout.session.async_payment_succeeded".equals(type)) {
                    liquidarCheckout(empresaId, tituloId, session);
                } else if ("checkout.session.async_payment_failed".equals(type)) {
                    atualizarCheckout(empresaId, session.getId(), "FAILED", session.getPaymentIntent());
                }
            } else if (type.startsWith("invoice.")) {
                Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
                if (invoice == null || invoice.getMetadata() == null) return;

                Map<String, String> metadata = invoice.getMetadata();
                Long eventoEmpresaId = metadataLong(metadata, "empresaId");
                if (!empresaId.equals(eventoEmpresaId)) {
                    throw new IllegalArgumentException("Webhook Stripe não pertence à empresa da URL");
                }
                Long tituloId = metadataLong(metadata, "tituloId");

                if ("invoice.paid".equals(type)) {
                    liquidarInvoice(empresaId, tituloId, invoice);
                } else if ("invoice.payment_failed".equals(type)) {
                    atualizarInvoice(empresaId, invoice.getId(), "FAILED");
                }
            } else if ("payment_intent.payment_failed".equals(type)) {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent == null || intent.getMetadata() == null) return;

                Map<String, String> metadata = intent.getMetadata();
                Long eventoEmpresaId = metadataLong(metadata, "empresaId");
                if (!empresaId.equals(eventoEmpresaId)) {
                    throw new IllegalArgumentException("Webhook Stripe não pertence à empresa da URL");
                }
                atualizarPaymentIntent(empresaId, intent.getId(), "FAILED");
            } else {
                return;
            }
        } finally {
            if (empresaId != null) {
                StripeWebhookEvent record = new StripeWebhookEvent();
                record.setEmpresaId(empresaId);
                record.setStripeEventId(event.getId());
                record.setEventType(type);
                webhookRepository.save(record);
            }
        }
    }

    private void liquidarCheckout(Long empresaId, Long tituloId, Session session) {
        if (empresaId == null || tituloId == null) return;

        paymentRepository.findByEmpresaIdAndCheckoutSessionId(empresaId, session.getId()).ifPresent(payment -> {
            if (!"PAID".equals(payment.getStatus())) {
                BigDecimal valor = BigDecimal.valueOf(session.getAmountTotal(), 2).setScale(2, RoundingMode.HALF_UP);
                tituloService.baixar(empresaId, tituloId,
                    new BaixaRequest(null, null, null, LocalDate.now(), valor,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Stripe Checkout " + session.getId()));
                payment.setStatus("PAID");
                payment.setPaymentIntentId(session.getPaymentIntent());
                paymentRepository.save(payment);
            }
        });
    }

    private void liquidarInvoice(Long empresaId, Long tituloId, Invoice invoice) {
        if (empresaId == null || tituloId == null) return;

        paymentRepository.findByEmpresaIdAndInvoiceId(empresaId, invoice.getId()).ifPresent(payment -> {
            if (!"PAID".equals(payment.getStatus())) {
                BigDecimal valor = BigDecimal.valueOf(invoice.getAmountPaid(), 2).setScale(2, RoundingMode.HALF_UP);
                tituloService.baixar(empresaId, tituloId,
                    new BaixaRequest(null, null, null, LocalDate.now(), valor,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Stripe Invoice " + invoice.getId()));
                payment.setStatus("PAID");
                paymentRepository.save(payment);
            }
        });
    }

    private void atualizarCheckout(Long empresaId, String sessionId, String status, String paymentIntentId) {
        if (empresaId == null) return;

        paymentRepository.findByEmpresaIdAndCheckoutSessionId(empresaId, sessionId).ifPresent(payment -> {
            payment.setStatus(status);
            payment.setPaymentIntentId(paymentIntentId);
            paymentRepository.save(payment);
        });
    }

    private void atualizarInvoice(Long empresaId, String invoiceId, String status) {
        if (empresaId == null) return;

        paymentRepository.findByEmpresaIdAndInvoiceId(empresaId, invoiceId).ifPresent(payment -> {
            payment.setStatus(status);
            paymentRepository.save(payment);
        });
    }

    private void atualizarPaymentIntent(Long empresaId, String paymentIntentId, String status) {
        if (empresaId == null) return;

        paymentRepository.findByEmpresaIdAndPaymentIntentId(empresaId, paymentIntentId).ifPresent(payment -> {
            payment.setStatus(status);
            paymentRepository.save(payment);
        });
    }

    private String obterOuCriarCustomer(Long empresaId, Pessoa pessoa) {
        return customerRepository.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(empresaId, pessoa.getId())
            .map(StripeCustomerMapping::getStripeCustomerId)
            .orElseGet(() -> criarCustomer(empresaId, pessoa));
    }

    private String criarCustomer(Long empresaId, Pessoa pessoa) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(pessoa.getNome())
                .setEmail(pessoa.getEmail())
                .putMetadata("empresaId", String.valueOf(empresaId))
                .putMetadata("pessoaId", String.valueOf(pessoa.getId()))
                .putMetadata("erp", "brasil-sass-erp")
                .build();

            var customer = client().v1().customers().create(
                params,
                RequestOptions.builder()
                    .setIdempotencyKey("erp-customer-" + empresaId + "-" + pessoa.getId())
                    .build());

            StripeCustomerMapping mapping = new StripeCustomerMapping();
            mapping.setEmpresaId(empresaId);
            mapping.setPessoaId(pessoa.getId());
            mapping.setStripeCustomerId(customer.getId());
            customerRepository.save(mapping);
            return customer.getId();
        } catch (StripeException e) {
            throw new IllegalStateException("Falha ao criar Customer Stripe: " + e.getMessage(), e);
        }
    }

    private Pessoa obterPessoa(Long empresaId, Long pessoaId) {
        if (pessoaId == null) {
            throw new IllegalArgumentException("Título sem pessoa vinculada");
        }

        Pessoa pessoa = pessoaRepository.findById(pessoaId)
            .orElseThrow(() -> new IllegalArgumentException("Pessoa do título não encontrada"));

        if (!empresaId.equals(pessoa.getEmpresaId())) {
            throw new IllegalArgumentException("Pessoa não pertence à empresa corrente");
        }
        return pessoa;
    }

    private void validarTituloReceber(Titulo titulo) {
        if (!"R".equalsIgnoreCase(titulo.getTipo())) {
            throw new IllegalArgumentException("Stripe só pode cobrar títulos a receber");
        }
        if (titulo.getValorSaldo() == null || titulo.getValorSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Título sem saldo para cobrança");
        }
        if ("CANCELADO".equalsIgnoreCase(titulo.getStatus())
            || "PENDENTE_APROVACAO".equalsIgnoreCase(titulo.getStatus())) {
            throw new IllegalArgumentException("Título não está liberado para cobrança");
        }
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }

    private Long metadataLong(Map<String, String> metadata, String key) {
        String value = metadata.get(key);
        if (value == null || value.isBlank()) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
