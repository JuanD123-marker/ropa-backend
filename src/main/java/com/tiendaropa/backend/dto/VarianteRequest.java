package com.tiendaropa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VarianteRequest {

    /** Texto libre: "S", "M", "L", "Única", o cualquier talla que necesite la tienda. */
    @NotBlank(message = "La talla no puede estar vacía")
    private String talla;

    private String color;

    @PositiveOrZero
    private int stock;
}
