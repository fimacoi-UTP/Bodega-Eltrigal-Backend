package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la distribución de ventas por método de pago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentasPorMetodoPagoResponse {

    private String metodo;

    private BigDecimal total;

    private Long cantidad;
}

