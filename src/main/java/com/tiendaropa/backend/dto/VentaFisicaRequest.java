package com.tiendaropa.backend.dto;

import com.tiendaropa.backend.model.enums.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Para que el administrador registre una venta hecha en el local, no en línea. */
@Getter
@Setter
public class VentaFisicaRequest {

    @NotNull(message = "Indica si se pagó en efectivo o por transferencia")
    private MetodoPago metodoPago;

    @NotEmpty
    @Valid
    private List<ItemVentaFisicaRequest> items;
}
