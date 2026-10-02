package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.DetalleVentaResponse;
import com.eltrigal.backend.dto.ItemVentaRequest;
import com.eltrigal.backend.dto.VentaRequest;
import com.eltrigal.backend.dto.VentaResponse;
import com.eltrigal.backend.entity.DetalleVenta;
import com.eltrigal.backend.entity.MovimientoInventario;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.entity.Venta;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.MovimientoInventarioRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import com.eltrigal.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servicio que contiene la lógica de negocio para el registro, consulta y anulación de ventas.
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private static final Set<String> METODOS_PAGO_VALIDOS = Set.of("EFECTIVO", "YAPE", "PLIN", "TARJETA");
    private static final BigDecimal DIVISOR_IGV = new BigDecimal("1.18");

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    /**
     * Registra una nueva venta de forma atómica:
     * 1. Valida método de pago y verifica stock disponible (fail-fast).
     * 2. Aplica descuentos de stock, calcula importes e IGV, genera movimientos de inventario y guarda la venta.
     *
     * @param req Datos de la venta e ítems
     * @return DTO VentaResponse con el resultado de la transacción
     */
    @Transactional
    public VentaResponse registrarVenta(VentaRequest req) {
        String metodoPagoNormalizado = req.getMetodoPago() != null ? req.getMetodoPago().trim().toUpperCase() : "";

        // 1. Validar método de pago
        if (!METODOS_PAGO_VALIDOS.contains(metodoPagoNormalizado)) {
            throw new BadRequestException("Método de pago no válido: '" + req.getMetodoPago() + "'. Métodos permitidos: " + METODOS_PAGO_VALIDOS);
        }

        // ==========================================
        // PRIMERA PASADA: VALIDACIÓN SIN TOCAR LA BD
        // ==========================================
        // Agrupar cantidades por si el mismo producto se envía más de una vez en el request
        Map<Long, Integer> cantidadesTotales = new HashMap<>();
        for (ItemVentaRequest item : req.getItems()) {
            cantidadesTotales.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }

        Map<Long, Producto> productosMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : cantidadesTotales.entrySet()) {
            Long productoId = entry.getKey();
            Integer cantidadSolicitada = entry.getValue();

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

            int stockDisponible = producto.getStock() != null ? producto.getStock() : 0;
            if (stockDisponible < cantidadSolicitada) {
                throw new BadRequestException("Stock insuficiente de " + producto.getNombre() + ": disponible " + stockDisponible + ", solicitado " + cantidadSolicitada);
            }

            productosMap.put(productoId, producto);
        }

        // ==========================================
        // SEGUNDA PASADA: APLICAR CAMBIOS
        // ==========================================
        Venta venta = new Venta();
        venta.setIdUsuario(req.getIdUsuario());
        venta.setNumeroTicket("VTA-" + System.currentTimeMillis());
        venta.setMetodoPago(metodoPagoNormalizado);
        venta.setMontoRecibido(req.getMontoRecibido() != null ? req.getMontoRecibido().setScale(2, RoundingMode.HALF_UP) : null);
        venta.setReferenciaPago(req.getReferenciaPago());
        venta.setAnulada(false);

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        for (ItemVentaRequest item : req.getItems()) {
            Producto producto = productosMap.get(item.getProductoId());

            BigDecimal precioUnitario = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotalLinea = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotalLinea);

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - item.getCantidad();
            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setPrecioOriginal(precioUnitario);
            detalle.setSubtotal(subtotalLinea);
            detalles.add(detalle);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(producto);
            mov.setIdUsuario(req.getIdUsuario());
            mov.setTipo("VENTA");
            mov.setCantidad(item.getCantidad());
            mov.setStockAnterior(stockAnterior);
            mov.setStockNuevo(stockNuevo);
            mov.setMotivo("Venta realizada");
            movimientos.add(mov);
        }

        // Cálculo de IGV (precios ya incluyen 18% IGV)
        total = total.setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotalBase = total.divide(DIVISOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotalBase).setScale(2, RoundingMode.HALF_UP);

        venta.setTotal(total);
        venta.setSubtotal(subtotalBase);
        venta.setIgv(igv);

        // Cálculo de vuelto en efectivo
        if ("EFECTIVO".equals(metodoPagoNormalizado) && req.getMontoRecibido() != null) {
            BigDecimal vuelto = req.getMontoRecibido().subtract(total).setScale(2, RoundingMode.HALF_UP);
            if (vuelto.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("El monto recibido (" + req.getMontoRecibido() + ") es menor que el total de la venta (" + total + ")");
            }
            venta.setVuelto(vuelto);
        } else {
            venta.setVuelto(null);
        }

        venta.setDetalles(detalles);
        Venta ventaGuardada = ventaRepository.save(venta);

        // Guardar movimientos de inventario con referencia al ID de la venta
        for (MovimientoInventario mov : movimientos) {
            mov.setReferencia("VENTA#" + ventaGuardada.getId());
            movimientoInventarioRepository.save(mov);
        }

        return toResponse(ventaGuardada);
    }

    /**
     * Consulta una venta por su ID junto con sus detalles.
     *
     * @param id Identificador de la venta
     * @return DTO VentaResponse
     */
    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
        return toResponse(venta);
    }

    /**
     * Obtiene la lista de ventas realizadas durante el día actual.
     *
     * @return Lista de VentaResponse
     */
    @Transactional(readOnly = true)
    public List<VentaResponse> ventasDeHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);
        return ventaRepository.findByCreadoEnBetween(inicio, fin)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Anula una venta: repone el stock de cada producto, registra movimientos de tipo ANULACION
     * y marca la venta como anulada con su respectivo motivo.
     *
     * @param id     Identificador de la venta
     * @param motivo Explicación del motivo de anulación
     * @return DTO VentaResponse actualizada
     */
    @Transactional
    public VentaResponse anular(Long id, String motivo) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));

        if (Boolean.TRUE.equals(venta.getAnulada())) {
            throw new BadRequestException("La venta ya se encuentra anulada");
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
            int stockNuevo = stockAnterior + detalle.getCantidad();
            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(producto);
            mov.setIdUsuario(venta.getIdUsuario());
            mov.setTipo("ANULACION");
            mov.setCantidad(detalle.getCantidad());
            mov.setStockAnterior(stockAnterior);
            mov.setStockNuevo(stockNuevo);
            mov.setMotivo(motivo != null && !motivo.isBlank() ? motivo : "Anulación de venta");
            mov.setReferencia("VENTA#" + venta.getId());
            movimientoInventarioRepository.save(mov);
        }

        venta.setAnulada(true);
        venta.setMotivoAnulacion(motivo);
        Venta ventaAnulada = ventaRepository.save(venta);

        return toResponse(ventaAnulada);
    }

    /**
     * Mapea la entidad Venta y sus detalles al DTO VentaResponse.
     *
     * @param venta Entidad JPA
     * @return DTO plano VentaResponse
     */
    private VentaResponse toResponse(Venta venta) {
        List<DetalleVentaResponse> items = venta.getDetalles() != null
                ? venta.getDetalles().stream().map(d -> DetalleVentaResponse.builder()
                .id(d.getId())
                .productoId(d.getProducto() != null ? d.getProducto().getId() : null)
                .productoNombre(d.getProducto() != null ? d.getProducto().getNombre() : null)
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .precioOriginal(d.getPrecioOriginal())
                .subtotal(d.getSubtotal())
                .build()).toList()
                : List.of();

        return VentaResponse.builder()
                .id(venta.getId())
                .numeroTicket(venta.getNumeroTicket())
                .idUsuario(venta.getIdUsuario())
                .subtotal(venta.getSubtotal())
                .igv(venta.getIgv())
                .total(venta.getTotal())
                .metodoPago(venta.getMetodoPago())
                .montoRecibido(venta.getMontoRecibido())
                .vuelto(venta.getVuelto())
                .referenciaPago(venta.getReferenciaPago())
                .anulada(venta.getAnulada())
                .motivoAnulacion(venta.getMotivoAnulacion())
                .creadoEn(venta.getCreadoEn())
                .items(items)
                .build();
    }

}
