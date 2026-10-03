package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Aprovacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AprovacaoRepository extends JpaRepository<Aprovacao, Long> {

    List<Aprovacao> findByEmpresaIdAndStatusOrderByDataSolicitacaoAsc(Long empresaId, String status);

    List<Aprovacao> findByTituloIdAndDeletedAtIsNullOrderByNivelAsc(Long tituloId);

    Optional<Aprovacao> findByTituloIdAndStatusAndNivelAndDeletedAtIsNull(Long tituloId, String status, Integer nivel);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aprovacao a where a.id = :id and a.empresaId = :empresaId and a.deletedAt is null")
    Optional<Aprovacao> findForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("select a from Aprovacao a where a.empresaId = :empresaId and a.status = 'PENDENTE' " +
           "and (:usuarioId is null or a.usuarioAprovadorId is null or a.usuarioAprovadorId = :usuarioId) " +
           "and a.deletedAt is null order by a.nivel asc, a.dataSolicitacao asc")
    List<Aprovacao> findPendentesParaUsuario(@Param("empresaId") Long empresaId, @Param("usuarioId") Long usuarioId);
}
