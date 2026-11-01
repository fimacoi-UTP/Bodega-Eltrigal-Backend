package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.DashboardKpiResponse;
import com.eltrigal.backend.dto.PedidosEstadoKpiResponse;
import com.eltrigal.backend.dto.TopProductoResponse;
import com.eltrigal.backend.dto.VentasPorCategoriaResponse;
import com.eltrigal.backend.dto.VentasPorDiaResponse;
import com.eltrigal.backend.dto.VentasPorMetodoPagoResponse;
import com.eltrigal.backend.repository.DetallePedidoRepository;
import com.eltrigal.backend.repository.DetalleVentaRepository;
import com.eltrigal.backend.repository.PedidoRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import com.eltrigal.backend.repository.VentaRepository;
import com.eltrigal.backend.repository.projection.CategoriaTotalProjection;
import com.eltrigal.backend.repository.projection.EstadoCantidadProjection;
import com.eltrigal.backend.repository.projection.MetodoPagoTotalProjection;
import com.eltrigal.backend.repository.projection.ProductoTotalProjection;
import com.eltrigal.backend.repository.projection.VentasPorDiaProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de negocio para la agregación de métricas y analítica del dashboard.
 * Realiza las operaciones analíticas a nivel de base de datos combinando ventas POS y pedidos web.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final PedidoRepository pedidoRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;

    /**
     * Calcula los KPIs principales del negocio:
     * - Ventas de hoy (monto y cantidad)
     * - Ventas del mes actual (monto y cantidad)
     * - Ticket promedio del mes
     * - Conteo de pedidos desglosados por estado
     * - Productos con existencias en stock bajo
     */
    public DashboardKpiResponse obtenerKpis() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.plusDays(1).atStartOfDay();

        LocalDate primerDiaMes = hoy.withDayOfMonth(1);
        LocalDateTime inicioMes = primerDiaMes.atStartOfDay();
        LocalDateTime finMes = primerDiaMes.plusMonths(1).atStartOfDay();

        // 1. Ventas de hoy
        BigDecimal montoVentasHoy = ventaRepository.sumTotalBetween(inicioHoy, finHoy);
        BigDecimal montoPedidosHoy = pedidoRepository.sumTotalBetween(inicioHoy, finHoy);
        BigDecimal ventasHoy = montoVentasHoy.add(montoPedidosHoy).setScale(2, RoundingMode.HALF_UP);

        long cantVentasHoy = ventaRepository.countBetween(inicioHoy, finHoy);
        long cantPedidosHoy = pedidoRepository.countBetween(inicioHoy, finHoy);
        long cantidadVentasHoy = cantVentasHoy + cantPedidosHoy;

        // 2. Ventas del mes
        BigDecimal montoVentasMes = ventaRepository.sumTotalBetween(inicioMes, finMes);
        BigDecimal montoPedidosMes = pedidoRepository.sumTotalBetween(inicioMes, finMes);
        BigDecimal ventasMes = montoVentasMes.add(montoPedidosMes).setScale(2, RoundingMode.HALF_UP);

        long cantVentasMes = ventaRepository.countBetween(inicioMes, finMes);
        long cantPedidosMes = pedidoRepository.countBetween(inicioMes, finMes);
        long cantidadVentasMes = cantVentasMes + cantPedidosMes;

        // 3. Ticket promedio del mes
        BigDecimal ticketPromedio = cantidadVentasMes > 0
                ? ventasMes.divide(BigDecimal.valueOf(cantidadVentasMes), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // 4. Productos con stock bajo
        long productosStockBajo = productoRepository.countStockBajo();

        // 5. Pedidos por estado
        List<EstadoCantidadProjection> estados = pedidoRepository.countPedidosPorEstado();
        Map<String, Long> mapaEstados = new HashMap<>();
        long totalPedidos = 0;
        for (EstadoCantidadProjection e : estados) {
            String est = e.getEstado() != null ? e.getEstado().toUpperCase().trim() : "DESCONOCIDO";
            long cant = e.getCantidad() != null ? e.getCantidad() : 0L;
            mapaEstados.put(est, cant);
            totalPedidos += cant;
        }

        long pendientes = mapaEstados.getOrDefault("PENDIENTE", 0L);
        long confirmados = mapaEstados.getOrDefault("CONFIRMADO", 0L);
        long enPreparacion = mapaEstados.getOrDefault("EN_PREPARACION", 0L);
        long enCamino = mapaEstados.getOrDefault("EN_CAMINO", 0L);
        long entregados = mapaEstados.getOrDefault("ENTREGADO", 0L);
        long cancelados = mapaEstados.getOrDefault("CANCELADO", 0L);

        PedidosEstadoKpiResponse pedidosKpi = PedidosEstadoKpiResponse.builder()
                .pendientes(pendientes)
                .confirmados(confirmados)
                .enPreparacion(enPreparacion)
                .enCamino(enCamino)
                .entregados(entregados)
                .cancelados(cancelados)
                .total(totalPedidos)
                .build();

        return DashboardKpiResponse.builder()
                .ventasHoy(ventasHoy)
                .cantidadVentasHoy(cantidadVentasHoy)
                .ventasMes(ventasMes)
                .cantidadVentasMes(cantidadVentasMes)
                .ticketPromedio(ticketPromedio)
                .productosStockBajo(productosStockBajo)
                .pedidosPendientes(pendientes)
                .pedidosEnCamino(enCamino)
                .pedidosEntregados(entregados)
                .pedidosPorEstado(pedidosKpi)
                .build();
    }

    /**
     * Obtiene la serie cronológica de ventas agrupadas por día para los últimos N días.
     * Completa los días sin ventas con 0.00 para alimentar correctamente gráficos de línea o barras.
     */
    public List<VentasPorDiaResponse> obtenerVentasPorDia(int dias) {
        if (dias <= 0) {
            dias = 7;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate desdeFecha = hoy.minusDays(dias - 1);
        LocalDateTime inicio = desdeFecha.atStartOfDay();

        List<VentasPorDiaProjection> ventasPorDia = ventaRepository.sumVentasPorDia(inicio);
        List<VentasPorDiaProjection> pedidosPorDia = pedidoRepository.sumPedidosPorDia(inicio);

        Map<LocalDate, DaySummary> mapaDias = new HashMap<>();

        for (VentasPorDiaProjection v : ventasPorDia) {
            if (v.getFecha() != null) {
                LocalDate fecha = v.getFecha().toLocalDate();
                DaySummary s = mapaDias.computeIfAbsent(fecha, k -> new DaySummary());
                if (v.getTotal() != null) {
                    s.total = s.total.add(v.getTotal());
                }
                if (v.getCantidad() != null) {
                    s.cantidad += v.getCantidad();
                }
            }
        }

        for (VentasPorDiaProjection p : pedidosPorDia) {
            if (p.getFecha() != null) {
                LocalDate fecha = p.getFecha().toLocalDate();
                DaySummary s = mapaDias.computeIfAbsent(fecha, k -> new DaySummary());
                if (p.getTotal() != null) {
                    s.total = s.total.add(p.getTotal());
                }
                if (p.getCantidad() != null) {
                    s.cantidad += p.getCantidad();
                }
            }
        }

        List<VentasPorDiaResponse> resultado = new ArrayList<>(dias);
        for (LocalDate d = desdeFecha; !d.isAfter(hoy); d = d.plusDays(1)) {
            DaySummary s = mapaDias.get(d);
            resultado.add(VentasPorDiaResponse.builder()
                    .fecha(d)
                    .total(s != null ? s.total.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .cantidad(s != null ? s.cantidad : 0L)
                    .build());
        }

        return resultado;
    }

    /**
     * Obtiene la distribución de ventas por categoría para gráficos circulares.
     */
    public List<VentasPorCategoriaResponse> obtenerVentasPorCategoria() {
        List<CategoriaTotalProjection> ventasCat = detalleVentaRepository.sumVentasPorCategoria();
        List<CategoriaTotalProjection> pedidosCat = detallePedidoRepository.sumPedidosPorCategoria();

        Map<String, BigDecimal> mapaCategorias = new HashMap<>();

        for (CategoriaTotalProjection v : ventasCat) {
            if (v.getCategoria() != null && v.getTotal() != null) {
                mapaCategorias.merge(v.getCategoria(), v.getTotal(), BigDecimal::add);
            }
        }

        for (CategoriaTotalProjection p : pedidosCat) {
            if (p.getCategoria() != null && p.getTotal() != null) {
                mapaCategorias.merge(p.getCategoria(), p.getTotal(), BigDecimal::add);
            }
        }

        return mapaCategorias.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(e -> VentasPorCategoriaResponse.builder()
                        .categoria(e.getKey())
                        .total(e.getValue().setScale(2, RoundingMode.HALF_UP))
                        .build())
                .sorted(Comparator.comparing(VentasPorCategoriaResponse::getTotal).reversed())
                .toList();
    }

    /**
     * Obtiene el ranking de los productos más vendidos considerando ventas en tienda y pedidos web.
     */
    public List<TopProductoResponse> obtenerTopProductos(int limit) {
        if (limit <= 0) {
            limit = 5;
        }

        List<ProductoTotalProjection> ventasProd = detalleVentaRepository.sumVentasPorProducto();
        List<ProductoTotalProjection> pedidosProd = detallePedidoRepository.sumPedidosPorProducto();

        Map<Long, ProductSummary> mapaProductos = new HashMap<>();

        for (ProductoTotalProjection v : ventasProd) {
            if (v.getProductoId() != null) {
                ProductSummary s = mapaProductos.computeIfAbsent(v.getProductoId(), k -> new ProductSummary(v.getProductoId(), v.getProducto()));
                if (v.getCantidad() != null) {
                    s.cantidad += v.getCantidad();
                }
                if (v.getTotal() != null) {
                    s.total = s.total.add(v.getTotal());
                }
            }
        }

        for (ProductoTotalProjection p : pedidosProd) {
            if (p.getProductoId() != null) {
                ProductSummary s = mapaProductos.computeIfAbsent(p.getProductoId(), k -> new ProductSummary(p.getProductoId(), p.getProducto()));
                if (p.getCantidad() != null) {
                    s.cantidad += p.getCantidad();
                }
                if (p.getTotal() != null) {
                    s.total = s.total.add(p.getTotal());
                }
            }
        }

        return mapaProductos.values().stream()
                .sorted(Comparator.comparing(ProductSummary::getCantidad).reversed()
                        .thenComparing(ProductSummary::getTotal, Comparator.reverseOrder()))
                .limit(limit)
                .map(s -> TopProductoResponse.builder()
                        .id(s.id)
                        .producto(s.nombre)
                        .cantidad(s.cantidad)
                        .total(s.total.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();
    }

    /**
     * Obtiene la distribución de ingresos por método de pago (EFECTIVO, YAPE, PLIN, TARJETA, etc.).
     */
    public List<VentasPorMetodoPagoResponse> obtenerVentasPorMetodoPago() {
        List<MetodoPagoTotalProjection> ventasMet = ventaRepository.sumVentasPorMetodoPago();
        List<MetodoPagoTotalProjection> pedidosMet = pedidoRepository.sumPedidosPorMetodoPago();

        Map<String, MetodoPagoSummary> mapaMetodos = new HashMap<>();

        for (MetodoPagoTotalProjection v : ventasMet) {
            String metodo = normalizarMetodoPago(v.getMetodo());
            MetodoPagoSummary s = mapaMetodos.computeIfAbsent(metodo, MetodoPagoSummary::new);
            if (v.getTotal() != null) {
                s.total = s.total.add(v.getTotal());
            }
            if (v.getCantidad() != null) {
                s.cantidad += v.getCantidad();
            }
        }

        for (MetodoPagoTotalProjection p : pedidosMet) {
            String metodo = normalizarMetodoPago(p.getMetodo());
            MetodoPagoSummary s = mapaMetodos.computeIfAbsent(metodo, MetodoPagoSummary::new);
            if (p.getTotal() != null) {
                s.total = s.total.add(p.getTotal());
            }
            if (p.getCantidad() != null) {
                s.cantidad += p.getCantidad();
            }
        }

        return mapaMetodos.values().stream()
                .filter(s -> s.total.compareTo(BigDecimal.ZERO) > 0 || s.cantidad > 0)
                .map(s -> VentasPorMetodoPagoResponse.builder()
                        .metodo(s.metodo)
                        .total(s.total.setScale(2, RoundingMode.HALF_UP))
                        .cantidad(s.cantidad)
                        .build())
                .sorted(Comparator.comparing(VentasPorMetodoPagoResponse::getTotal).reversed())
                .toList();
    }

    private String normalizarMetodoPago(String metodo) {
        if (metodo == null || metodo.isBlank()) {
            return "OTROS";
        }
        return metodo.trim().toUpperCase();
    }

    // Helper interno mutable para acumular totales por día
    private static class DaySummary {
        BigDecimal total = BigDecimal.ZERO;
        long cantidad = 0;
    }

    // Helper interno mutable para acumular ventas por producto
    private static class ProductSummary {
        final Long id;
        final String nombre;
        long cantidad = 0;
        BigDecimal total = BigDecimal.ZERO;

        ProductSummary(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        long getCantidad() {
            return cantidad;
        }

        BigDecimal getTotal() {
            return total;
        }
    }

    // Helper interno mutable para acumular ventas por método de pago
    private static class MetodoPagoSummary {
        final String metodo;
        BigDecimal total = BigDecimal.ZERO;
        long cantidad = 0;

        MetodoPagoSummary(String metodo) {
            this.metodo = metodo;
        }

        BigDecimal getTotal() {
            return total;
        }
    }
}

