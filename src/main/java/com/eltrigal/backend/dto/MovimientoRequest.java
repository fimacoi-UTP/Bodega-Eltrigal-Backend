package com.eltrigal.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar un movimiento o ajuste de inventario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequest {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser como mínimo 1")
    private Integer cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private Long idUsuario;

}
