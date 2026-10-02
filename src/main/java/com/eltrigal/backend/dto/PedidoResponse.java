package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta completa de un pedido hacia el cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {

    private Long id;
    private String numeroPedido;
    private Long idUsuario;
    private String estado;
    private String tipoEntrega;
    private String direccionEntrega;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String metodoPago;
    private String referenciaPago;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private List<DetallePedidoResponse> items;
    private List<HistorialEstadoResponse> historial;

}
