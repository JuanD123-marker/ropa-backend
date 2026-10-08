package com.tiendaropa.backend.model.enums;

/** Permite que una venta hecha en el local (sin comprador ni envío) viva en la misma tabla Pedido. */
public enum CanalVenta {
    EN_LINEA,
    TIENDA_FISICA
}
