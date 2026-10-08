package com.tiendaropa.backend.dto;

import com.tiendaropa.backend.model.enums.CanalVenta;
import com.tiendaropa.backend.model.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Una fila de la lista "ventas del período" en el resumen financiero — no
 * trae el detalle de items, solo lo justo para listarla. metodoPago queda
 * en null para pedidos en línea (todavía no se registra ahí).
 */
public record VentaResumen(
        Long id,
        CanalVenta canal,
        MetodoPago metodoPago,
        Instant fecha,
        BigDecimal total,
        int cantidadItems
) {
}
