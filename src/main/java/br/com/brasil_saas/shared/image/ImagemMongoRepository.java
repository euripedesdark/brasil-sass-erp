package br.com.brasil_saas.shared.image;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagemMongoRepository extends MongoRepository<ImagemDocumento, String> {
    Optional<ImagemDocumento> findByEmpresaIdAndTipoEntidadeAndEntidadeId(Long empresaId, String tipoEntidade, Long entidadeId);

    /**
     * findFirst (e nao find) de proposito: a pasta de icones legados contem
     * centenas de .bmp com conteudo identico, entao varios registros podem
     * compartilhar o mesmo hash. A migracao so precisa saber se o conteudo
     * ja foi guardado alguma vez.
     */
    Optional<ImagemDocumento> findFirstByHash(String hash);

    List<ImagemDocumento> findByTipoEntidade(String tipoEntidade);

    List<ImagemDocumento> findByTipoEntidadeAndEntidadeId(String tipoEntidade, Long entidadeId);

    void deleteByEmpresaIdAndTipoEntidadeAndEntidadeId(Long empresaId, String tipoEntidade, Long entidadeId);

    void deleteByTipoEntidade(String tipoEntidade);
}