package br.com.brasil_saas.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/** Vinculo usuario <-> modulo, com marcacao de somente leitura. */
@Entity
@Table(name = "bc_core_usuario_modulo", schema = "brasil_saas")
@Getter
@Setter
@IdClass(UsuarioModuloId.class)
public class UsuarioModulo {

    @Id
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Id
    @Column(name = "modulo_id", nullable = false)
    private Long moduloId;

    @Column(name = "somente_leitura", nullable = false)
    private Boolean somenteLeitura = false;

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Immutable
    private LocalDateTime criadoEm;
}
