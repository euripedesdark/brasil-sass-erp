package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    Optional<Deposito> findFirstByEmpresaIdAndTipoAndAtivoTrueOrderByIdAsc(Long empresaId, String tipo);
    Optional<Deposito> findFirstByEmpresaIdAndAtivoTrueOrderByIdAsc(Long empresaId);
    List<Deposito> findByEmpresaIdAndAtivoTrueOrderByNomeAsc(Long empresaId);
    Optional<Deposito> findByEmpresaIdAndCodigoAndAtivoTrue(Long empresaId, String codigo);
    Optional<Deposito> findByIdAndEmpresaIdAndAtivoTrue(Long id, Long empresaId);
}