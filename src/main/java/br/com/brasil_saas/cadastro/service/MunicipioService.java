package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.model.Municipio;
import br.com.brasil_saas.cadastro.repository.MunicipioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MunicipioService {

    private final MunicipioRepository repository;

    public Page<Municipio> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Municipio> buscar(String codigo, String nome, String termo, Pageable pageable) {
        String codigoFiltro = normalizar(codigo);
        String nomeFiltro = normalizar(nome);
        String termoFiltro = normalizar(termo);

        if (termoFiltro != null) {
            codigoFiltro = termoFiltro;
            nomeFiltro = termoFiltro;
        }

        return repository.buscarPorFiltros(codigoFiltro, nomeFiltro, pageable);
    }

    public Optional<Municipio> buscarPorCodigo(String codigoIbge) {
        return repository.findByCodigoIbge(codigoIbge.trim());
    }

    public Optional<Municipio> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Municipio criar(Municipio municipio) {
        municipio.setId(null);
        normalizarMunicipio(municipio);
        validar(municipio);
        repository.findByCodigoIbge(municipio.getCodigoIbge()).ifPresent(existing -> {
            throw new IllegalArgumentException("Já existe município com o código IBGE " + municipio.getCodigoIbge());
        });
        if (municipio.getCreatedAt() == null) {
            municipio.setCreatedAt(LocalDateTime.now());
        }
        municipio.setUpdatedAt(LocalDateTime.now());
        return repository.save(municipio);
    }

    @Transactional
    public Municipio atualizar(Long id, Municipio dados) {
        Municipio atual = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Município não encontrado: " + id));

        normalizarMunicipio(dados);
        validar(dados);

        repository.findByCodigoIbge(dados.getCodigoIbge())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Já existe município com o código IBGE " + dados.getCodigoIbge());
                });

        atual.setCodigoIbge(dados.getCodigoIbge());
        atual.setNome(dados.getNome());
        atual.setUf(dados.getUf());
        atual.setCodigoSiafi(dados.getCodigoSiafi());
        atual.setUpdatedAt(LocalDateTime.now());

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Município não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private static void normalizarMunicipio(Municipio municipio) {
        if (municipio.getCodigoIbge() != null) {
            municipio.setCodigoIbge(municipio.getCodigoIbge().trim());
        }
        if (municipio.getNome() != null) {
            municipio.setNome(municipio.getNome().trim());
        }
        if (municipio.getUf() != null) {
            municipio.setUf(municipio.getUf().trim().toUpperCase());
        }
        if (municipio.getCodigoSiafi() != null) {
            municipio.setCodigoSiafi(municipio.getCodigoSiafi().trim());
        }
    }

    private static void validar(Municipio municipio) {
        if (municipio.getCodigoIbge() == null || municipio.getCodigoIbge().isBlank()) {
            throw new IllegalArgumentException("Código IBGE é obrigatório");
        }
        if (municipio.getNome() == null || municipio.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do município é obrigatório");
        }
        if (municipio.getUf() == null || municipio.getUf().length() != 2) {
            throw new IllegalArgumentException("UF deve possuir 2 caracteres");
        }
    }
}
