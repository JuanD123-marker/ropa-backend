package com.tiendaropa.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** Una línea de un pedido/venta, con los datos de la prenda ya "aplanados" (nombre/talla/color). */
@Getter
@Builder
public class ItemPedidoResponse {
    private Long id;
    private Long varianteId;
    private String prendaNombre;
    private String talla;
    private String color;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal costoUnitario;
}
