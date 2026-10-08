package com.tiendaropa.backend.dto;

import com.tiendaropa.backend.model.enums.TipoUsuario;

public record LoginResponse(String token, TipoUsuario tipo) {
}
