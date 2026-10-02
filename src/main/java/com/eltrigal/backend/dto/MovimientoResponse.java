package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta con datos de un movimiento de inventario (Kardex).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoResponse {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private Long loteId;
    private Long idUsuario;
    private String tipo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String motivo;
    private String referencia;
    private LocalDateTime creadoEn;

}
