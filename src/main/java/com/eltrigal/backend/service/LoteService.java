package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.LoteRequest;
import com.eltrigal.backend.dto.LoteResponse;
import com.eltrigal.backend.entity.Lote;
import com.eltrigal.backend.entity.MovimientoInventario;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.LoteRepository;
import com.eltrigal.backend.repository.MovimientoInventarioRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona la lógica de negocio de los lotes de productos.
 */
@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    /**
     * Registra el ingreso de un lote de producto, actualiza el stock del producto
     * y genera el movimiento de inventario correspondiente en una sola transacción atómica.
     *
     * @param req Datos del lote a ingresar
     * @return DTO LoteResponse del lote creado
     */
    @Transactional
    public LoteResponse registrarIngreso(LoteRequest req) {
        // 1. Cargar el producto (404 si no existe)
        Producto producto = productoRepository.findById(req.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + req.getProductoId()));

        // 2. Crear y guardar el lote
        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setCodigoLote(req.getCodigoLote());
        lote.setCantidadInicial(req.getCantidad());
        lote.setCantidadActual(req.getCantidad());
        lote.setCostoUnitario(req.getCostoUnitario());
        lote.setFechaVencimiento(req.getFechaVencimiento());
        lote.setActivo(true);
        Lote loteGuardado = loteRepository.save(lote);

        // 3. Actualizar stock del producto
        int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
        int stockNuevo = stockAnterior + req.getCantidad();
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // 4. Crear y registrar el movimiento de inventario tipo ENTRADA
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setLote(loteGuardado);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(req.getCantidad());
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(req.getMotivo() != null && !req.getMotivo().isBlank() ? req.getMotivo() : "Ingreso de lote");
        movimiento.setReferencia("LOTE#" + loteGuardado.getId());
        movimientoInventarioRepository.save(movimiento);

        // 5. Devolver respuesta
        return toResponse(loteGuardado);
    }

    /**
     * Lista todos los lotes activos asociados a un producto.
     *
     * @param productoId Identificador del producto
     * @return Lista de lotes activos
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> listarPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return loteRepository.findByProductoIdAndActivoTrue(productoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene los lotes activos cuya fecha de vencimiento sea menor o igual a hoy + dias.
     *
     * @param dias Número de días a proyectar hacia adelante
     * @return Lista de lotes por vencer
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> porVencer(int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        return loteRepository.findByActivoTrueAndFechaVencimientoLessThanEqual(limite)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Mapea una entidad Lote a su DTO LoteResponse.
     *
     * @param lote Entidad JPA
     * @return DTO LoteResponse
     */
    private LoteResponse toResponse(Lote lote) {
        return LoteResponse.builder()
                .id(lote.getId())
                .productoId(lote.getProducto() != null ? lote.getProducto().getId() : null)
                .productoNombre(lote.getProducto() != null ? lote.getProducto().getNombre() : null)
                .codigoLote(lote.getCodigoLote())
                .cantidadInicial(lote.getCantidadInicial())
                .cantidadActual(lote.getCantidadActual())
                .costoUnitario(lote.getCostoUnitario())
                .fechaVencimiento(lote.getFechaVencimiento())
                .fechaIngreso(lote.getFechaIngreso())
                .activo(lote.getActivo())
                .build();
    }

}
