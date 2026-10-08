package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.PedidoResponse;
import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Requiere ROLE_COMPRADOR: solo para quien sí se registró (ver el historial es la razón principal para hacerlo). */
@RestController
@RequestMapping("/api/comprador/pedidos")
@RequiredArgsConstructor
public class CompradorPedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public List<PedidoResponse> misPedidos(@AuthenticationPrincipal UsuarioAutenticado comprador) {
        return pedidoService.historialDelComprador(comprador.getId());
    }
}
