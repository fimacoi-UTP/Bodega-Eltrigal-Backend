package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.MovimientoRequest;
import com.eltrigal.backend.dto.MovimientoResponse;
import com.eltrigal.backend.entity.MovimientoInventario;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.MovimientoInventarioRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Servicio que gestiona la lógica de negocio de los movimientos de inventario y Kardex.
 */
@Service
@RequiredArgsConstructor
public class MovimientoInventarioService {

    private static final Set<String> TIPOS_AJUSTE_VALIDOS = Set.of("AJUSTE_POS", "AJUSTE_NEG", "MERMA");

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;

    /**
     * Registra un ajuste de inventario manual o merma, validando tipo de operación y suficiencia de stock.
     *
     * @param req Datos del ajuste a registrar
     * @return DTO MovimientoResponse con el movimiento generado
     */
    @Transactional
    public MovimientoResponse registrarAjuste(MovimientoRequest req) {
        String tipoNormalizado = req.getTipo() != null ? req.getTipo().trim().toUpperCase() : "";

        // 1. Validar tipo permitido
        if (!TIPOS_AJUSTE_VALIDOS.contains(tipoNormalizado)) {
            throw new BadRequestException("Tipo de movimiento no válido: '" + req.getTipo() + "'. Tipos permitidos: " + TIPOS_AJUSTE_VALIDOS);
        }

        // 2. Cargar el producto (404 si no existe)
        Producto producto = productoRepository.findById(req.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + req.getProductoId()));

        int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
        int stockNuevo;

        // 3. Calcular nuevo stock
        if ("AJUSTE_POS".equals(tipoNormalizado)) {
            stockNuevo = stockAnterior + req.getCantidad();
        } else {
            stockNuevo = stockAnterior - req.getCantidad();
        }

        // 4. Validar que no quede en negativo ANTES de persistir
        if (stockNuevo < 0) {
            throw new BadRequestException("Stock insuficiente: el ajuste dejaría el stock en " + stockNuevo);
        }

        // 5. Actualizar stock del producto
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // 6. Registrar movimiento
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setLote(null);
        movimiento.setIdUsuario(req.getIdUsuario());
        movimiento.setTipo(tipoNormalizado);
        movimiento.setCantidad(req.getCantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(req.getMotivo());
        movimiento.setReferencia("AJUSTE_MANUAL");

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        return toResponse(guardado);
    }

    /**
     * Consulta el historial de movimientos (Kardex) de un producto ordenado descendentemente por fecha.
     *
     * @param productoId Identificador del producto
     * @return Lista de MovimientoResponse
     */
    @Transactional(readOnly = true)
    public List<MovimientoResponse> kardexDeProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return movimientoInventarioRepository.findByProductoIdOrderByCreadoEnDesc(productoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Mapea una entidad MovimientoInventario a su DTO MovimientoResponse.
     *
     * @param m Entidad JPA
     * @return DTO MovimientoResponse
     */
    private MovimientoResponse toResponse(MovimientoInventario m) {
        return MovimientoResponse.builder()
                .id(m.getId())
                .productoId(m.getProducto() != null ? m.getProducto().getId() : null)
                .productoNombre(m.getProducto() != null ? m.getProducto().getNombre() : null)
                .loteId(m.getLote() != null ? m.getLote().getId() : null)
                .idUsuario(m.getIdUsuario())
                .tipo(m.getTipo())
                .cantidad(m.getCantidad())
                .stockAnterior(m.getStockAnterior())
                .stockNuevo(m.getStockNuevo())
                .motivo(m.getMotivo())
                .referencia(m.getReferencia())
                .creadoEn(m.getCreadoEn())
                .build();
    }

}
