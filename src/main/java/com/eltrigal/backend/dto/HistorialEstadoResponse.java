package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta con una entrada del historial de estados de un pedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoResponse {

    private String estado;
    private LocalDateTime creadoEn;

}
