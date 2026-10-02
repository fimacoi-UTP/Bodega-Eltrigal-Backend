package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.VentaRequest;
import com.eltrigal.backend.dto.VentaResponse;
import com.eltrigal.backend.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el módulo de ventas.
 * Expone endpoints bajo /api/ventas.
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    /**
     * Registra una nueva venta de forma transaccional.
     * POST /api/ventas -> 201 Created
     */
    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody VentaRequest req) {
        VentaResponse response = ventaService.registrarVenta(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta una venta por su identificador único.
     * GET /api/ventas/{id} -> 200 OK / 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    /**
     * Lista todas las ventas realizadas en la fecha actual.
     * GET /api/ventas/hoy -> 200 OK
     */
    @GetMapping("/hoy")
    public ResponseEntity<List<VentaResponse>> ventasDeHoy() {
        return ResponseEntity.ok(ventaService.ventasDeHoy());
    }

    /**
     * Anula una venta registrada y repone el inventario.
     * PUT /api/ventas/{id}/anular?motivo=... -> 200 OK
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<VentaResponse> anular(
            @PathVariable Long id,
            @RequestParam String motivo
    ) {
        return ResponseEntity.ok(ventaService.anular(id, motivo));
    }

}
