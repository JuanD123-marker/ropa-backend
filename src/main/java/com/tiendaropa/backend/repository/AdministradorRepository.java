package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
