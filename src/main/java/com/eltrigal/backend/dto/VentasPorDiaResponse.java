package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la serie cronológica de ventas por día.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentasPorDiaResponse {

    private LocalDate fecha;

    private BigDecimal total;

    private Long cantidad;
}

