package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta con datos de un lote.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteResponse {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private String codigoLote;
    private Integer cantidadInicial;
    private Integer cantidadActual;
    private BigDecimal costoUnitario;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaIngreso;
    private Boolean activo;

}
