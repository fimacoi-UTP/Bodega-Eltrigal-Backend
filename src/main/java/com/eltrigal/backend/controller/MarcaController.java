package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.MarcaRequest;
import com.eltrigal.backend.dto.MarcaResponse;
import com.eltrigal.backend.service.MarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de solo lectura para marcas.
 * Expone endpoints bajo /api/marcas.
 */
@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    /**
     * Lista las marcas activas ordenadas por nombre.
     * GET /api/marcas -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<MarcaResponse>> listarActivas() {
        return ResponseEntity.ok(marcaService.listarActivas());
    }

    /**
     * Crea una marca por nombre o devuelve la existente (find-or-create).
     * POST /api/marcas -> 200 OK
     */
    @PostMapping
    public ResponseEntity<MarcaResponse> crearOObtener(@Valid @RequestBody MarcaRequest req) {
        return ResponseEntity.ok(marcaService.crearOObtener(req.getNombre()));
    }

}
