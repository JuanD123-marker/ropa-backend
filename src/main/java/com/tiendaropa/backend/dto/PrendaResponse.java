package com.tiendaropa.backend.dto;

import com.tiendaropa.backend.model.enums.EstadoPrenda;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class PrendaResponse {
    private Long id;
    private String nombre;
    private String categoria;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioOferta;
    private BigDecimal costo;
    private EstadoPrenda estado;
    private Instant fechaCreacion;
    private List<String> fotosUrl;
    private List<VarianteResponse> variantes;
}
