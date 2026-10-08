package com.tiendaropa.backend.model.enums;

/** Solo se pide hoy para ventas de tienda física; un pedido en línea no lo tiene todavía (no hay pasarela de pagos conectada). */
public enum MetodoPago {
    EFECTIVO,
    TRANSFERENCIA
}
