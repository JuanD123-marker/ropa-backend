package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Una prenda puede tener varias fotos (ej. delantera y trasera). Cada una ya
 * pasó por el proceso de fondo profesional antes de guardarse aquí; este
 * modelo solo guarda la referencia (url), nunca el archivo en sí.
 */
@Entity
@Table(name = "foto_prenda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FotoPrenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "prenda_id", nullable = false)
    private Prenda prenda;

    @Column(nullable = false)
    private String url;

    /** Orden de aparición dentro de la ficha de la prenda. */
    @Builder.Default
    private int orden = 0;

    /** true = foto de portada; false = foto adicional (ej. la trasera). */
    @Builder.Default
    private boolean principal = false;
}
