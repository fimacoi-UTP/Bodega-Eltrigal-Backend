package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.ProductoPublicoResponse;
import com.eltrigal.backend.dto.ProductoRequest;
import com.eltrigal.backend.dto.ProductoResponse;
import com.eltrigal.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el recurso Producto.
 * Separa la lectura pública (storefront) de la lectura de gestión interna (dashboard y POS).
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Lista únicamente los productos visibles para la tienda web pública (storefront).
     * Retorna ProductoPublicoResponse sin datos sensibles de stock exacto ni costos.
     * GET /api/productos -> 200 OK (Público / permitAll)
     */
    @GetMapping
    public ResponseEntity<List<ProductoPublicoResponse>> listar() {
        return ResponseEntity.ok(productoService.listarPublico());
    }

    /**
     * Obtiene el detalle público de un producto visible para la tienda web.
     * GET /api/productos/{id} -> 200 OK (Público / permitAll)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoPublicoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPublicoPorId(id));
    }

    /**
     * Lista todos los productos activos con información completa para gestión interna (Dashboard y POS).
     * Incluye stock exacto, stock mínimo y visibilidad.
     * GET /api/productos/gestion -> 200 OK (ADMIN y CAJERO)
     */
    @GetMapping("/gestion")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<List<ProductoResponse>> listarGestion() {
        return ResponseEntity.ok(productoService.listar());
    }

    /**
     * Obtiene el detalle completo de gestión de un producto por su ID (Dashboard y POS).
     * GET /api/productos/gestion/{id} -> 200 OK (ADMIN y CAJERO)
     */
    @GetMapping("/gestion/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<ProductoResponse> obtenerGestionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    /**
     * Registra un nuevo producto en el catálogo.
     * Permitido únicamente para: ADMIN.
     * POST /api/productos -> 201 Created
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req) {
        ProductoResponse response = productoService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un producto existente en el catálogo.
     * Permitido únicamente para: ADMIN.
     * PUT /api/productos/{id} -> 200 OK (o 404 si no existe)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest req
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, req));
    }

    /**
     * Modifica la visibilidad de un producto en la tienda web.
     * Permitido únicamente para: ADMIN.
     * PUT/PATCH /api/productos/{id}/visibilidad -> 200 OK
     */
    @RequestMapping(value = "/{id}/visibilidad", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> cambiarVisibilidad(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean visible,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Boolean nuevoVisible = visible;
        if (nuevoVisible == null && body != null) {
            if (body.containsKey("visibleWeb")) {
                nuevoVisible = Boolean.valueOf(String.valueOf(body.get("visibleWeb")));
            } else if (body.containsKey("visible")) {
                nuevoVisible = Boolean.valueOf(String.valueOf(body.get("visible")));
            }
        }
        return ResponseEntity.ok(productoService.cambiarVisibilidad(id, nuevoVisible != null ? nuevoVisible : true));
    }

    /**
     * Ajusta existencias en stock de un producto.
     * Permitido para: ADMIN y CAJERO.
     * PUT/PATCH /api/productos/{id}/stock -> 200 OK
     */
    @RequestMapping(value = "/{id}/stock", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<ProductoResponse> ajustarStock(
            @PathVariable Long id,
            @RequestParam(required = false) Integer stock,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        Integer nuevoStock = stock;
        if (nuevoStock == null && body != null && body.containsKey("stock")) {
            nuevoStock = Integer.valueOf(String.valueOf(body.get("stock")));
        }
        return ResponseEntity.ok(productoService.ajustarStock(id, nuevoStock != null ? nuevoStock : 0));
    }

    /**
     * Realiza baja lógica (soft delete) del producto.
     * Permitido únicamente para: ADMIN.
     * DELETE /api/productos/{id} -> 204 No Content (o 404 si no existe)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
