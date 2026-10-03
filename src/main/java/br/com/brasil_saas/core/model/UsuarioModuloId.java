package br.com.brasil_saas.core.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Chave composta de {@link UsuarioModulo}. */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioModuloId implements Serializable {

    private Long usuarioId;
    private Long moduloId;
}
