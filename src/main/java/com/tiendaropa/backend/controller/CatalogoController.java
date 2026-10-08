package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.PrendaResponse;
import com.tiendaropa.backend.service.PrendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Público: cualquiera puede navegar el catálogo sin iniciar sesión.
 * tiendaId por ahora se recibe como parámetro porque solo hay una tienda
 * activa; cuando haya varias, esto se resuelve por subdominio o slug.
 */
@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final PrendaService prendaService;

    @GetMapping("/prendas")
    public List<PrendaResponse> prendasPublicadas(@RequestParam Long tiendaId) {
        return prendaService.listarPublicadas(tiendaId);
    }
}
