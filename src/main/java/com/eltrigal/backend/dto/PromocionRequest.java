package com.eltrigal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para la creación y actualización de una promoción con relaciones M:N.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionRequest {

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El tipo de promoción es obligatorio")
    private String tipo;

    private BigDecimal valor;

    private Boolean aplicaTodos;

    private Integer prioridad;

    private Boolean esAcumulable;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Boolean activo;

    private List<Long> productoIds;

    private List<Integer> categoriaIds;

}
