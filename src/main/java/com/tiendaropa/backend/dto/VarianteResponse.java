package com.tiendaropa.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VarianteResponse {
    private Long id;
    private String talla;
    private String color;
    private int stock;
}
