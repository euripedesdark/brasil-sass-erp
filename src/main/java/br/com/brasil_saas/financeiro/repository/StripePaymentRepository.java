package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.StripePayment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripePaymentRepository extends JpaRepository<StripePayment, Long> {
    Optional<StripePayment> findFirstByEmpresaIdAndTituloIdAndStatusOrderByIdDesc(Long empresaId, Long tituloId, String status);
    Optional<StripePayment> findByEmpresaIdAndCheckoutSessionId(Long empresaId, String checkoutSessionId);
    Optional<StripePayment> findByEmpresaIdAndInvoiceId(Long empresaId, String invoiceId);
    Optional<StripePayment> findByEmpresaIdAndPaymentIntentId(Long empresaId, String paymentIntentId);
}
