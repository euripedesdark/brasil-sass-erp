package br.com.brasil_saas.core.service;

import br.com.brasil_saas.core.model.DocumentoFluxo;
import br.com.brasil_saas.core.repository.DocumentoFluxoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentoFluxoService {

    private final DocumentoFluxoRepository repo;

    @Transactional
    public DocumentoFluxo ligar(Long empresaId, Long userId,
                                String origemTipo, Long origemId, String origemNumero,
                                String destinoTipo, Long destinoId, String destinoNumero,
                                String relacao) {
        DocumentoFluxo f = new DocumentoFluxo();
        f.setEmpresaId(empresaId);
        f.setOrigemTipo(origemTipo);
        f.setOrigemId(origemId);
        f.setOrigemNumero(origemNumero);
        f.setDestinoTipo(destinoTipo);
        f.setDestinoId(destinoId);
        f.setDestinoNumero(destinoNumero);
        f.setRelacao(relacao == null || relacao.isBlank() ? "GERA" : relacao);
        f.setCreatedAt(LocalDateTime.now());
        f.setCreatedBy(userId);
        return repo.save(f);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> fluxo(Long empresaId, String tipo, Long id) {
        List<DocumentoFluxo> links = repo.porDocumento(empresaId, tipo, id);
        List<Map<String, Object>> nos = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        nos.add(no(tipo, id, null, null, null));
        seen.add(tipo + ":" + id);

        for (DocumentoFluxo l : links) {
            String ok = l.getOrigemTipo() + ":" + l.getOrigemId();
            String dk = l.getDestinoTipo() + ":" + l.getDestinoId();
            if (seen.add(ok)) {
                nos.add(no(l.getOrigemTipo(), l.getOrigemId(), l.getOrigemNumero(), null, null));
            }
            if (seen.add(dk)) {
                nos.add(no(l.getDestinoTipo(), l.getDestinoId(), l.getDestinoNumero(), l.getRelacao(), l.getCreatedAt()));
            }
        }

        List<Map<String, Object>> arestas = new ArrayList<>();
        for (DocumentoFluxo l : links) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("origemTipo", l.getOrigemTipo());
            a.put("origemId", l.getOrigemId());
            a.put("origemNumero", l.getOrigemNumero());
            a.put("destinoTipo", l.getDestinoTipo());
            a.put("destinoId", l.getDestinoId());
            a.put("destinoNumero", l.getDestinoNumero());
            a.put("relacao", l.getRelacao());
            a.put("createdAt", l.getCreatedAt());
            arestas.add(a);
        }

        List<Map<String, Object>> out = new ArrayList<>();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("documentoTipo", tipo);
        root.put("documentoId", id);
        root.put("nos", nos);
        root.put("ligacoes", arestas);
        out.add(root);
        return out;
    }

    @Transactional(readOnly = true)
    public List<DocumentoFluxo> ligacoes(Long empresaId, String tipo, Long id) {
        return repo.porDocumento(empresaId, tipo, id);
    }

    private Map<String, Object> no(String tipo, Long id, String numero, String via, Object em) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tipo", tipo);
        m.put("id", id);
        m.put("numero", numero);
        if (via != null) m.put("via", via);
        if (em != null) m.put("em", em);
        return m;
    }
}
