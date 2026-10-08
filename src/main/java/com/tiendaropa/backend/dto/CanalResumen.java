package com.tiendaropa.backend.dto;

import java.math.BigDecimal;

/** Mismos tres números del resumen general, pero calculados solo para un canal (en línea o tienda física). */
public record CanalResumen(
        BigDecimal ingresos,
        BigDecimal ganancia,
        long totalPedidos
) {
}
