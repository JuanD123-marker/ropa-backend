package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.PedidoRequest;
import com.tiendaropa.backend.dto.PedidoResponse;
import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Checkout en línea. Es público (ver SecurityConfig): si viene un JWT de
 * comprador válido, el pedido queda asociado a esa cuenta; si no, se crea
 * como compra de invitado con los datos de contacto del propio request.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse crear(
            @AuthenticationPrincipal UsuarioAutenticado compradorOpcional,
            @RequestParam Long tiendaId,
            @Valid @RequestBody PedidoRequest request) {
        Long compradorId = compradorOpcional != null ? compradorOpcional.getId() : null;
        return pedidoService.crearPedidoEnLinea(tiendaId, compradorId, request);
    }
}
