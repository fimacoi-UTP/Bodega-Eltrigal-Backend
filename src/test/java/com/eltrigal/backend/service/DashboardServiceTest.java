package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.DashboardKpiResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Debe calcular KPIs correctamente combinando ventas y pedidos")
    void testObtenerKpis() {
        when(ventaRepository.sumTotalBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("150.00"), new BigDecimal("1200.00"));
        when(pedidoRepository.sumTotalBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("50.00"), new BigDecimal("300.00"));

        when(ventaRepository.countBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L, 20L);
        when(pedidoRepository.countBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1L, 5L);

        when(productoRepository.countStockBajo()).thenReturn(4L);

        EstadoCantidadProjection estPendiente = mock(EstadoCantidadProjection.class);
        when(estPendiente.getEstado()).thenReturn("PENDIENTE");
        when(estPendiente.getCantidad()).thenReturn(2L);

        EstadoCantidadProjection estEntregado = mock(EstadoCantidadProjection.class);
        when(estEntregado.getEstado()).thenReturn("ENTREGADO");
        when(estEntregado.getCantidad()).thenReturn(3L);

        when(pedidoRepository.countPedidosPorEstado()).thenReturn(List.of(estPendiente, estEntregado));

        DashboardKpiResponse kpis = dashboardService.obtenerKpis();

        assertNotNull(kpis);
        assertEquals(new BigDecimal("200.00"), kpis.getVentasHoy());
        assertEquals(4L, kpis.getCantidadVentasHoy());
        assertEquals(new BigDecimal("1500.00"), kpis.getVentasMes());
        assertEquals(25L, kpis.getCantidadVentasMes());
        // 1500 / 25 = 60.00
        assertEquals(new BigDecimal("60.00"), kpis.getTicketPromedio());
        assertEquals(4L, kpis.getProductosStockBajo());

        assertEquals(2L, kpis.getPedidosPorEstado().getPendientes());
        assertEquals(3L, kpis.getPedidosPorEstado().getEntregados());
        assertEquals(5L, kpis.getPedidosPorEstado().getTotal());
    }

    @Test
    @DisplayName("Debe calcular ventas por día completando días sin ventas con 0.00")
    void testObtenerVentasPorDia() {
        LocalDate hoy = LocalDate.now();
        Date sqlHoy = Date.valueOf(hoy);

        VentasPorDiaProjection vProj = mock(VentasPorDiaProjection.class);
        when(vProj.getFecha()).thenReturn(sqlHoy);
        when(vProj.getTotal()).thenReturn(new BigDecimal("100.00"));
        when(vProj.getCantidad()).thenReturn(2L);

        when(ventaRepository.sumVentasPorDia(any(LocalDateTime.class))).thenReturn(List.of(vProj));
        when(pedidoRepository.sumPedidosPorDia(any(LocalDateTime.class))).thenReturn(List.of());

        List<VentasPorDiaResponse> resultado = dashboardService.obtenerVentasPorDia(7);

        assertEquals(7, resultado.size());
        VentasPorDiaResponse ultimoDia = resultado.get(6);
        assertEquals(hoy, ultimoDia.getFecha());
        assertEquals(new BigDecimal("100.00"), ultimoDia.getTotal());
        assertEquals(2L, ultimoDia.getCantidad());

        // Días anteriores sin ventas deben tener 0.00
        VentasPorDiaResponse primerDia = resultado.get(0);
        assertEquals(new BigDecimal("0.00"), primerDia.getTotal());
        assertEquals(0L, primerDia.getCantidad());
    }

    @Test
    @DisplayName("Debe combinar ventas por categoría y ordenar descendentemente")
    void testObtenerVentasPorCategoria() {
        CategoriaTotalProjection c1 = mock(CategoriaTotalProjection.class);
        when(c1.getCategoria()).thenReturn("Abarrotes");
        when(c1.getTotal()).thenReturn(new BigDecimal("80.00"));

        CategoriaTotalProjection c2 = mock(CategoriaTotalProjection.class);
        when(c2.getCategoria()).thenReturn("Bebidas");
        when(c2.getTotal()).thenReturn(new BigDecimal("40.00"));

        CategoriaTotalProjection cp = mock(CategoriaTotalProjection.class);
        when(cp.getCategoria()).thenReturn("Abarrotes");
        when(cp.getTotal()).thenReturn(new BigDecimal("50.00"));

        when(detalleVentaRepository.sumVentasPorCategoria()).thenReturn(List.of(c1, c2));
        when(detallePedidoRepository.sumPedidosPorCategoria()).thenReturn(List.of(cp));

        List<VentasPorCategoriaResponse> resultado = dashboardService.obtenerVentasPorCategoria();

        assertEquals(2, resultado.size());
        assertEquals("Abarrotes", resultado.get(0).getCategoria());
        assertEquals(new BigDecimal("130.00"), resultado.get(0).getTotal());

        assertEquals("Bebidas", resultado.get(1).getCategoria());
        assertEquals(new BigDecimal("40.00"), resultado.get(1).getTotal());
    }

    @Test
    @DisplayName("Debe obtener top productos ordenados por cantidad y respetar el límite")
    void testObtenerTopProductos() {
        ProductoTotalProjection p1 = mock(ProductoTotalProjection.class);
        when(p1.getProductoId()).thenReturn(1L);
        when(p1.getProducto()).thenReturn("Arroz 5kg");
        when(p1.getCantidad()).thenReturn(10L);
        when(p1.getTotal()).thenReturn(new BigDecimal("249.00"));

        ProductoTotalProjection p2 = mock(ProductoTotalProjection.class);
        when(p2.getProductoId()).thenReturn(2L);
        when(p2.getProducto()).thenReturn("Azúcar 1kg");
        when(p2.getCantidad()).thenReturn(5L);
        when(p2.getTotal()).thenReturn(new BigDecimal("22.50"));

        ProductoTotalProjection p1Web = mock(ProductoTotalProjection.class);
        when(p1Web.getProductoId()).thenReturn(1L);
        when(p1Web.getCantidad()).thenReturn(3L);
        when(p1Web.getTotal()).thenReturn(new BigDecimal("74.70"));

        when(detalleVentaRepository.sumVentasPorProducto()).thenReturn(List.of(p1, p2));
        when(detallePedidoRepository.sumPedidosPorProducto()).thenReturn(List.of(p1Web));

        List<TopProductoResponse> top = dashboardService.obtenerTopProductos(5);

        assertEquals(2, top.size());
        assertEquals(1L, top.get(0).getId());
        assertEquals("Arroz 5kg", top.get(0).getProducto());
        assertEquals(13L, top.get(0).getCantidad());
        assertEquals(new BigDecimal("323.70"), top.get(0).getTotal());

        assertEquals(2L, top.get(1).getId());
        assertEquals("Azúcar 1kg", top.get(1).getProducto());
        assertEquals(5L, top.get(1).getCantidad());
        assertEquals(new BigDecimal("22.50"), top.get(1).getTotal());
    }

    @Test
    @DisplayName("Debe combinar ventas por método de pago normalizando mayúsculas")
    void testObtenerVentasPorMetodoPago() {
        MetodoPagoTotalProjection m1 = mock(MetodoPagoTotalProjection.class);
        when(m1.getMetodo()).thenReturn("efectivo");
        when(m1.getTotal()).thenReturn(new BigDecimal("100.00"));
        when(m1.getCantidad()).thenReturn(2L);

        MetodoPagoTotalProjection m2 = mock(MetodoPagoTotalProjection.class);
        when(m2.getMetodo()).thenReturn("EFECTIVO");
        when(m2.getTotal()).thenReturn(new BigDecimal("50.00"));
        when(m2.getCantidad()).thenReturn(1L);

        when(ventaRepository.sumVentasPorMetodoPago()).thenReturn(List.of(m1));
        when(pedidoRepository.sumPedidosPorMetodoPago()).thenReturn(List.of(m2));

        List<VentasPorMetodoPagoResponse> metodos = dashboardService.obtenerVentasPorMetodoPago();

        assertEquals(1, metodos.size());
        assertEquals("EFECTIVO", metodos.get(0).getMetodo());
        assertEquals(new BigDecimal("150.00"), metodos.get(0).getTotal());
        assertEquals(3L, metodos.get(0).getCantidad());
    }
}

