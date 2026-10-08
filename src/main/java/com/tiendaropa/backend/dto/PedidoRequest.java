package com.tiendaropa.backend.dto;

import com.tiendaropa.backend.model.enums.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Checkout en línea. Sirve tanto para un comprador con sesión (el
 * controller asocia el comprador_id desde el token, si viene) como para
 * un invitado (solo con estos datos de contacto).
 */
@Getter
@Setter
public class PedidoRequest {

    @NotNull
    private TipoEntrega tipoEntrega;

    /** Requerida si tipoEntrega = DOMICILIO_PROPIO. */
    private String direccionEnvio;

    private String nombreContacto;

    @Email
    private String correoContacto;

    private String telefonoContacto;

    @NotEmpty
    @Valid
    private List<ItemPedidoRequest> items;
}
