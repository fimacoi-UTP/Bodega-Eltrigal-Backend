package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.PromocionRequest;
import com.eltrigal.backend.dto.PromocionResponse;
import com.eltrigal.backend.service.PromocionService;
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
 * Controlador REST para la gestión de promociones.
 * Expone endpoints bajo /api/promociones.
 */
@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;

    /**
     * Lista todas las promociones del sistema.
     * GET /api/promociones -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<PromocionResponse>> listar() {
        return ResponseEntity.ok(promocionService.listar());
    }

    /**
     * Obtiene las promociones vigentes al día de hoy (acceso público).
     * GET /api/promociones/vigentes -> 200 OK
     */
    @GetMapping("/vigentes")
    public ResponseEntity<List<PromocionResponse>> obtenerVigentes() {
        return ResponseEntity.ok(promocionService.obtenerVigentesResponse());
    }

    /**
     * Registra una nueva promoción.
     * POST /api/promociones -> 201 Created
     */
    @PostMapping
    public ResponseEntity<PromocionResponse> crear(@Valid @RequestBody PromocionRequest req) {
        PromocionResponse response = promocionService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza una promoción existente.
     * PUT /api/promociones/{id} -> 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromocionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PromocionRequest req
    ) {
        return ResponseEntity.ok(promocionService.actualizar(id, req));
    }

    /**
     * Da de baja lógica (soft delete) a una promoción.
     * DELETE /api/promociones/{id} -> 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
