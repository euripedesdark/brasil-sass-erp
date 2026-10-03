package br.com.brasil_saas.financeiro.service;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.*;
import java.time.LocalDate; import java.util.List;
public interface TituloService {
    List<TituloResponse> listar(Long empresaId, String status);
    TituloResponse buscar(Long empresaId, Long id);
    List<ParcelaResponse> parcelas(Long empresaId, Long tituloId);
    List<ParcelaResponse> gerarParcelas(Long empresaId, Long tituloId, Long condicaoPagamentoId);
    BaixaResponse baixar(Long empresaId, Long tituloId, BaixaRequest request);
    List<ParcelaResponse> vencimentos(Long empresaId, LocalDate inicio, LocalDate fim);
}
