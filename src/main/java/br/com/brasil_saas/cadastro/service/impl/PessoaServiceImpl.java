package br.com.brasil_saas.cadastro.service.impl;

import br.com.brasil_saas.cadastro.dto.*;
import br.com.brasil_saas.cadastro.model.*;
import br.com.brasil_saas.cadastro.repository.*;
import br.com.brasil_saas.cadastro.service.PessoaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PessoaServiceImpl implements PessoaService {

    private final PessoaRepository pessoaRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContatoRepository contatoRepository;
    private final MunicipioRepository municipioRepository;

    @Override
    @Transactional
    public PessoaResponse criar(PessoaRequest request, Long empresaId) {
        if (request.documento() != null && !request.documento().isBlank()) {
            if (pessoaRepository.existsByEmpresaIdAndDocumentoAndDeletedAtIsNull(empresaId, request.documento())) {
                throw new BusinessException("CPF_CNPJ_DUPLICADO", "Documento já cadastrado");
            }
        }

        Pessoa pessoa = new Pessoa();
        // Sem isso o INSERT fica com empresa_id nulo e o banco recusa com
        // violacao de not-null, devolvida como 409 — que parece duplicidade
        // mas e falta do tenant. Criar pessoa nunca funcionou por causa
        // deste campo.
        pessoa.setEmpresaId(empresaId);
        pessoa.setTipo(request.tipo());
        pessoa.setNome(request.nome());
        pessoa.setDocumento(request.documento());
        pessoa.setEmail(request.email());
        pessoa.setTelefone(request.telefone());
        pessoa.setStatus(request.status() != null ? request.status() : "ATIVO");
        pessoa.setObservacao(request.observacao());

        if (request.fisica() != null) {
            PessoaFisica fisica = new PessoaFisica();
            fisica.setPessoa(pessoa);
            fisica.setCpf(request.fisica().cpf());
            fisica.setRg(request.fisica().rg());
            fisica.setOrgaoExpedidor(request.fisica().orgaoExpedidor());
            fisica.setDataNascimento(request.fisica().dataNascimento());
            fisica.setSexo(request.fisica().sexo());
            fisica.setEstadoCivil(request.fisica().estadoCivil());
            pessoa.setFisica(fisica);
        }

        if (request.juridica() != null) {
            PessoaJuridica juridica = new PessoaJuridica();
            juridica.setPessoa(pessoa);
            juridica.setCnpj(request.juridica().cnpj());
            juridica.setInscricaoEstadual(request.juridica().inscricaoEstadual());
            juridica.setInscricaoMunicipal(request.juridica().inscricaoMunicipal());
            juridica.setDataAbertura(request.juridica().dataAbertura());
            juridica.setPorte(request.juridica().porte());
            juridica.setNaturezaJuridica(request.juridica().naturezaJuridica());
            pessoa.setJuridica(juridica);
        }

        if (request.enderecos() != null) {
            List<Endereco> enderecos = new ArrayList<>();
            for (EnderecoRequest er : request.enderecos()) {
                Endereco e = new Endereco();
                e.setPessoa(pessoa);
                e.setTipo(er.tipo() != null ? er.tipo() : "PRINCIPAL");
                e.setLogradouro(er.logradouro());
                e.setNumero(er.numero());
                e.setComplemento(er.complemento());
                e.setBairro(er.bairro());
                e.setCep(er.cep());
                e.setUf(er.uf());
                e.setLatitude(er.latitude());
                e.setLongitude(er.longitude());
                e.setPrincipal(er.principal() != null ? er.principal() : false);
                if (er.municipioId() != null) {
                    municipioRepository.findById(er.municipioId()).ifPresent(e::setMunicipio);
                }
                enderecos.add(e);
            }
            pessoa.setEnderecos(enderecos);
        }

        if (request.contatos() != null) {
            List<Contato> contatos = new ArrayList<>();
            for (ContatoRequest cr : request.contatos()) {
                Contato c = new Contato();
                c.setPessoa(pessoa);
                c.setTipo(cr.tipo() != null ? cr.tipo() : "PRINCIPAL");
                c.setNome(cr.nome());
                c.setEmail(cr.email());
                c.setTelefone(cr.telefone());
                c.setObservacao(cr.observacao());
                contatos.add(c);
            }
            pessoa.setContatos(contatos);
        }

        Pessoa saved = pessoaRepository.save(pessoa);
        return PessoaResponse.from(saved);
    }

    @Override
    @Transactional
    public PessoaResponse atualizar(Long empresaId, Long id, PessoaRequest request) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .filter(p -> empresaId.equals(p.getEmpresaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));

        if (request.documento() != null && !request.documento().isBlank()) {
            if (pessoaRepository.existsByEmpresaIdAndDocumentoAndIdNotAndDeletedAtIsNull(empresaId, request.documento(), id)) {
                throw new BusinessException("CPF_CNPJ_DUPLICADO", "Documento já cadastrado");
            }
        }

        pessoa.setTipo(request.tipo());
        pessoa.setNome(request.nome());
        pessoa.setDocumento(request.documento());
        pessoa.setEmail(request.email());
        pessoa.setTelefone(request.telefone());
        pessoa.setStatus(request.status());
        pessoa.setObservacao(request.observacao());

        if (request.fisica() != null) {
            PessoaFisica fisica = pessoa.getFisica() != null ? pessoa.getFisica() : new PessoaFisica();
            fisica.setPessoa(pessoa);
            fisica.setCpf(request.fisica().cpf());
            fisica.setRg(request.fisica().rg());
            fisica.setOrgaoExpedidor(request.fisica().orgaoExpedidor());
            fisica.setDataNascimento(request.fisica().dataNascimento());
            fisica.setSexo(request.fisica().sexo());
            fisica.setEstadoCivil(request.fisica().estadoCivil());
            pessoa.setFisica(fisica);
        }

        if (request.juridica() != null) {
            PessoaJuridica juridica = pessoa.getJuridica() != null ? pessoa.getJuridica() : new PessoaJuridica();
            juridica.setPessoa(pessoa);
            juridica.setCnpj(request.juridica().cnpj());
            juridica.setInscricaoEstadual(request.juridica().inscricaoEstadual());
            juridica.setInscricaoMunicipal(request.juridica().inscricaoMunicipal());
            juridica.setDataAbertura(request.juridica().dataAbertura());
            juridica.setPorte(request.juridica().porte());
            juridica.setNaturezaJuridica(request.juridica().naturezaJuridica());
            pessoa.setJuridica(juridica);
        }

        // Endereços também fazem parte do cadastro operacional. Na edição,
        // substituímos o conjunto enviado pela tela dentro da mesma transação,
        // preservando o vínculo com a Pessoa e o tenant.
        if (request.enderecos() != null) {
            pessoa.getEnderecos().clear();
            for (EnderecoRequest er : request.enderecos()) {
                Endereco e = new Endereco();
                e.setPessoa(pessoa);
                e.setEmpresaId(empresaId);
                e.setTipo(er.tipo() != null ? er.tipo() : "PRINCIPAL");
                e.setLogradouro(er.logradouro());
                e.setNumero(er.numero());
                e.setComplemento(er.complemento());
                e.setBairro(er.bairro());
                e.setCep(er.cep());
                e.setUf(er.uf());
                e.setLatitude(er.latitude());
                e.setLongitude(er.longitude());
                e.setPrincipal(er.principal() != null ? er.principal() : false);
                if (er.municipioId() != null) {
                    municipioRepository.findById(er.municipioId()).ifPresent(e::setMunicipio);
                }
                pessoa.getEnderecos().add(e);
            }
        }

        Pessoa saved = pessoaRepository.save(pessoa);
        return PessoaResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PessoaResponse buscarPorId(Long empresaId, Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .filter(p -> empresaId.equals(p.getEmpresaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));
        return PessoaResponse.from(pessoa);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PessoaResponse> listar(Long empresaId, String nome, String documento, Pageable pageable) {
        Page<Pessoa> page = pessoaRepository.buscar(empresaId, nome, documento, pageable);
        return PageResponse.from(page, PessoaResponse::from);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .filter(p -> empresaId.equals(p.getEmpresaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));
        pessoa.setDeletedAt(LocalDateTime.now());
        pessoaRepository.save(pessoa);
    }
}
