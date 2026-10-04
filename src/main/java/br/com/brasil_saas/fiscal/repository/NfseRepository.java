package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Nfse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NfseRepository extends JpaRepository<Nfse, Long> {
    Optional<Nfse> findByEmpresaIdAndCodigoVerificacao(Long empresaId, String codigoVerificacao);

    java.util.List<Nfse> findAllByEmpresaIdAndDeletedAtIsNullOrderByDataEmissaoDesc(Long empresaId);

    /**
     * Busca a nota pelo id <b>e</b> pela empresa.
     *
     * <p>Existe porque o id sozinho nao protege nada. Os ids sao sequenciais e
     * previsiveis, entao um ADMIN de uma empresa que adivinhe o id da nota de
     * outra consegue baixar o XML, o PDF, ler o retorno da prefeitura e
     * cancelar a nota. O filtro por tenant na query — e nao depois, no Java — e
     * o que impede: se o id for de outra empresa, a query nao acha.
     */
    Optional<Nfse> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    java.util.Optional<Nfse> findTop1ByEmpresaIdOrderByIdDesc(Long empresaId);
}
