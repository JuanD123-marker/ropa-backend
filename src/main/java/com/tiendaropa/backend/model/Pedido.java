package com.tiendaropa.backend.model;

import com.tiendaropa.backend.model.enums.CanalVenta;
import com.tiendaropa.backend.model.enums.EstadoPedido;
import com.tiendaropa.backend.model.enums.MetodoPago;
import com.tiendaropa.backend.model.enums.TipoEntrega;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Un pedido en línea Y una venta de la tienda física son la misma entidad:
 * una venta física simplemente tiene canal = TIENDA_FISICA, sin comprador ni
 * dirección, registrada directamente como ENTREGADO por el administrador.
 * Así el inventario y las finanzas se alimentan de un solo lugar.
 */
@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    /** Null si es una compra de invitado o una venta de tienda física. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id")
    private Comprador comprador;

    private String nombreContacto;
    private String correoContacto;
    private String telefonoContacto;

    @Enumerated(EnumType.STRING)
    private TipoEntrega tipoEntrega;

    /** Solo aplica si tipoEntrega = DOMICILIO_PROPIO. */
    private String direccionEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.CONFIRMADO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalVenta canal;

    /** Solo aplica a ventas de tienda física por ahora — un pedido en línea todavía no pasa por pasarela de pagos. */
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant fechaCreacion;

    @UpdateTimestamp
    private Instant fechaActualizacion;
}
