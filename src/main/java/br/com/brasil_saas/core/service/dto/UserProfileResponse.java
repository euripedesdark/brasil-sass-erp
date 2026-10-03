package br.com.brasil_saas.core.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String nome;
    private String username;
    private String email;
    private Long empresaId;
    private String empresaNome;
    private Set<String> perfis;
    private Set<String> permissoes;

    /**
     * Modulos liberados para este usuario.
     *
     * O menu lateral e construido no frontend a partir daqui: sem este campo
     * a tela mostraria todos os modulos e a pessoa entraria numa area sem
     * permissao, Discovering-so no 403 da API.
     */
    private Set<String> modulos;

    /** true quando o modulo esta liberado apenas para leitura. */
    private Set<String> modulosSomenteLeitura;
}
