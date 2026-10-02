package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta con datos de un producto hacia el cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {

    private Long id;
    private String nombre;
    private String codigoBarras;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Integer stockMinimo;
    private String unidadMedida;
    private String imagen;
    private Boolean visibleWeb;
    private Boolean activo;

    private Integer categoriaId;
    private String categoriaNombre;

    private Integer marcaId;
    private String marcaNombre;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

}
