package com.tiendaropa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * Una variante de talla dentro de una edición de prenda. Si "id" viene, se
 * actualiza esa variante existente; si viene null, se crea una nueva.
 *
 * El set completo de variantes que llega en PrendaEditRequest.variantes
 * reemplaza al que había: las existentes que no aparezcan en la lista se
 * eliminan, a menos que ya tengan pedidos registrados, en cuyo caso la
 * edición falla con un mensaje claro en vez de romper el historial de
 * ventas (ver PrendaService.reemplazarVariantes).
 */
@Getter
@Setter
public class VarianteEditRequest {

    private Long id;

    @NotBlank(message = "La talla no puede estar vacía")
    private String talla;

    private String color;

    @PositiveOrZero
    private int stock;
}
