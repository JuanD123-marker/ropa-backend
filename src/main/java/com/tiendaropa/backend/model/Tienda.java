package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Una tienda dentro de la app. Hoy solo existe una fila (la del negocio real),
 * pero se modela como entidad propia desde el inicio para poder sumar más
 * tiendas después sin rediseñar el resto del sistema.
 */
@Entity
@Table(name = "tienda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String logoUrl;

    @Column(length = 1000)
    private String descripcion;

    private String telefono;

    private String direccion;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant fechaCreacion;
}
