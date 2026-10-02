package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.ProductoRequest;
import com.eltrigal.backend.dto.ProductoResponse;
import com.eltrigal.backend.entity.Categoria;
import com.eltrigal.backend.entity.Marca;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.CategoriaRepository;
import com.eltrigal.backend.repository.MarcaRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio que contiene la lógica de negocio para la gestión de productos.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    /**
     * Lista todos los productos activos.
     *
     * @return Lista de ProductoResponse con productos activos
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene el detalle de un producto por su ID.
     *
     * @param id Identificador único del producto
     * @return DTO ProductoResponse
     * @throws ResourceNotFoundException si el producto no existe
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return toResponse(producto);
    }

    /**
     * Registra un nuevo producto en el sistema.
     *
     * @param req Datos del producto a crear
     * @return ProductoResponse con el producto creado
     */
    public ProductoResponse crear(ProductoRequest req) {
        Categoria categoria = categoriaRepository.findById(req.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + req.getCategoriaId()));

        Marca marca = null;
        if (req.getMarcaId() != null) {
            marca = marcaRepository.findById(req.getMarcaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + req.getMarcaId()));
        }

        Producto producto = new Producto();
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setNombre(req.getNombre());
        producto.setCodigoBarras(req.getCodigoBarras());
        producto.setDescripcion(req.getDescripcion());
        producto.setPrecio(req.getPrecio());
        producto.setStock(req.getStock() != null ? req.getStock() : 0);
        producto.setStockMinimo(req.getStockMinimo() != null ? req.getStockMinimo() : 10);
        producto.setUnidadMedida(req.getUnidadMedida());
        producto.setImagen(req.getImagen());
        producto.setVisibleWeb(req.getVisibleWeb() != null ? req.getVisibleWeb() : true);
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    /**
     * Actualiza un producto existente en el sistema.
     *
     * @param id  Identificador único del producto
     * @param req Datos actualizados
     * @return ProductoResponse actualizado
     */
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(req.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + req.getCategoriaId()));

        Marca marca = null;
        if (req.getMarcaId() != null) {
            marca = marcaRepository.findById(req.getMarcaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con ID: " + req.getMarcaId()));
        }

        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setNombre(req.getNombre());
        producto.setCodigoBarras(req.getCodigoBarras());
        producto.setDescripcion(req.getDescripcion());
        producto.setPrecio(req.getPrecio());
        if (req.getStock() != null) {
            producto.setStock(req.getStock());
        }
        if (req.getStockMinimo() != null) {
            producto.setStockMinimo(req.getStockMinimo());
        }
        producto.setUnidadMedida(req.getUnidadMedida());
        producto.setImagen(req.getImagen());
        if (req.getVisibleWeb() != null) {
            producto.setVisibleWeb(req.getVisibleWeb());
        }

        Producto actualizado = productoRepository.save(producto);
        return toResponse(actualizado);
    }

    /**
     * Realiza un soft delete (baja lógica) cambiando activo a false.
     *
     * @param id Identificador único del producto
     */
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    /**
     * Mapea una entidad Producto a su DTO plano ProductoResponse.
     *
     * @param producto Entidad JPA
     * @return DTO ProductoResponse
     */
    private ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .codigoBarras(producto.getCodigoBarras())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .stockMinimo(producto.getStockMinimo())
                .unidadMedida(producto.getUnidadMedida())
                .imagen(producto.getImagen())
                .visibleWeb(producto.getVisibleWeb())
                .activo(producto.getActivo())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .categoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                .marcaId(producto.getMarca() != null ? producto.getMarca().getId() : null)
                .marcaNombre(producto.getMarca() != null ? producto.getMarca().getNombre() : null)
                .creadoEn(producto.getCreadoEn())
                .actualizadoEn(producto.getActualizadoEn())
                .build();
    }

}
