package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Baixa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaixaRepository extends JpaRepository<Baixa, Long> {
    List<Baixa> findByTituloIdAndDeletedAtIsNull(Long tituloId);

    @org.springframework.data.jpa.repository.Query("select b from Baixa b where b.empresaId = :empresaId and b.contaBancariaId = :contaBancariaId and b.dataBaixa between :inicio and :fim and b.deletedAt is null order by b.dataBaixa desc, b.id desc")
    List<Baixa> findByContaPeriodo(Long empresaId, Long contaBancariaId, java.time.LocalDate inicio, java.time.LocalDate fim);
}
