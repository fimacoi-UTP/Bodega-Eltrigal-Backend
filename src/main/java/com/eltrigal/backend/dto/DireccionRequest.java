package com.eltrigal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación o actualización de una dirección de entrega.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionRequest {

    @NotBlank(message = "La etiqueta es obligatoria")
    private String etiqueta;

    private String destinatario;

    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    private String referencia;

    private String distrito;

    private String ciudad;

    private Boolean predeterminada;

}
