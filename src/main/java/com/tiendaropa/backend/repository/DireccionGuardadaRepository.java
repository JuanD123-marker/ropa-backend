package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.DireccionGuardada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionGuardadaRepository extends JpaRepository<DireccionGuardada, Long> {
    List<DireccionGuardada> findByCompradorId(Long compradorId);
}
