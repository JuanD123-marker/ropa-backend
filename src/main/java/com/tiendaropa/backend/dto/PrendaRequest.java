package com.tiendaropa.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que llega al crear/publicar una prenda. Las urls de fotos ya vienen
 * procesadas (fondo profesional aplicado) desde el paso anterior del flujo
 * de subida; este endpoint solo las asocia a la prenda.
 */
@Getter
@Setter
public class PrendaRequest {

    @NotBlank
    private String nombre;

    private String categoria;

    private String descripcion;

    @NotNull
    @Positive
    private BigDecimal precio;

    /** Opcional: si viene, es el precio con descuento. */
    private BigDecimal precioOferta;

    /** Opcional: si no se registra, los reportes de finanzas no calculan margen para esta prenda. */
    private BigDecimal costo;

    private List<String> fotosUrl;

    @NotEmpty(message = "La prenda debe tener al menos una variante de talla")
    @Valid
    private List<VarianteRequest> variantes;
}
