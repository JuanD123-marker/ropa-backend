package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.PedidoResponse;
import com.tiendaropa.backend.model.enums.EstadoPedido;
import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class AdminPedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado admin) {
        return pedidoService.historialDeLaTienda(admin.getTiendaId());
    }

    @PatchMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(body.get("estado"));
        return pedidoService.cambiarEstado(id, nuevoEstado);
    }
}
