package com.eltrigal.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para registrar una venta en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaRequest {

    @NotNull(message = "El id de usuario es obligatorio")
    private Long idUsuario;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private BigDecimal montoRecibido;

    private String referenciaPago;

    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Valid
    private List<ItemVentaRequest> items;

}
