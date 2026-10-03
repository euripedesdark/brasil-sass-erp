package br.com.brasil_saas.shared.image;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoMongoRepository extends MongoRepository<DocumentoArquivo, String> {

    Optional<DocumentoArquivo> findByEmpresaIdAndTipoEntidadeAndEntidadeId(
            Long empresaId, String tipoEntidade, Long entidadeId);

    Optional<DocumentoArquivo> findFirstByHash(String hash);

    List<DocumentoArquivo> findByEmpresaIdAndTipoEntidade(Long empresaId, String tipoEntidade);

    /**
     * Listagem por modulo, restrita aos modulos que o usuario pode acessar.
     * Os ids de modulo chegam como chave do documento, nao como id interno —
     * por isso o filtro e por `modulo` (string) e nao por moduloId.
     */
    List<DocumentoArquivo> findByEmpresaIdAndModuloIn(Long empresaId, List<String> modulos);

    List<DocumentoArquivo> findByEmpresaId(Long empresaId);

    List<DocumentoArquivo> findByTipoEntidade(String tipoEntidade);

    /**
     * Documentos de um tipo criados antes de uma data. Usado pela limpeza por
     * prazo de guarda: o PDF da NFS-e expira em 60 dias, o XML em 5 anos.
     */
    List<DocumentoArquivo> findByTipoEntidadeAndCriadoEmBefore(String tipoEntidade, LocalDateTime limite);

    /** Quantos documentos de um tipo restam apos a limpeza, para o log. */
    long countByTipoEntidade(String tipoEntidade);

    void deleteByEmpresaIdAndTipoEntidadeAndEntidadeId(Long empresaId, String tipoEntidade, Long entidadeId);
}
