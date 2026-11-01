package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la distribución de ventas por categoría.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentasPorCategoriaResponse {

    private String categoria;

    private BigDecimal total;
}

