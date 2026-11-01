package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO con los indicadores clave de rendimiento (KPIs) del dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpiResponse {

    private BigDecimal ventasHoy;

    private Long cantidadVentasHoy;

    private BigDecimal ventasMes;

    private Long cantidadVentasMes;

    private BigDecimal ticketPromedio;

    private Long productosStockBajo;

    private Long pedidosPendientes;

    private Long pedidosEnCamino;

    private Long pedidosEntregados;

    private PedidosEstadoKpiResponse pedidosPorEstado;
}

