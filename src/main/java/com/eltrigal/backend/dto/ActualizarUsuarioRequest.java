package com.eltrigal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la actualización de un usuario existente desde el panel de administración.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;

}
