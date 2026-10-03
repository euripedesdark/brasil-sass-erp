package br.com.brasil_saas.portais.service;
import br.com.brasil_saas.portais.model.*;
import java.util.List;
import java.util.Map;
public interface PortalService {
    List<PtlAcesso> acessos(Long empresaId);
    PtlAcesso gerar(Long empresaId, Long userId, String tipo, Long pessoaId, Integer diasValidade);
    void revogar(Long empresaId, Long id);
    Map<String, Object> validar(String token);
    Map<String, Object> minhaConta(String token);
}
