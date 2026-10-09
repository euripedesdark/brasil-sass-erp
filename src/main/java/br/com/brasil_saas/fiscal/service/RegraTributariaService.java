package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.fiscal.model.RegraTributaria;
import br.com.brasil_saas.fiscal.repository.RegraTributariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegraTributariaService {

    private final RegraTributariaRepository repository;

    public List<RegraTributaria> listar(Long empresaId) {
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderByPrioridadeDescIdDesc(empresaId);
    }

    public RegraTributaria buscar(Long empresaId, Long id) {
        return repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regra não encontrada"));
    }

    @Transactional
    public RegraTributaria salvar(Long empresaId, RegraTributaria r) {
        if (r.getId() != null) {
            RegraTributaria atual = buscar(empresaId, r.getId());
            atual.setNome(r.getNome());
            atual.setNcm(normalizar(r.getNcm()));
            atual.setCfop(normalizar(r.getCfop()));
            atual.setUfOrigem(normalizar(r.getUfOrigem()));
            atual.setUfDestino(normalizar(r.getUfDestino()));
            atual.setCstIcms(r.getCstIcms());
            atual.setAliquotaIcms(r.getAliquotaIcms());
            atual.setCstIpi(r.getCstIpi());
            atual.setAliquotaIpi(r.getAliquotaIpi());
            atual.setCstPis(r.getCstPis());
            atual.setAliquotaPis(r.getAliquotaPis());
            atual.setCstCofins(r.getCstCofins());
            atual.setAliquotaCofins(r.getAliquotaCofins());
            atual.setAliquotaSt(r.getAliquotaSt());
            atual.setMva(r.getMva());
            atual.setAliquotaFcp(r.getAliquotaFcp());
            atual.setAliquotaInterna(r.getAliquotaInterna());
            atual.setReducaoBasePct(r.getReducaoBasePct());
            atual.setAtiva(r.getAtiva() == null || r.getAtiva());
            atual.setPrioridade(r.getPrioridade() == null ? 0 : r.getPrioridade());
            return repository.save(atual);
        }
        r.setEmpresaId(empresaId);
        if (r.getUuid() == null) r.setUuid(UUID.randomUUID());
        r.setNcm(normalizar(r.getNcm()));
        r.setCfop(normalizar(r.getCfop()));
        r.setUfOrigem(normalizar(r.getUfOrigem()));
        r.setUfDestino(normalizar(r.getUfDestino()));
        if (r.getAtiva() == null) r.setAtiva(true);
        if (r.getPrioridade() == null) r.setPrioridade(0);
        return repository.save(r);
    }

    @Transactional
    public void excluir(Long empresaId, Long id) {
        RegraTributaria r = buscar(empresaId, id);
        r.setDeletedAt(LocalDateTime.now());
        r.setAtiva(false);
        repository.save(r);
    }

    public RegraTributaria resolver(Empresa empresa, Produto produto) {
        return resolver(empresa, produto, null);
    }

    public RegraTributaria resolver(Empresa empresa, Produto produto, String ufDestino) {
        if (empresa == null || empresa.getId() == null || produto == null) {
            throw new IllegalArgumentException("Empresa e produto obrigatórios para resolver tributação");
        }
        String ncm = normalizar(produto.getNcm());
        String cfop = normalizar(produto.getCfopPadrao());
        return repository.buscarRegra(empresa.getId(), ncm, cfop, normalizar(empresa.getUf()), normalizar(ufDestino))
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma regra tributária ativa para NCM=" + ncm + ", CFOP=" + cfop));
    }

    public Optional<RegraTributaria> resolverOpcional(Long empresaId, String ncm, String cfop,
                                                     String ufOrigem, String ufDestino) {
        return repository.buscarRegra(empresaId, normalizar(ncm), normalizar(cfop),
                normalizar(ufOrigem), normalizar(ufDestino));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }
}
