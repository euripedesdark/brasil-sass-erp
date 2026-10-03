package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.AIConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIConfigRepository extends JpaRepository<AIConfig, Long> {

    Optional<AIConfig> findByEmpresaId(Long empresaId);

    Boolean existsByEmpresaId(Long empresaId);

    /**
     * A configuração ativa da empresa, para o Assistente ERP escolher o modelo.
     *
     * <p>Existe para que trocar de modelo seja um {@code UPDATE} em
     * {@code bc_ia_config.default_model} e não uma mudança em código — que é o
     * que permite ir de um modelo para outro sem alterar o resto do ERP.
     * Ordenado por id porque pode haver mais de uma linha ativa: vale a mais
     * antiga, que é a que alguém cadastrou primeiro.
     */
    Optional<AIConfig> findFirstByEmpresaIdAndIsEnabledTrueOrderByIdAsc(Long empresaId);
}
