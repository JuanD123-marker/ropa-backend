package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.ResumenFinancieroResponse;
import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.FinanzasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/finanzas")
@RequiredArgsConstructor
public class AdminFinanzasController {

    private final FinanzasService finanzasService;

    /** Incluye ventas en línea y de tienda física juntas. */
    @GetMapping("/resumen")
    public ResumenFinancieroResponse resumen(
            @AuthenticationPrincipal UsuarioAutenticado admin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        return finanzasService.resumen(admin.getTiendaId(), desde, hasta);
    }
}
