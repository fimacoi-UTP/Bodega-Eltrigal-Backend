package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.DetallePedidoResponse;
import com.eltrigal.backend.dto.HistorialEstadoResponse;
import com.eltrigal.backend.dto.ItemPedidoRequest;
import com.eltrigal.backend.dto.PedidoRequest;
import com.eltrigal.backend.dto.PedidoResponse;
import com.eltrigal.backend.entity.DetallePedido;
import com.eltrigal.backend.entity.HistorialEstadoPedido;
import com.eltrigal.backend.entity.MovimientoInventario;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.entity.Pedido;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.MovimientoInventarioRepository;
import com.eltrigal.backend.repository.PedidoRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servicio que contiene la lógica de negocio y máquina de estados (State Pattern) para la gestión de pedidos.
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
            "PENDIENTE",  Set.of("CONFIRMADO", "CANCELADO"),
            "CONFIRMADO", Set.of("EN_CAMINO", "CANCELADO"),
            "EN_CAMINO",  Set.of("ENTREGADO"),
            "ENTREGADO",  Set.of(),
            "CANCELADO",  Set.of()
    );

    private static final Set<String> TIPOS_ENTREGA = Set.of("RECOJO_TIENDA", "DELIVERY");
    private static final Set<String> METODOS_PAGO = Set.of("EFECTIVO", "YAPE", "PLIN", "TARJETA");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    /**
     * Registra un nuevo pedido reservando stock y creando detalle e historial inicial en una sola transacción.
     *
     * @param req Datos del pedido
     * @return DTO PedidoResponse
     */
    @Transactional
    public PedidoResponse crearPedido(PedidoRequest req) {
        String tipoEntregaNorm = req.getTipoEntrega() != null ? req.getTipoEntrega().trim().toUpperCase() : "";
        String metodoPagoNorm = req.getMetodoPago() != null ? req.getMetodoPago().trim().toUpperCase() : "";

        if (!TIPOS_ENTREGA.contains(tipoEntregaNorm)) {
            throw new BadRequestException("Tipo de entrega no válido: '" + req.getTipoEntrega() + "'. Tipos permitidos: " + TIPOS_ENTREGA);
        }

        if (!METODOS_PAGO.contains(metodoPagoNorm)) {
            throw new BadRequestException("Método de pago no válido: '" + req.getMetodoPago() + "'. Métodos permitidos: " + METODOS_PAGO);
        }

        // ==========================================
        // PRIMERA PASADA: VALIDACIÓN FAIL-FAST
        // ==========================================
        Map<Long, Integer> cantidadesTotales = new HashMap<>();
        for (ItemPedidoRequest item : req.getItems()) {
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
        // SEGUNDA PASADA: APLICAR RESERVA Y CREAR PEDIDO
        // ==========================================
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(req.getIdUsuario());
        pedido.setNumeroPedido("PED-" + System.currentTimeMillis());
        pedido.setEstado("PENDIENTE");
        pedido.setTipoEntrega(tipoEntregaNorm);
        pedido.setDireccionEntrega(req.getDireccionEntrega());
        pedido.setMetodoPago(metodoPagoNorm);
        pedido.setReferenciaPago(req.getReferenciaPago());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetallePedido> detalles = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();

        for (ItemPedidoRequest item : req.getItems()) {
            Producto producto = productosMap.get(item.getProductoId());

            BigDecimal precioUnitario = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotalLinea = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(subtotalLinea);

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - item.getCantidad();
            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
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
            mov.setMotivo("Pedido web");
            movimientos.add(mov);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoEnvio = req.getCostoEnvio() != null ? req.getCostoEnvio().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(costoEnvio).setScale(2, RoundingMode.HALF_UP);

        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(costoEnvio);
        pedido.setTotal(total);
        pedido.setDetalles(detalles);

        // Entrada inicial en historial
        HistorialEstadoPedido historialInicial = new HistorialEstadoPedido();
        historialInicial.setPedido(pedido);
        historialInicial.setEstado("PENDIENTE");
        List<HistorialEstadoPedido> historialList = new ArrayList<>();
        historialList.add(historialInicial);
        pedido.setHistorial(historialList);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Guardar movimientos de inventario con referencia al pedido creado
        for (MovimientoInventario mov : movimientos) {
            mov.setReferencia("PEDIDO#" + pedidoGuardado.getId());
            movimientoInventarioRepository.save(mov);
        }

        return toResponse(pedidoGuardado);
    }

    /**
     * Aplica el cambio de estado de un pedido según la máquina de estados configurada.
     * En caso de transición a CANCELADO, repone el inventario de todos los ítems.
     *
     * @param id          Identificador del pedido
     * @param nuevoEstado Estado al cual se desea transicionar
     * @return DTO PedidoResponse actualizado
     */
    @Transactional
    public PedidoResponse cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));

        String nuevoEstadoNorm = nuevoEstado != null ? nuevoEstado.trim().toUpperCase() : "";

        if (!TRANSICIONES.containsKey(nuevoEstadoNorm)) {
            throw new BadRequestException("Estado de pedido no válido: '" + nuevoEstado + "'. Estados válidos: " + TRANSICIONES.keySet());
        }

        String estadoActual = pedido.getEstado();
        Set<String> transicionesPermitidas = TRANSICIONES.get(estadoActual);

        if (transicionesPermitidas == null || !transicionesPermitidas.contains(nuevoEstadoNorm)) {
            throw new BadRequestException("Transición no permitida: de " + estadoActual + " a " + nuevoEstadoNorm);
        }

        // Si se cancela el pedido, reponer inventario
        if ("CANCELADO".equals(nuevoEstadoNorm)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
                int stockNuevo = stockAnterior + detalle.getCantidad();
                producto.setStock(stockNuevo);
                productoRepository.save(producto);

                MovimientoInventario mov = new MovimientoInventario();
                mov.setProducto(producto);
                mov.setIdUsuario(pedido.getIdUsuario());
                mov.setTipo("ANULACION");
                mov.setCantidad(detalle.getCantidad());
                mov.setStockAnterior(stockAnterior);
                mov.setStockNuevo(stockNuevo);
                mov.setMotivo("Cancelación de pedido");
                mov.setReferencia("PEDIDO#" + pedido.getId());
                movimientoInventarioRepository.save(mov);
            }
        }

        pedido.setEstado(nuevoEstadoNorm);

        // Agregar al historial
        HistorialEstadoPedido historial = new HistorialEstadoPedido();
        historial.setPedido(pedido);
        historial.setEstado(nuevoEstadoNorm);
        pedido.getHistorial().add(historial);

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        return toResponse(pedidoActualizado);
    }

    /**
     * Consulta un pedido por su identificador único.
     *
     * @param id Identificador del pedido
     * @return DTO PedidoResponse
     */
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        return toResponse(pedido);
    }

    /**
     * Lista pedidos filtrando por estado opcional.
     *
     * @param estado Estado del pedido (opcional)
     * @return Lista de PedidoResponse
     */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return pedidoRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return pedidoRepository.findByEstado(estado.trim().toUpperCase())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Mapea una entidad Pedido a su DTO PedidoResponse.
     *
     * @param pedido Entidad JPA
     * @return DTO PedidoResponse
     */
    private PedidoResponse toResponse(Pedido pedido) {
        List<DetallePedidoResponse> items = pedido.getDetalles() != null
                ? pedido.getDetalles().stream().map(d -> DetallePedidoResponse.builder()
                .id(d.getId())
                .productoId(d.getProducto() != null ? d.getProducto().getId() : null)
                .productoNombre(d.getProducto() != null ? d.getProducto().getNombre() : null)
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .precioOriginal(d.getPrecioOriginal())
                .subtotal(d.getSubtotal())
                .build()).toList()
                : List.of();

        List<HistorialEstadoResponse> historial = pedido.getHistorial() != null
                ? pedido.getHistorial().stream().map(h -> HistorialEstadoResponse.builder()
                .estado(h.getEstado())
                .creadoEn(h.getCreadoEn())
                .build()).toList()
                : List.of();

        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .idUsuario(pedido.getIdUsuario())
                .estado(pedido.getEstado())
                .tipoEntrega(pedido.getTipoEntrega())
                .direccionEntrega(pedido.getDireccionEntrega())
                .subtotal(pedido.getSubtotal())
                .costoEnvio(pedido.getCostoEnvio())
                .total(pedido.getTotal())
                .metodoPago(pedido.getMetodoPago())
                .referenciaPago(pedido.getReferenciaPago())
                .creadoEn(pedido.getCreadoEn())
                .actualizadoEn(pedido.getActualizadoEn())
                .items(items)
                .historial(historial)
                .build();
    }

}
