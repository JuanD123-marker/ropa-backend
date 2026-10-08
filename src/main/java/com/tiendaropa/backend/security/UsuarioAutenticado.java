package com.tiendaropa.backend.security;

import com.tiendaropa.backend.model.enums.TipoUsuario;
import lombok.Getter;

/** Lo que queda disponible en el contexto de seguridad tras validar el JWT. */
@Getter
public class UsuarioAutenticado {
    private final Long id;
    private final TipoUsuario tipo;
    private final Long tiendaId;

    public UsuarioAutenticado(Long id, TipoUsuario tipo, Long tiendaId) {
        this.id = id;
        this.tipo = tipo;
        this.tiendaId = tiendaId;
    }
}
