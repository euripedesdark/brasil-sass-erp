package br.com.brasil_saas.financeiro.service;
import br.com.brasil_saas.financeiro.model.Emprestimo;
import br.com.brasil_saas.financeiro.repository.EmprestimoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service @RequiredArgsConstructor
public class EmprestimoService {
    private final EmprestimoRepository repo;
    private <T> T exigir(Optional<T> o, String msg) {
        return o.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg));
    }
    public List<Emprestimo> listar(Long empresaId) { return repo.findByEmpresaIdAndDeletedAtIsNullOrderByDataContratacaoDesc(empresaId); }
    @Transactional public Emprestimo salvar(Long empresaId, Emprestimo e) {
        e.setId(null);
        if (e.getStatus() == null) e.setStatus("ATIVO");
        return repo.save(e);
    }
    @Transactional public Emprestimo quitar(Long empresaId, Long id) {
        Emprestimo e = exigir(repo.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Emprestimo inexistente");
        e.setStatus("QUITADO");
        e.setDataQuitacao(LocalDate.now());
        return repo.save(e);
    }
    @Transactional public void excluir(Long empresaId, Long id) {
        repo.delete(exigir(repo.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Emprestimo inexistente"));
    }
}
