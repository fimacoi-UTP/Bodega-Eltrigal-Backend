package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.LoteRequest;
import com.eltrigal.backend.dto.LoteResponse;
import com.eltrigal.backend.service.LoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de lotes de productos.
 * Expone endpoints bajo /api/lotes.
 */
@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    /**
     * Registra el ingreso de un nuevo lote, actualizando stock y kardex.
     * POST /api/lotes -> 201 Created
     */
    @PostMapping
    public ResponseEntity<LoteResponse> registrarIngreso(@Valid @RequestBody LoteRequest req) {
        LoteResponse response = loteService.registrarIngreso(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos los lotes activos de un producto.
     * GET /api/lotes/producto/{productoId} -> 200 OK
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<LoteResponse>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(loteService.listarPorProducto(productoId));
    }

    /**
     * Obtiene los lotes activos próximos a vencer.
     * GET /api/lotes/por-vencer?dias=30 -> 200 OK
     */
    @GetMapping("/por-vencer")
    public ResponseEntity<List<LoteResponse>> porVencer(
            @RequestParam(defaultValue = "30") int dias
    ) {
        return ResponseEntity.ok(loteService.porVencer(dias));
    }

}
