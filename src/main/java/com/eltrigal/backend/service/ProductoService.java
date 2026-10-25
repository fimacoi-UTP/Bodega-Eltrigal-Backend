package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.ProductoPublicoResponse;
import com.eltrigal.backend.dto.ProductoRequest;
import com.eltrigal.backend.dto.ProductoResponse;
import com.eltrigal.backend.entity.Categoria;
import com.eltrigal.backend.entity.Marca;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ConflictException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.CategoriaRepository;
import com.eltrigal.backend.repository.MarcaRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import com.eltrigal.backend.util.Ean13Util;
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
     * Lista únicamente los productos visibles y activos para la tienda web pública (storefront).
     *
     * @return Lista de ProductoPublicoResponse sin información sensible de stock exacto o costos
     */
    @Transactional(readOnly = true)
    public List<ProductoPublicoResponse> listarPublico() {
        return productoRepository.findByActivoTrueAndVisibleWebTrue()
                .stream()
                .map(this::toPublicoResponse)
                .toList();
    }

    /**
     * Obtiene el detalle público de un producto visible para la tienda web.
     *
     * @param id Identificador único del producto
     * @return DTO ProductoPublicoResponse
     */
    @Transactional(readOnly = true)
    public ProductoPublicoResponse obtenerPublicoPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()) && Boolean.TRUE.equals(p.getVisibleWeb()))
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado o no disponible en tienda con ID: " + id));
        return toPublicoResponse(producto);
    }

    /**
     * Lista todos los productos activos para gestión interna (dashboard y POS).
     *
     * @return Lista completa de ProductoResponse con stock y datos de gestión
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
        String codigoEnviado = (req.getCodigoBarras() != null && !req.getCodigoBarras().isBlank())
                ? req.getCodigoBarras().trim()
                : null;

        if (codigoEnviado != null) {
            if (!Ean13Util.esValido(codigoEnviado)) {
                throw new BadRequestException("El código de barras proporcionado no es un EAN-13 válido (debe tener 13 dígitos y checksum correcto)");
            }
            if (productoRepository.existsByCodigoBarras(codigoEnviado)) {
                throw new ConflictException("El código de barras '" + codigoEnviado + "' ya está registrado");
            }
            producto.setCodigoBarras(codigoEnviado);
        }

        Producto guardado = productoRepository.save(producto);

        // Si no se proporcionó código de barras, se autogenera EAN-13 usando el ID generado
        if (codigoEnviado == null) {
            String codigoGenerado = Ean13Util.generar(guardado.getId());
            guardado.setCodigoBarras(codigoGenerado);
            guardado = productoRepository.save(guardado);
        }

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

        if (req.getCodigoBarras() != null && !req.getCodigoBarras().isBlank()) {
            String codigoLimpio = req.getCodigoBarras().trim();
            if (!Ean13Util.esValido(codigoLimpio)) {
                throw new BadRequestException("El código de barras proporcionado no es un EAN-13 válido (debe tener 13 dígitos y checksum correcto)");
            }
            productoRepository.findByCodigoBarras(codigoLimpio)
                    .filter(p -> !p.getId().equals(id))
                    .ifPresent(p -> {
                        throw new ConflictException("El código de barras '" + codigoLimpio + "' ya está registrado en otro producto");
                    });
            producto.setCodigoBarras(codigoLimpio);
        } else if (producto.getCodigoBarras() == null || producto.getCodigoBarras().isBlank()) {
            producto.setCodigoBarras(Ean13Util.generar(producto.getId()));
        }

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
        producto.setColor(req.getColor());
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
     * Modifica la visibilidad de un producto en la tienda web.
     */
    public ProductoResponse cambiarVisibilidad(Long id, Boolean visibleWeb) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setVisibleWeb(visibleWeb != null ? visibleWeb : true);
        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    /**
     * Ajusta las existencias físicas en stock de un producto.
     */
    public ProductoResponse ajustarStock(Long id, Integer nuevoStock) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setStock(nuevoStock != null ? nuevoStock : 0);
        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
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
                .color(producto.getColor())
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

    /**
     * Mapea una entidad Producto a su DTO público para tienda web.
     * No incluye costo, stock exacto ni stock mínimo.
     */
    private ProductoPublicoResponse toPublicoResponse(Producto producto) {
        boolean disponible = producto.getStock() != null && producto.getStock() > 0;
        String categoriaNombre = producto.getCategoria() != null ? producto.getCategoria().getNombre() : null;
        String marcaNombre = producto.getMarca() != null ? producto.getMarca().getNombre() : null;

        return ProductoPublicoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .precioVenta(producto.getPrecio())
                .imagen(producto.getImagen())
                .urlImagen(producto.getImagen())
                .color(producto.getColor())
                .unidadMedida(producto.getUnidadMedida())
                .codigoBarras(producto.getCodigoBarras())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .categoria(categoriaNombre)
                .categoriaNombre(categoriaNombre)
                .marcaId(producto.getMarca() != null ? producto.getMarca().getId() : null)
                .marca(marcaNombre)
                .marcaNombre(marcaNombre)
                .disponible(disponible)
                .build();
    }

}
