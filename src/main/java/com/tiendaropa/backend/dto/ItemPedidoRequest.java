package com.tiendaropa.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemPedidoRequest {

    @NotNull
    private Long varianteId;

    @Positive
    private int cantidad;
}
