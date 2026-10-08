package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.VarianteTalla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VarianteTallaRepository extends JpaRepository<VarianteTalla, Long> {
    List<VarianteTalla> findByPrendaId(Long prendaId);
    List<VarianteTalla> findByPrendaIdAndStockLessThanEqual(Long prendaId, int umbral);
}
