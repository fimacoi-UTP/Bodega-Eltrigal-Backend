package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.ReclamoEstadoRequest;
import com.eltrigal.backend.dto.ReclamoRequest;
import com.eltrigal.backend.dto.ReclamoResponse;
import com.eltrigal.backend.entity.EstadoReclamo;
import com.eltrigal.backend.service.ReclamoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el módulo Libro de Reclamaciones (Indecopi).
 * Base path: /api/reclamos
 */
@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
public class ReclamoController {

    private final ReclamoService reclamoService;

    /**
     * Registro público de un reclamo o queja.
     * POST /api/reclamos -> 201 Created con correlativo generado.
     */
    @PostMapping
    public ResponseEntity<ReclamoResponse> crear(@Valid @RequestBody ReclamoRequest req) {
        ReclamoResponse response = reclamoService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listado administrativo de reclamos con filtro opcional por estado.
     * GET /api/reclamos -> 200 OK (Solo ADMIN).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReclamoResponse>> listar(
            @RequestParam(required = false) EstadoReclamo estado) {
        return ResponseEntity.ok(reclamoService.listar(estado));
    }

    /**
     * Obtención de un reclamo por su identificador.
     * GET /api/reclamos/{id} -> 200 OK (Solo ADMIN).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReclamoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reclamoService.obtenerPorId(id));
    }

    /**
     * Actualización del estado y acciones tomadas del reclamo.
     * PATCH /api/reclamos/{id}/estado -> 200 OK (Solo ADMIN).
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReclamoResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ReclamoEstadoRequest req) {
        return ResponseEntity.ok(reclamoService.actualizarEstado(id, req));
    }
}

