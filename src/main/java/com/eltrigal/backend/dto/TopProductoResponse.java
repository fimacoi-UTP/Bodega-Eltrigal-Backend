package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para el ranking de productos más vendidos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductoResponse {

    private Long id;

    private String producto;

    private Long cantidad;

    private BigDecimal total;
}

