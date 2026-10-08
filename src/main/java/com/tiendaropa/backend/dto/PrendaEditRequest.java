package com.tiendaropa.backend.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Edición parcial de una prenda ya existente: solo se actualizan los campos
 * que vengan distintos de null, así que el administrador puede mandar solo
 * lo que cambió.
 *
 * fotosUrl: si viene (no null), REEMPLAZA el set completo de fotos de la
 * prenda por esta lista, en ese orden (la primera queda como portada). Si
 * no viene, las fotos actuales no se tocan. No puede venir vacía.
 *
 * variantes: si viene (no null), REEMPLAZA el set completo de variantes de
 * talla: las que traigan "id" se actualizan, las que no traigan "id" se
 * crean, y las existentes que no aparezcan en la lista se eliminan. Si no
 * viene, las variantes actuales no se tocan. No puede venir vacía.
 */
@Getter
@Setter
public class PrendaEditRequest {

    private String nombre;

    private String categoria;

    private String descripcion;

    private BigDecimal precio;

    private BigDecimal precioOferta;

    private BigDecimal costo;

    private List<String> fotosUrl;

    @Valid
    private List<VarianteEditRequest> variantes;
}
