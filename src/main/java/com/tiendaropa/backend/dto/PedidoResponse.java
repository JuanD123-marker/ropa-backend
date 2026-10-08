package com.tiendaropa.backend.dto;

import com.tiendaropa.backend.model.enums.CanalVenta;
import com.tiendaropa.backend.model.enums.EstadoPedido;
import com.tiendaropa.backend.model.enums.MetodoPago;
import com.tiendaropa.backend.model.enums.TipoEntrega;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO plano de un pedido (en línea o venta de tienda física — ver
 * {@link com.tiendaropa.backend.model.Pedido}), con sus items ya resueltos.
 * total = suma de precioUnitario * cantidad de todos los items.
 */
@Getter
@Builder
public class PedidoResponse {
    private Long id;
    private String nombreContacto;
    private String correoContacto;
    private String telefonoContacto;
    private TipoEntrega tipoEntrega;
    private String direccionEnvio;
    private EstadoPedido estado;
    private CanalVenta canal;
    private MetodoPago metodoPago;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;
    private BigDecimal total;
    private List<ItemPedidoResponse> items;
}
