package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.FotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Sube una foto de prenda: la procesa (quitar fondo opcional + fondo gris
 * uniforme) y la deja en Supabase Storage. El admin junta las URLs que
 * devuelve este endpoint para armar `PrendaRequest.fotosUrl` al crear o
 * editar la prenda. Todo bajo /api/admin/** requiere ROLE_ADMINISTRADOR (ver
 * SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/fotos")
@RequiredArgsConstructor
public class AdminFotoController {

    private final FotoService fotoService;

    /** quitarFondo por defecto true; mándalo en false para dejar la foto tal cual llegó. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> subir(
            @AuthenticationPrincipal UsuarioAutenticado admin,
            @RequestParam("foto") MultipartFile foto,
            @RequestParam(value = "quitarFondo", defaultValue = "true") boolean quitarFondo) {
        String url = fotoService.procesarYSubir(admin.getTiendaId(), foto, quitarFondo);
        return Map.of("url", url);
    }
}