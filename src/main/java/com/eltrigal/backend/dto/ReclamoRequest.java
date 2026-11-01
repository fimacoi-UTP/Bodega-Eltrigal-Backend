package com.eltrigal.backend.dto;

import com.eltrigal.backend.entity.CanalCompra;
import com.eltrigal.backend.entity.Motivo;
import com.eltrigal.backend.entity.RelacionadoA;
import com.eltrigal.backend.entity.TipoDocumento;
import com.eltrigal.backend.entity.TipoRespuesta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la solicitud pública de registro de un reclamo o queja.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReclamoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Los nombres no pueden exceder los 120 caracteres")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 120, message = "El apellido paterno no puede exceder los 120 caracteres")
    private String apellidoPaterno;

    @Size(max = 120, message = "El apellido materno no puede exceder los 120 caracteres")
    private String apellidoMaterno;

    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    private String numeroDocumento;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;

    private TipoRespuesta tipoRespuesta;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede exceder los 150 caracteres")
    private String correo;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    private String direccion;

    @Size(max = 150, message = "La urbanización no puede exceder los 150 caracteres")
    private String urbanizacion;

    @Size(max = 100, message = "El departamento no puede exceder los 100 caracteres")
    private String departamento;

    @Size(max = 100, message = "La provincia no puede exceder los 100 caracteres")
    private String provincia;

    @Size(max = 100, message = "El distrito no puede exceder los 100 caracteres")
    private String distrito;

    private Boolean menorDeEdad;

    private CanalCompra canalCompra;

    @NotNull(message = "El motivo (RECLAMO o QUEJA) es obligatorio")
    private Motivo motivo;

    @NotNull(message = "El campo relacionadoA (PRODUCTO o SERVICIO) es obligatorio")
    private RelacionadoA relacionadoA;

    @Size(max = 50, message = "El número de pedido no puede exceder los 50 caracteres")
    private String numeroPedido;

    @Size(max = 500, message = "La descripción del bien no puede exceder los 500 caracteres")
    private String descripcionBien;

    @NotBlank(message = "El detalle del reclamo es obligatorio")
    @Size(max = 4000, message = "El detalle no puede exceder los 4000 caracteres")
    private String detalle;

    @NotBlank(message = "El requerimiento o pedido concreto es obligatorio")
    private String requerimiento;

    private BigDecimal montoReclamado;
}

