package br.com.brasil_saas.notificacoes.service;

import br.com.brasil_saas.notificacoes.model.Notificacao;
import br.com.brasil_saas.notificacoes.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacaoService {
    private final NotificacaoRepository repository;

    public List<Notificacao> listar(Long empresaId, Long usuarioId, boolean soNaoLidas) {
        if (usuarioId != null && soNaoLidas) {
            return repository.findByEmpresaIdAndUsuarioIdAndLidaFalseAndDeletedAtIsNullOrderByIdDesc(empresaId, usuarioId);
        }
        if (usuarioId != null) {
            return repository.findByEmpresaIdAndUsuarioIdAndDeletedAtIsNullOrderByIdDesc(empresaId, usuarioId);
        }
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId);
    }

    public long naoLidas(Long empresaId, Long usuarioId) {
        if (usuarioId == null) return 0;
        return repository.countByEmpresaIdAndUsuarioIdAndLidaFalseAndDeletedAtIsNull(empresaId, usuarioId);
    }

    @Transactional
    public Notificacao criar(Long empresaId, Notificacao n) {
        if (n.getTitulo() == null || n.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título obrigatório");
        }
        n.setId(null);
        n.setEmpresaId(empresaId);
        if (n.getUuid() == null) n.setUuid(UUID.randomUUID());
        if (n.getTipo() == null) n.setTipo("INFO");
        n.setLida(false);
        return repository.save(n);
    }

    @Transactional
    public Notificacao marcarLida(Long empresaId, Long id) {
        Notificacao n = repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        n.setLida(true);
        return repository.save(n);
    }

    @Transactional
    public int marcarTodasLidas(Long empresaId, Long usuarioId) {
        List<Notificacao> list = repository.findByEmpresaIdAndUsuarioIdAndLidaFalseAndDeletedAtIsNullOrderByIdDesc(empresaId, usuarioId);
        for (Notificacao n : list) {
            n.setLida(true);
            repository.save(n);
        }
        return list.size();
    }
}
