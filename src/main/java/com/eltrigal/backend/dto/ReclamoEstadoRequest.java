package com.eltrigal.backend.dto;

import com.eltrigal.backend.entity.EstadoReclamo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización de estado y acciones tomadas por parte del administrador.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReclamoEstadoRequest {

    @NotNull(message = "El estado del reclamo es obligatorio")
    private EstadoReclamo estado;

    private String accionesTomadas;
}

