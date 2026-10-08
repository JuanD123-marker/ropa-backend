package com.tiendaropa.backend.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Una línea de venta física es UNA de estas dos cosas:
 * - varianteId: una talla/color que ya está en el inventario (descuenta stock,
 *   el precio/costo salen de la prenda).
 * - nombreLibre + precioUnitario: algo que se vendió sin estar catalogado (no
 *   toca el inventario; costoUnitario es opcional, para que la ganancia
 *   también se pueda calcular si se conoce).
 *
 * No hay anotación de validación que exprese ese "uno u otro" — se valida a
 * mano en PedidoService, con un mensaje claro si llegan ambos o ninguno.
 */
@Getter
@Setter
public class ItemVentaFisicaRequest {

    private Long varianteId;

    private String nombreLibre;

    private BigDecimal precioUnitario;

    private BigDecimal costoUnitario;

    @Positive
    private int cantidad;
}
