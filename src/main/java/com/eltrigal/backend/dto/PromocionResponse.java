package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta con datos de una promoción y sus asociaciones M:N.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocionResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private BigDecimal valor;
    private Boolean aplicaTodos;
    private Integer prioridad;
    private Boolean esAcumulable;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    private List<ProductoRef> productos;
    private List<CategoriaRef> categorias;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductoRef {
        private Long id;
        private String nombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoriaRef {
        private Integer id;
        private String nombre;
    }

}
