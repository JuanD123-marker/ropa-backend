package com.tiendaropa.backend.service;

import com.tiendaropa.backend.dto.CanalResumen;
import com.tiendaropa.backend.dto.PrendaVendida;
import com.tiendaropa.backend.dto.ResumenFinancieroResponse;
import com.tiendaropa.backend.dto.VentaResumen;
import com.tiendaropa.backend.model.ItemPedido;
import com.tiendaropa.backend.model.Pedido;
import com.tiendaropa.backend.model.Prenda;
import com.tiendaropa.backend.model.enums.CanalVenta;
import com.tiendaropa.backend.repository.ItemPedidoRepository;
import com.tiendaropa.backend.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * El resumen se calcula al vuelo a partir de ItemPedido — no se guarda en
 * ninguna tabla aparte, así nunca se desactualiza. Las líneas sin costo
 * registrado se cuentan aparte en vez de tratarse como margen cero o
 * como ganancia real.
 */
@Service
@RequiredArgsConstructor
public class FinanzasService {

    private static final int TOP_PRENDAS_LIMITE = 5;

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    /**
     * readOnly porque el mapeo necesita navegar item.getVariante().getPrenda()
     * (relaciones lazy) mientras la sesión de Hibernate sigue abierta — mismo
     * motivo por el que PedidoService.aRespuesta() necesita la anotación.
     */
    @Transactional(readOnly = true)
    public ResumenFinancieroResponse resumen(Long tiendaId, Instant desde, Instant hasta) {
        List<Pedido> pedidos = pedidoRepository.findByTiendaIdAndFechaCreacionBetween(tiendaId, desde, hasta);

        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal ganancia = BigDecimal.ZERO;
        long itemsSinCosto = 0;

        BigDecimal ingresosEnLinea = BigDecimal.ZERO;
        BigDecimal gananciaEnLinea = BigDecimal.ZERO;
        long pedidosEnLinea = 0;

        BigDecimal ingresosFisica = BigDecimal.ZERO;
        BigDecimal gananciaFisica = BigDecimal.ZERO;
        long pedidosFisica = 0;

        List<VentaResumen> ventas = new ArrayList<>();
        Map<String, AcumuladorPrenda> porPrenda = new LinkedHashMap<>();

        for (Pedido pedido : pedidos) {
            List<ItemPedido> items = itemPedidoRepository.findByPedidoId(pedido.getId());

            BigDecimal subtotalPedido = BigDecimal.ZERO;
            BigDecimal gananciaPedido = BigDecimal.ZERO;

            for (ItemPedido item : items) {
                BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
                ingresos = ingresos.add(subtotal);
                subtotalPedido = subtotalPedido.add(subtotal);

                if (item.getCostoUnitario() != null) {
                    BigDecimal costoTotal = item.getCostoUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
                    BigDecimal gananciaItem = subtotal.subtract(costoTotal);
                    ganancia = ganancia.add(gananciaItem);
                    gananciaPedido = gananciaPedido.add(gananciaItem);
                } else {
                    itemsSinCosto++;
                }

                // Las ventas sueltas (sin variante) no están en el inventario, pero igual
                // cuentan en el ranking de más vendidas, agrupadas por su nombre libre.
                Prenda prenda = item.getVariante() != null ? item.getVariante().getPrenda() : null;
                String clave = prenda != null ? "p" + prenda.getId() : "l" + item.getNombreLibre();
                Long prendaId = prenda != null ? prenda.getId() : null;
                String nombre = prenda != null ? prenda.getNombre() : item.getNombreLibre();

                AcumuladorPrenda acumulador = porPrenda.computeIfAbsent(
                        clave, k -> new AcumuladorPrenda(prendaId, nombre));
                acumulador.cantidad += item.getCantidad();
                acumulador.ingresos = acumulador.ingresos.add(subtotal);
            }

            if (pedido.getCanal() == CanalVenta.EN_LINEA) {
                ingresosEnLinea = ingresosEnLinea.add(subtotalPedido);
                gananciaEnLinea = gananciaEnLinea.add(gananciaPedido);
                pedidosEnLinea++;
            } else {
                ingresosFisica = ingresosFisica.add(subtotalPedido);
                gananciaFisica = gananciaFisica.add(gananciaPedido);
                pedidosFisica++;
            }

            ventas.add(new VentaResumen(
                    pedido.getId(), pedido.getCanal(), pedido.getMetodoPago(), pedido.getFechaCreacion(),
                    subtotalPedido, items.size()));
        }

        ventas.sort(Comparator.comparing(VentaResumen::fecha).reversed());

        List<PrendaVendida> topPrendas = porPrenda.values().stream()
                .map(a -> new PrendaVendida(a.prendaId, a.nombre, a.cantidad, a.ingresos))
                .sorted(Comparator.comparingInt(PrendaVendida::cantidadVendida).reversed())
                .limit(TOP_PRENDAS_LIMITE)
                .collect(Collectors.toList());

        return new ResumenFinancieroResponse(
                ingresos,
                ganancia,
                itemsSinCosto,
                pedidos.size(),
                new CanalResumen(ingresosEnLinea, gananciaEnLinea, pedidosEnLinea),
                new CanalResumen(ingresosFisica, gananciaFisica, pedidosFisica),
                ventas,
                topPrendas
        );
    }

    /** Acumulador interno mientras se recorren los items; no sale de este servicio. prendaId queda null para ventas sueltas. */
    private static class AcumuladorPrenda {
        final Long prendaId;
        final String nombre;
        int cantidad = 0;
        BigDecimal ingresos = BigDecimal.ZERO;

        AcumuladorPrenda(Long prendaId, String nombre) {
            this.prendaId = prendaId;
            this.nombre = nombre;
        }
    }
}
