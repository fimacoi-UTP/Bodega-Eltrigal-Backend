package com.eltrigal.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la creación y actualización de un producto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequest {

    @NotNull(message = "El id de la categoría es obligatorio")
    private Integer categoriaId;

    private Integer marcaId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String codigoBarras;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0.0")
    private BigDecimal precio;

    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private Integer stock;

    private Integer stockMinimo;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    private String imagen;

    private Boolean visibleWeb;

}
