package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.PedidoResponse;
import com.tiendaropa.backend.dto.VentaFisicaRequest;
import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** El administrador registra aquí lo que se vendió en el local, para que inventario y finanzas queden completos. */
@RestController
@RequestMapping("/api/admin/ventas-fisicas")
@RequiredArgsConstructor
public class AdminVentaFisicaController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse registrar(@AuthenticationPrincipal UsuarioAutenticado admin, @Valid @RequestBody VentaFisicaRequest request) {
        return pedidoService.registrarVentaFisica(admin.getTiendaId(), request);
    }
}
