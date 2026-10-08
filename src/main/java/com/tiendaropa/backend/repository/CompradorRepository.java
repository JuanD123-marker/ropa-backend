package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.Comprador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompradorRepository extends JpaRepository<Comprador, Long> {
    Optional<Comprador> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
