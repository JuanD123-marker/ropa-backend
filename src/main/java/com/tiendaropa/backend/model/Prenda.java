package com.tiendaropa.backend.model;

import com.tiendaropa.backend.model.enums.EstadoPrenda;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "prenda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @Column(nullable = false)
    private String nombre;

    private String categoria;

    @Column(length = 2000)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    /** Precio con descuento; si es null, no hay oferta activa. */
    @Column(precision = 12, scale = 2)
    private BigDecimal precioOferta;

    /**
     * Costo de la prenda para el administrador. Es OPCIONAL: si queda en null,
     * los reportes de finanzas muestran el ingreso bruto de esa prenda en vez
     * de un margen calculado.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPrenda estado = EstadoPrenda.BORRADOR;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant fechaCreacion;
}
