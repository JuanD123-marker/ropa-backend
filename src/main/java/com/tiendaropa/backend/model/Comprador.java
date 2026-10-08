package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Solo existe una fila aquí si la persona decidió registrarse. Comprar como
 * invitado no crea un Comprador: los datos de contacto quedan directamente
 * en el Pedido.
 */
@Entity
@Table(name = "comprador", uniqueConstraints = @UniqueConstraint(columnNames = "correo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant fechaCreacion;
}
