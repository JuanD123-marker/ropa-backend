package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiendaRepository extends JpaRepository<Tienda, Long> {
}
