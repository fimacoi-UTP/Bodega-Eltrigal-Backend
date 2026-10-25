package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta con datos básicos de una marca (usado en selects).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaResponse {

    private Integer id;
    private String nombre;

}
