package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta con los datos de una dirección de entrega.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionResponse {

    private Long id;
    private String etiqueta;
    private String destinatario;
    private String telefono;
    private String direccion;
    private String referencia;
    private String distrito;
    private String ciudad;
    private Boolean predeterminada;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

}
