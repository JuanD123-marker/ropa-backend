package com.tiendaropa.backend.dto;

import java.math.BigDecimal;

/** Una fila del ranking "prendas más vendidas" del período, sin importar el canal. */
public record PrendaVendida(
        Long prendaId,
        String nombre,
        int cantidadVendida,
        BigDecimal ingresos
) {
}
