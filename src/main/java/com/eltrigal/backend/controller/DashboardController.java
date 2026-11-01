package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.DashboardKpiResponse;
import com.eltrigal.backend.dto.TopProductoResponse;
import com.eltrigal.backend.dto.VentasPorCategoriaResponse;
import com.eltrigal.backend.dto.VentasPorDiaResponse;
import com.eltrigal.backend.dto.VentasPorMetodoPagoResponse;
import com.eltrigal.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para las métricas y gráficos del Dashboard.
 * Base path: /api/dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtiene los KPIs clave de negocio (ventas de hoy, mes, ticket promedio, pedidos por estado, stock bajo).
     * GET /api/dashboard/kpis -> 200 OK
     */
    @GetMapping("/kpis")
    public ResponseEntity<DashboardKpiResponse> obtenerKpis() {
        return ResponseEntity.ok(dashboardService.obtenerKpis());
    }

    /**
     * Obtiene la serie temporal de ventas por día para gráficos de línea o barras.
     * GET /api/dashboard/ventas-por-dia?dias=7 -> 200 OK
     */
    @GetMapping("/ventas-por-dia")
    public ResponseEntity<List<VentasPorDiaResponse>> obtenerVentasPorDia(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(dashboardService.obtenerVentasPorDia(dias));
    }

    /**
     * Obtiene el total de ventas agrupado por categoría para gráficos circulares / dona.
     * GET /api/dashboard/ventas-por-categoria -> 200 OK
     */
    @GetMapping("/ventas-por-categoria")
    public ResponseEntity<List<VentasPorCategoriaResponse>> obtenerVentasPorCategoria() {
        return ResponseEntity.ok(dashboardService.obtenerVentasPorCategoria());
    }

    /**
     * Obtiene el ranking de productos más vendidos.
     * GET /api/dashboard/top-productos?limit=5 -> 200 OK
     */
    @GetMapping("/top-productos")
    public ResponseEntity<List<TopProductoResponse>> obtenerTopProductos(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.obtenerTopProductos(limit));
    }

    /**
     * Obtiene el total de ventas agrupado por método de pago.
     * GET /api/dashboard/ventas-por-metodo-pago -> 200 OK
     */
    @GetMapping("/ventas-por-metodo-pago")
    public ResponseEntity<List<VentasPorMetodoPagoResponse>> obtenerVentasPorMetodoPago() {
        return ResponseEntity.ok(dashboardService.obtenerVentasPorMetodoPago());
    }
}

