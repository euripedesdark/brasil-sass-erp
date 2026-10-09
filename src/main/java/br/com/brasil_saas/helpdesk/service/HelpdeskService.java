package br.com.brasil_saas.helpdesk.service;

import br.com.brasil_saas.helpdesk.model.Chamado;
import br.com.brasil_saas.helpdesk.model.ChamadoComentario;
import br.com.brasil_saas.helpdesk.repository.ChamadoComentarioRepository;
import br.com.brasil_saas.helpdesk.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HelpdeskService {

    private final ChamadoRepository chamadoRepository;
    private final ChamadoComentarioRepository comentarioRepository;

    public List<Chamado> listar(Long empresaId, String status) {
        if (status != null && !status.isBlank()) {
            return chamadoRepository.findByEmpresaIdAndStatusAndDeletedAtIsNullOrderByIdDesc(empresaId, status.trim().toUpperCase());
        }
        return chamadoRepository.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId);
    }

    public Chamado buscar(Long empresaId, Long id) {
        return chamadoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
    }

    @Transactional
    public Chamado salvar(Long empresaId, Chamado c) {
        if (c.getId() != null) {
            Chamado atual = buscar(empresaId, c.getId());
            atual.setTitulo(c.getTitulo());
            atual.setDescricao(c.getDescricao());
            atual.setPrioridade(nz(c.getPrioridade(), "MEDIA"));
            atual.setCategoria(c.getCategoria());
            atual.setSolicitante(c.getSolicitante());
            atual.setResponsavel(c.getResponsavel());
            atual.setClienteId(c.getClienteId());
            return chamadoRepository.save(atual);
        }
        c.setEmpresaId(empresaId);
        if (c.getUuid() == null) c.setUuid(UUID.randomUUID());
        c.setNumero("HD-" + System.currentTimeMillis() % 1000000);
        c.setStatus("ABERTO");
        c.setPrioridade(nz(c.getPrioridade(), "MEDIA"));
        c.setAbertoEm(LocalDateTime.now());
        return chamadoRepository.save(c);
    }

    @Transactional
    public Chamado mudarStatus(Long empresaId, Long id, String status) {
        Chamado c = buscar(empresaId, id);
        String st = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("ABERTO", "EM_ANDAMENTO", "AGUARDANDO", "RESOLVIDO", "FECHADO", "CANCELADO").contains(st)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido");
        }
        c.setStatus(st);
        if ("FECHADO".equals(st) || "RESOLVIDO".equals(st) || "CANCELADO".equals(st)) {
            c.setFechadoEm(LocalDateTime.now());
        }
        return chamadoRepository.save(c);
    }

    public List<ChamadoComentario> comentarios(Long empresaId, Long chamadoId) {
        buscar(empresaId, chamadoId);
        return comentarioRepository.findByEmpresaIdAndChamadoIdAndDeletedAtIsNullOrderByIdAsc(empresaId, chamadoId);
    }

    @Transactional
    public ChamadoComentario comentar(Long empresaId, Long chamadoId, String autor, String texto) {
        buscar(empresaId, chamadoId);
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comentário obrigatório");
        }
        ChamadoComentario c = new ChamadoComentario();
        c.setEmpresaId(empresaId);
        c.setUuid(UUID.randomUUID());
        c.setChamadoId(chamadoId);
        c.setAutor(autor);
        c.setTexto(texto.trim());
        return comentarioRepository.save(c);
    }

    public Map<String, Object> resumo(Long empresaId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("abertos", chamadoRepository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "ABERTO"));
        m.put("emAndamento", chamadoRepository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "EM_ANDAMENTO"));
        m.put("resolvidos", chamadoRepository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "RESOLVIDO"));
        m.put("fechados", chamadoRepository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "FECHADO"));
        return m;
    }

    private static String nz(String v, String d) {
        return v == null || v.isBlank() ? d : v.trim().toUpperCase();
    }
}
