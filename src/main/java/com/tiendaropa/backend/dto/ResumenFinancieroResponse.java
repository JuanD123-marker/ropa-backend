package com.tiendaropa.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * gananciaCalculable = suma de (precio - costo) solo de los items donde SÍ
 * había costo registrado. itemsSinCosto cuenta cuántas líneas no tienen
 * costo, para no hacer pasar el ingreso bruto de esas por "ganancia real".
 *
 * enLinea/fisica desglosan los mismos tres números por canal de venta.
 * ventas trae cada pedido del período (más reciente primero) para poder
 * listarlos uno por uno. topPrendas son las prendas con más unidades
 * vendidas en el período (hasta 5), sin importar el canal.
 */
public record ResumenFinancieroResponse(
        BigDecimal ingresosTotales,
        BigDecimal gananciaCalculable,
        long itemsSinCosto,
        long totalPedidos,
        CanalResumen enLinea,
        CanalResumen fisica,
        List<VentaResumen> ventas,
        List<PrendaVendida> topPrendas
) {
}
