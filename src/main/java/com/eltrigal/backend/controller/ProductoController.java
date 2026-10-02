package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.ProductoRequest;
import com.eltrigal.backend.dto.ProductoResponse;
import com.eltrigal.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el recurso Producto.
 * Expone endpoints bajo la ruta /api/productos.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Lista todos los productos activos.
     * GET /api/productos -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar() {
        return ResponseEntity.ok(productoService.listar());
    }

    /**
     * Obtiene el detalle de un producto por su identificador.
     * GET /api/productos/{id} -> 200 OK (o 404 si no existe)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    /**
     * Registra un nuevo producto.
     * POST /api/productos -> 201 Created
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req) {
        ProductoResponse response = productoService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un producto existente.
     * PUT /api/productos/{id} -> 200 OK (o 404 si no existe)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest req
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, req));
    }

    /**
     * Realiza baja lógica (soft delete) del producto.
     * DELETE /api/productos/{id} -> 204 No Content (o 404 si no existe)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
