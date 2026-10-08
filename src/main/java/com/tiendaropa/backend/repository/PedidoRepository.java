package com.tiendaropa.backend.repository;

import com.tiendaropa.backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByTiendaIdOrderByFechaCreacionDesc(Long tiendaId);
    List<Pedido> findByCompradorIdOrderByFechaCreacionDesc(Long compradorId);
    List<Pedido> findByTiendaIdAndFechaCreacionBetween(Long tiendaId, Instant desde, Instant hasta);
}
