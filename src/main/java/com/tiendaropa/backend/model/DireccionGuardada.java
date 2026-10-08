package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Direcciones que un comprador registrado guarda para no escribirlas en cada compra. */
@Entity
@Table(name = "direccion_guardada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionGuardada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Comprador comprador;

    /** Ej. "Casa", "Trabajo" — para que el comprador identifique cuál usar. */
    private String etiqueta;

    @Column(nullable = false)
    private String direccion;

    private String ciudad;

    private String telefono;

    @Builder.Default
    private boolean predeterminada = false;
}
