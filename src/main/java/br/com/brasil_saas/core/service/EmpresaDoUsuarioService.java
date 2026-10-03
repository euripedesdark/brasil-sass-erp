package br.com.brasil_saas.core.service;

import br.com.brasil_saas.core.controller.MinhaEmpresaController.EmpresaRequest;
import br.com.brasil_saas.core.controller.MinhaEmpresaController.SituacaoResponse;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.core.repository.UsuarioRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Empresa do usuario.
 *
 * Empresa e tenant: e o filtro de todos os dados do sistema. Sem ela o usuario
 * existe, faz login e nao ve nada — ate cadastrar a empresa dele.
 */
@Service
@RequiredArgsConstructor
public class EmpresaDoUsuarioService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public SituacaoResponse situacao(AuthenticatedUser user) {
        if (user == null || user.getId() == null) {
            return new SituacaoResponse(false, null, null, null);
        }
        // O empresaId do token pode estar desatualizado (a empresa foi criada
        // depois do login), entao a fonte da verdade e o usuario no banco.
        Long empresaId = usuarioRepository.findEmpresaIdById(user.getId()).orElse(null);

        if (empresaId == null) {
            return new SituacaoResponse(false, null, null, null);
        }
        return empresaRepository.findById(empresaId)
                .map(e -> new SituacaoResponse(true, e.getId(), e.getRazaoSocial(), e.getCnpj()))
                .orElseGet(() -> new SituacaoResponse(false, null, null, null));
    }

    @Transactional
    public SituacaoResponse cadastrar(EmpresaRequest request, AuthenticatedUser user) {
        Long usuarioId = usuarioId(user);
        if (empresaDoUsuario(usuarioId) != null) {
            // Criar empresa por este caminho quando ja existe uma seria uma
            // forma de o usuario trocar de tenant pelo navegador.
            throw new BusinessException("Voce ja tem uma empresa cadastrada");
        }

        String cnpj = digitos(request.cnpj());
        empresaRepository.findByCnpj(cnpj).ifPresent(e -> {
            throw new BusinessException("Ja existe uma empresa cadastrada com este CNPJ");
        });

        Empresa empresa = new Empresa();
        aplicar(empresa, request, cnpj);
        empresa = empresaRepository.save(empresa);

        usuarioRepository.vincularEmpresa(usuarioId, empresa.getId());

        return new SituacaoResponse(true, empresa.getId(),
                empresa.getRazaoSocial(), empresa.getCnpj());
    }

    @Transactional
    public SituacaoResponse atualizar(EmpresaRequest request, AuthenticatedUser user) {
        Long usuarioId = usuarioId(user);
        Long empresaId = empresaDoUsuario(usuarioId);
        if (empresaId == null) {
            throw new ResourceNotFoundException("Nenhuma empresa cadastrada para este usuario");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));

        String cnpj = digitos(request.cnpj());
        empresaRepository.findByCnpj(cnpj)
                .filter(outra -> !outra.getId().equals(empresaId))
                .ifPresent(outra -> {
                    throw new BusinessException("Este CNPJ pertence a outra empresa do sistema");
                });

        aplicar(empresa, request, cnpj);
        empresa = empresaRepository.save(empresa);

        return new SituacaoResponse(true, empresa.getId(),
                empresa.getRazaoSocial(), empresa.getCnpj());
    }

    // ------------------------------------------------------------------ apoio

    private Long empresaDoUsuario(Long usuarioId) {
        return usuarioRepository.findEmpresaIdById(usuarioId).orElse(null);
    }

    private static Long usuarioId(AuthenticatedUser user) {
        if (user == null || user.getId() == null) {
            throw new BusinessException("Usuario nao autenticado");
        }
        return user.getId();
    }

    private static String digitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private static void aplicar(Empresa e, EmpresaRequest r, String cnpj) {
        e.setRazaoSocial(r.razaoSocial().trim());
        e.setNomeFantasia(vazioParaNulo(r.nomeFantasia()));
        e.setCnpj(cnpj);
        e.setInscricaoEstadual(vazioParaNulo(r.inscricaoEstadual()));
        e.setInscricaoMunicipal(vazioParaNulo(r.inscricaoMunicipal()));
        e.setRegimeTributario(r.regimeTributario() == null || r.regimeTributario().isBlank()
                ? "SIMPLES_NACIONAL" : r.regimeTributario().trim().toUpperCase(Locale.ROOT));
        e.setCodigoIbge(vazioParaNulo(r.codigoIbge()));
        e.setEndereco(vazioParaNulo(r.endereco()));
        e.setNumero(vazioParaNulo(r.numero()));
        e.setComplemento(vazioParaNulo(r.complemento()));
        e.setBairro(vazioParaNulo(r.bairro()));
        e.setUf(upper(vazioParaNulo(r.uf())));
        e.setCep(vazioParaNulo(r.cep()));
        e.setTelefone(vazioParaNulo(r.telefone()));
        if (e.getStatus() == null || e.getStatus().isBlank()) {
            e.setStatus("ATIVA");
        }
    }

    private static String vazioParaNulo(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String upper(String v) {
        return v == null ? null : v.toUpperCase(Locale.ROOT);
    }
}
