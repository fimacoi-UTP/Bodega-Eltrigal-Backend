package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la respuesta con datos del detalle de una venta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVentaResponse {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal precioOriginal;
    private BigDecimal subtotal;

}
