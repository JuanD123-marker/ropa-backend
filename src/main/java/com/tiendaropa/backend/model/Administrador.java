package com.tiendaropa.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** Usuario del lado de la tienda. Login completamente separado del comprador. */
@Entity
@Table(name = "administrador", uniqueConstraints = @UniqueConstraint(columnNames = "correo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String correo;

    /** Hash de la contraseña (BCrypt) — nunca se guarda en texto plano. */
    @Column(nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant fechaCreacion;
}
