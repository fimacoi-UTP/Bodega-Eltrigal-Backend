package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta con datos generales y detalles de una venta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaResponse {

    private Long id;
    private String numeroTicket;
    private Long idUsuario;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String metodoPago;
    private BigDecimal montoRecibido;
    private BigDecimal vuelto;
    private String referenciaPago;
    private Boolean anulada;
    private String motivoAnulacion;
    private LocalDateTime creadoEn;
    private List<DetalleVentaResponse> items;

}
