package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta detallada de administración de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAdminResponse {

    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String rol;
    private Boolean activo;
    private String avatarUrl;
    private LocalDateTime creadoEn;

}
