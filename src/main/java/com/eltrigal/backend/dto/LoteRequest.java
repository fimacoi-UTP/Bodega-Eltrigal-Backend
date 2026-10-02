package com.eltrigal.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar el ingreso de un lote de producto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteRequest {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;

    private String codigoLote;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser como mínimo 1")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo unitario debe ser mayor o igual a 0.0")
    private BigDecimal costoUnitario;

    private LocalDate fechaVencimiento;

    private String motivo;

}
