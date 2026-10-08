package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.Prenda;
import com.tiendaropa.backend.model.enums.EstadoPrenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrendaRepository extends JpaRepository<Prenda, Long> {
    List<Prenda> findByTiendaIdAndEstado(Long tiendaId, EstadoPrenda estado);
    List<Prenda> findByTiendaId(Long tiendaId);
}
