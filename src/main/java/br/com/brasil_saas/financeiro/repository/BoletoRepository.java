package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Boleto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoletoRepository extends JpaRepository<Boleto, Long> {

    List<Boleto> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId);

    Optional<Boleto> findByEmpresaIdAndBancoAndNossoNumero(Long empresaId, String banco, String nossoNumero);

    /**
     * Chave de conciliacao: o retorno CNAB traz o nosso_numero so com digitos,
     * enquanto a API emite no formato com barra e DV (175/12345678-4).
     */
    Optional<Boleto> findByEmpresaIdAndBancoAndNossoNumeroChave(Long empresaId, String banco, String nossoNumeroChave);

    List<Boleto> findByEmpresaIdAndStatus(Long empresaId, String status);
}
