package br.com.brasil_saas.producao.repository;
import br.com.brasil_saas.producao.model.OperacaoRoteiro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OperacaoRoteiroRepository extends JpaRepository<OperacaoRoteiro,Long>{
 List<OperacaoRoteiro> findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(Long empresaId,Long roteiroId);
 Optional<OperacaoRoteiro> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
}
