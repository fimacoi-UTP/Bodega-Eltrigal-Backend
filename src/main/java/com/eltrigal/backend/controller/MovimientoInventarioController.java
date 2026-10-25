package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.MovimientoRequest;
import com.eltrigal.backend.dto.MovimientoResponse;
import com.eltrigal.backend.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para registrar y consultar movimientos de inventario y Kardex.
 * Expone endpoints bajo /api/movimientos con control de acceso por rol.
 */
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    /**
     * Registra un ajuste de inventario o merma.
     * Permitido para: ADMIN y CAJERO.
     * POST /api/movimientos -> 201 Created
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<MovimientoResponse> registrarAjuste(@Valid @RequestBody MovimientoRequest req) {
        MovimientoResponse response = movimientoInventarioService.registrarAjuste(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta el Kardex histórico de un producto ordenado descendentemente.
     * Permitido para: ADMIN y CAJERO.
     * GET /api/movimientos/producto/{productoId} -> 200 OK
     */
    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public ResponseEntity<List<MovimientoResponse>> kardexDeProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(movimientoInventarioService.kardexDeProducto(productoId));
    }

}
