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
 * DTO para registrar un pedido en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequest {

    @NotNull(message = "El id de usuario es obligatorio")
    private Long idUsuario;

    @NotBlank(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega;

    private String direccionEntrega;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String referenciaPago;

    private BigDecimal costoEnvio;

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    @Valid
    private List<ItemPedidoRequest> items;

}
