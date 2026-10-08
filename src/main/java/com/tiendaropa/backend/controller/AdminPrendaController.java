package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.PrendaEditRequest;
import com.tiendaropa.backend.dto.PrendaRequest;
import com.tiendaropa.backend.dto.PrendaResponse;
import com.tiendaropa.backend.dto.VarianteResponse;
import com.tiendaropa.backend.model.enums.EstadoPrenda;
import com.tiendaropa.backend.security.UsuarioAutenticado;
import com.tiendaropa.backend.service.PrendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Todo bajo /api/admin/** requiere ROLE_ADMINISTRADOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/prendas")
@RequiredArgsConstructor
public class AdminPrendaController {

    private final PrendaService prendaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrendaResponse crear(@AuthenticationPrincipal UsuarioAutenticado admin, @Valid @RequestBody PrendaRequest request) {
        return prendaService.crear(admin.getTiendaId(), request);
    }

    @GetMapping
    public List<PrendaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado admin) {
        return prendaService.listarDeLaTienda(admin.getTiendaId());
    }

    /** Edición parcial: solo manda los campos que quieras cambiar, el resto se deja igual. */
    @PatchMapping("/{id}")
    public PrendaResponse editar(@AuthenticationPrincipal UsuarioAutenticado admin,
                          @PathVariable Long id,
                          @Valid @RequestBody PrendaEditRequest request) {
        return prendaService.editar(admin.getTiendaId(), id, request);
    }

    /** Elimina la prenda por completo (fotos y variantes incluidas). No se puede deshacer. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@AuthenticationPrincipal UsuarioAutenticado admin, @PathVariable Long id) {
        prendaService.eliminar(admin.getTiendaId(), id);
    }

    /** Pausar, volver a publicar, o marcar agotada: body {"estado": "PAUSADA"}. */
    @PatchMapping("/{id}/estado")
    public PrendaResponse cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado admin,
                                 @PathVariable Long id,
                                 @RequestBody Map<String, String> body) {
        EstadoPrenda nuevoEstado = EstadoPrenda.valueOf(body.get("estado"));
        return prendaService.cambiarEstado(admin.getTiendaId(), id, nuevoEstado);
    }

    /** Ajuste manual de stock de una variante puntual: body {"stock": 10}. */
    @PatchMapping("/variantes/{varianteId}/stock")
    public VarianteResponse ajustarStock(@AuthenticationPrincipal UsuarioAutenticado admin,
                                       @PathVariable Long varianteId,
                                       @RequestBody Map<String, Integer> body) {
        return prendaService.ajustarStock(admin.getTiendaId(), varianteId, body.get("stock"));
    }
}
