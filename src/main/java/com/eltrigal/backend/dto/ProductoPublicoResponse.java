package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la consulta pública de productos en la tienda web (storefront).
 * Expone únicamente información de catálogo sin datos sensibles como stock exacto o costos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoPublicoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioVenta;
    private String imagen;
    private String urlImagen;
    private String color;
    private String unidadMedida;
    private String codigoBarras;
    private Integer categoriaId;
    private String categoria;
    private String categoriaNombre;
    private Integer marcaId;
    private String marca;
    private String marcaNombre;
    private Boolean disponible;

}
