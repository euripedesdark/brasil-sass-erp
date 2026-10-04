package br.com.brasil_saas.rh.service;
import br.com.brasil_saas.rh.model.EsocialEvento;
import br.com.brasil_saas.rh.repository.EsocialEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service @RequiredArgsConstructor
public class EsocialFilaService {
    private final EsocialEventoRepository repo;
    private final RestTemplate rest = new RestTemplate();
    private String baseUrl() {
        String v = System.getenv("ESOCIAL_URL");
        return v == null || v.isBlank() ? "http://localhost:8081" : v;
    }
    private <T> T exigir(Optional<T> o, String msg) {
        return o.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg));
    }
    public List<EsocialEvento> listar(Long empresaId, String status) {
        if (status == null || status.isBlank()) return repo.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId);
        return repo.findByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, status);
    }
    @Transactional public EsocialEvento registrar(Long empresaId, String tipo, Long funcionarioId, String payload) {
        List<String> tipos = List.of("S-2200", "S-1200", "S-1210", "S-2299", "S-3000");
        if (tipos.contains(tipo) == false) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Tipo invalido");
        EsocialEvento e = new EsocialEvento();
        e.setTipo(tipo);
        e.setFuncionarioId(funcionarioId);
        e.setPayload(payload);
        e.setStatus("PENDENTE");
        return repo.save(e);
    }
    @Transactional public EsocialEvento transmitir(Long empresaId, Long id) {
        EsocialEvento e = exigir(repo.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Evento inexistente");
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tipo", e.getTipo());
        corpo.put("funcionarioId", e.getFuncionarioId());
        corpo.put("payload", e.getPayload());
        try {
            Map<?, ?> resp = rest.postForObject(baseUrl() + "/ocorrencias", corpo, Map.class);
            e.setStatus("ENVIADO");
            if (resp != null && resp.get("protocolo") != null) e.setProtocolo(String.valueOf(resp.get("protocolo")));
            if (resp != null && resp.get("recibo") != null) e.setRecibo(String.valueOf(resp.get("recibo")));
            e.setErro(null);
        } catch (Exception ex) {
            e.setStatus("FALHA");
            String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            e.setErro(msg.length() > 2000 ? msg.substring(0, 2000) : msg);
        }
        return repo.save(e);
    }
    @Transactional public void excluir(Long empresaId, Long id) {
        EsocialEvento e = exigir(repo.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Evento inexistente");
        if ("ENVIADO".equals(e.getStatus())) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Evento ja enviado");
        repo.delete(e);
    }
}
