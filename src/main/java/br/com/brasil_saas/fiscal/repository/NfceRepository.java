package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Nfce;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NfceRepository extends JpaRepository<Nfce, Long> {
    Optional<Nfce> findByEmpresaIdAndChaveAcesso(Long empresaId, String chaveAcesso);
}
