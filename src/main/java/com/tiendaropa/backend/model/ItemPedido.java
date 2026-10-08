package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * precioUnitario y costoUnitario son una "foto" del momento de la venta:
 * si el precio o el costo de la Prenda cambian después, las ventas ya
 * hechas no deben moverse. costoUnitario puede quedar en null si el
 * administrador no había registrado costo para esa prenda.
 *
 * variante es null en dos casos, ambos con nombreLibre trayendo el nombre
 * en texto plano en vez de una referencia viva al inventario:
 * (1) una "venta suelta" de tienda física — algo que se vendió sin estar
 * catalogado, ver {@link com.tiendaropa.backend.service.PedidoService} — o
 * (2) una venta que SÍ estaba catalogada, pero cuya prenda o variante se
 * borró después (el inventario rota) — ver
 * {@link com.tiendaropa.backend.service.PrendaService#eliminar}. En ambos
 * casos no hay stock que descontar ni variante viva que consultar.
 */
@Entity
@Table(name = "item_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** Null si es una venta suelta (nombreLibre trae el nombre en ese caso). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id")
    private VarianteTalla variante;

    /** Solo se usa cuando variante es null — nombre escrito a mano para una venta suelta. */
    private String nombreLibre;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 12, scale = 2)
    private BigDecimal costoUnitario;
}
