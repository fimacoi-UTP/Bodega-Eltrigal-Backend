package com.eltrigal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear (u obtener si ya existe) una marca por nombre.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaRequest {

    @NotBlank(message = "El nombre de la marca es obligatorio")
    private String nombre;

}
