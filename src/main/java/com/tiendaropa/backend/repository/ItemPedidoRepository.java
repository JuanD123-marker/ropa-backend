package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    List<ItemPedido> findByPedidoId(Long pedidoId);

    /** Usado al eliminar/rotar prendas o variantes: encuentra qué ventas pasadas quedarían huérfanas. */
    List<ItemPedido> findByVarianteIdIn(Collection<Long> varianteIds);
}
