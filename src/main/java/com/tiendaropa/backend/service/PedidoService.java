package com.tiendaropa.backend.service;

import com.tiendaropa.backend.dto.ItemPedidoRequest;
import com.tiendaropa.backend.dto.ItemPedidoResponse;
import com.tiendaropa.backend.dto.ItemVentaFisicaRequest;
import com.tiendaropa.backend.dto.PedidoRequest;
import com.tiendaropa.backend.dto.PedidoResponse;
import com.tiendaropa.backend.dto.VentaFisicaRequest;
import com.tiendaropa.backend.model.*;
import com.tiendaropa.backend.model.enums.CanalVenta;
import com.tiendaropa.backend.model.enums.EstadoPedido;
import com.tiendaropa.backend.model.enums.TipoEntrega;
import com.tiendaropa.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Un pedido en línea y una venta de tienda física comparten la misma tabla
 * (Pedido/ItemPedido): lo único que cambia es el canal, si hay comprador y
 * dirección, y el estado inicial. Así el inventario y las finanzas siempre
 * se leen de un solo lugar.
 *
 * Igual que PrendaService: los controladores nunca reciben la entidad
 * Pedido/ItemPedido directamente (tienen relaciones lazy que no se deben
 * serializar tal cual) — todo pasa por {@link #aRespuesta(Pedido)}. Los
 * métodos de solo lectura son @Transactional(readOnly = true) porque
 * aRespuesta navega relaciones lazy (variante → prenda) que necesitan la
 * sesión de Hibernate todavía abierta.
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final VarianteTallaRepository varianteTallaRepository;
    private final TiendaRepository tiendaRepository;
    private final CompradorRepository compradorRepository;

    @Transactional
    public PedidoResponse crearPedidoEnLinea(Long tiendaId, Long compradorIdOpcional, PedidoRequest request) {
        if (request.getTipoEntrega() == TipoEntrega.DOMICILIO_PROPIO
                && (request.getDireccionEnvio() == null || request.getDireccionEnvio().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la dirección de envío");
        }

        Tienda tienda = tiendaRepository.findById(tiendaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        Comprador comprador = null;
        if (compradorIdOpcional != null) {
            comprador = compradorRepository.findById(compradorIdOpcional)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprador no encontrado"));
        }

        Pedido pedido = Pedido.builder()
                .tienda(tienda)
                .comprador(comprador)
                .nombreContacto(request.getNombreContacto())
                .correoContacto(request.getCorreoContacto())
                .telefonoContacto(request.getTelefonoContacto())
                .tipoEntrega(request.getTipoEntrega())
                .direccionEnvio(request.getDireccionEnvio())
                .estado(EstadoPedido.CONFIRMADO)
                .canal(CanalVenta.EN_LINEA)
                .build();
        pedido = pedidoRepository.save(pedido);

        crearItemsDeCatalogoYDescontarStock(pedido, request.getItems());
        return aRespuesta(pedido);
    }

    /**
     * El administrador registra aquí lo que se vendió en el local (sin
     * comprador ni envío, ya entregado). Cada línea puede ser una variante
     * del inventario (descuenta stock) o una "venta suelta" sin catalogar
     * (nombre/precio escritos a mano, no toca el inventario) — ver
     * {@link ItemVentaFisicaRequest}.
     */
    @Transactional
    public PedidoResponse registrarVentaFisica(Long tiendaId, VentaFisicaRequest request) {
        Tienda tienda = tiendaRepository.findById(tiendaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));

        Pedido pedido = Pedido.builder()
                .tienda(tienda)
                .comprador(null)
                .tipoEntrega(null)
                .estado(EstadoPedido.ENTREGADO)
                .canal(CanalVenta.TIENDA_FISICA)
                .metodoPago(request.getMetodoPago())
                .build();
        pedido = pedidoRepository.save(pedido);

        for (ItemVentaFisicaRequest itemReq : request.getItems()) {
            boolean tieneVariante = itemReq.getVarianteId() != null;
            boolean tieneNombreLibre = itemReq.getNombreLibre() != null && !itemReq.getNombreLibre().isBlank();

            if (tieneVariante == tieneNombreLibre) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada línea de la venta debe ser una prenda del inventario O una venta suelta con nombre, no ambas ni ninguna.");
            }

            if (tieneVariante) {
                VarianteTalla variante = varianteTallaRepository.findById(itemReq.getVarianteId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));

                if (variante.getStock() < itemReq.getCantidad()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "No hay stock suficiente para " + variante.getPrenda().getNombre()
                                    + " (talla " + variante.getTalla() + ")");
                }

                variante.setStock(variante.getStock() - itemReq.getCantidad());
                varianteTallaRepository.save(variante);

                Prenda prenda = variante.getPrenda();
                itemPedidoRepository.save(ItemPedido.builder()
                        .pedido(pedido)
                        .variante(variante)
                        .cantidad(itemReq.getCantidad())
                        .precioUnitario(prenda.getPrecioOferta() != null ? prenda.getPrecioOferta() : prenda.getPrecio())
                        .costoUnitario(prenda.getCosto())
                        .build());
            } else {
                if (itemReq.getPrecioUnitario() == null || itemReq.getPrecioUnitario().signum() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "\"" + itemReq.getNombreLibre() + "\" necesita un precio mayor a 0.");
                }

                itemPedidoRepository.save(ItemPedido.builder()
                        .pedido(pedido)
                        .variante(null)
                        .nombreLibre(itemReq.getNombreLibre().trim())
                        .cantidad(itemReq.getCantidad())
                        .precioUnitario(itemReq.getPrecioUnitario())
                        .costoUnitario(itemReq.getCostoUnitario())
                        .build());
            }
        }

        return aRespuesta(pedido);
    }

    /** Solo para checkout en línea: ahí todo item SÍ tiene que ser una variante real del catálogo. */
    private void crearItemsDeCatalogoYDescontarStock(Pedido pedido, List<ItemPedidoRequest> items) {
        for (ItemPedidoRequest itemReq : items) {
            VarianteTalla variante = varianteTallaRepository.findById(itemReq.getVarianteId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));

            if (variante.getStock() < itemReq.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay stock suficiente para " + variante.getPrenda().getNombre()
                                + " (talla " + variante.getTalla() + ")");
            }

            variante.setStock(variante.getStock() - itemReq.getCantidad());
            varianteTallaRepository.save(variante);

            Prenda prenda = variante.getPrenda();
            itemPedidoRepository.save(ItemPedido.builder()
                    .pedido(pedido)
                    .variante(variante)
                    .cantidad(itemReq.getCantidad())
                    .precioUnitario(prenda.getPrecioOferta() != null ? prenda.getPrecioOferta() : prenda.getPrecio())
                    .costoUnitario(prenda.getCosto())
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> historialDeLaTienda(Long tiendaId) {
        return pedidoRepository.findByTiendaIdOrderByFechaCreacionDesc(tiendaId)
                .stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> historialDelComprador(Long compradorId) {
        return pedidoRepository.findByCompradorIdOrderByFechaCreacionDesc(compradorId)
                .stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        pedido.setEstado(nuevoEstado);
        pedido = pedidoRepository.save(pedido);
        return aRespuesta(pedido);
    }

    /**
     * Arma el DTO plano de un pedido con sus items ya resueltos (nombre/talla/color
     * de cada línea). Si el item no tiene variante (venta suelta), usa su
     * nombreLibre y deja talla/color/varianteId en null.
     */
    private PedidoResponse aRespuesta(Pedido pedido) {
        List<ItemPedidoResponse> items = itemPedidoRepository.findByPedidoId(pedido.getId())
                .stream()
                .map(item -> {
                    VarianteTalla variante = item.getVariante();
                    if (variante != null) {
                        return ItemPedidoResponse.builder()
                                .id(item.getId())
                                .varianteId(variante.getId())
                                .prendaNombre(variante.getPrenda().getNombre())
                                .talla(variante.getTalla())
                                .color(variante.getColor())
                                .cantidad(item.getCantidad())
                                .precioUnitario(item.getPrecioUnitario())
                                .costoUnitario(item.getCostoUnitario())
                                .build();
                    }
                    return ItemPedidoResponse.builder()
                            .id(item.getId())
                            .varianteId(null)
                            .prendaNombre(item.getNombreLibre())
                            .talla(null)
                            .color(null)
                            .cantidad(item.getCantidad())
                            .precioUnitario(item.getPrecioUnitario())
                            .costoUnitario(item.getCostoUnitario())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PedidoResponse.builder()
                .id(pedido.getId())
                .nombreContacto(pedido.getNombreContacto())
                .correoContacto(pedido.getCorreoContacto())
                .telefonoContacto(pedido.getTelefonoContacto())
                .tipoEntrega(pedido.getTipoEntrega())
                .direccionEnvio(pedido.getDireccionEnvio())
                .estado(pedido.getEstado())
                .canal(pedido.getCanal())
                .metodoPago(pedido.getMetodoPago())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .total(total)
                .items(items)
                .build();
    }
}
