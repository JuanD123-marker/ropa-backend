package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * El inventario real vive aquí: una prenda tiene una fila por cada
 * combinación de talla/color que maneja, con su propio stock.
 */
@Entity
@Table(
    name = "variante_talla",
    uniqueConstraints = @UniqueConstraint(columnNames = {"prenda_id", "talla", "color"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VarianteTalla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "prenda_id", nullable = false)
    private Prenda prenda;

    /**
     * Texto libre, no un catálogo fijo: "S", "M", "L", "XL", "Única", o
     * cualquier talla que el administrador necesite escribir (confirmado
     * 6 oct. 2026 — la tienda puede vender cosas con tallas no estándar,
     * como gorras con talla única).
     */
    @Column(nullable = false)
    private String talla;

    /** Puede quedar vacío si la prenda no maneja variantes de color. */
    private String color;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;
}
