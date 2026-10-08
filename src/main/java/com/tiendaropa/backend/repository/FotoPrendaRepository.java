package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.FotoPrenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FotoPrendaRepository extends JpaRepository<FotoPrenda, Long> {
    List<FotoPrenda> findByPrendaIdOrderByOrdenAsc(Long prendaId);
}
