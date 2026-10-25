package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.DireccionRequest;
import com.eltrigal.backend.dto.DireccionResponse;
import com.eltrigal.backend.service.DireccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
 * Controlador REST para la gestión de direcciones de entrega del usuario autenticado.
 * Expone endpoints bajo /api/usuarios/me/direcciones.
 */
@RestController
@RequestMapping("/api/usuarios/me/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    /**
     * Lista todas las direcciones activas del usuario autenticado.
     * GET /api/usuarios/me/direcciones -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<DireccionResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(direccionService.listar(authentication.getName()));
    }

    /**
     * Registra una nueva dirección para el usuario autenticado.
     * POST /api/usuarios/me/direcciones -> 201 Created
     */
    @PostMapping
    public ResponseEntity<DireccionResponse> crear(
            Authentication authentication,
            @Valid @RequestBody DireccionRequest req
    ) {
        DireccionResponse response = direccionService.crear(authentication.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza una dirección existente del usuario autenticado.
     * PUT /api/usuarios/me/direcciones/{id} -> 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<DireccionResponse> actualizar(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DireccionRequest req
    ) {
        return ResponseEntity.ok(direccionService.actualizar(authentication.getName(), id, req));
    }

    /**
     * Realiza la baja lógica de una dirección del usuario autenticado.
     * DELETE /api/usuarios/me/direcciones/{id} -> 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            Authentication authentication,
            @PathVariable Long id
    ) {
        direccionService.eliminar(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca una dirección específica como predeterminada para el usuario autenticado.
     * PUT /api/usuarios/me/direcciones/{id}/predeterminada -> 200 OK
     */
    @PutMapping("/{id}/predeterminada")
    public ResponseEntity<DireccionResponse> marcarPredeterminada(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(direccionService.marcarPredeterminada(authentication.getName(), id));
    }

}
