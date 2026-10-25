package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.CategoriaRequest;
import com.eltrigal.backend.dto.CategoriaResponse;
import com.eltrigal.backend.service.CategoriaService;
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
 * Controlador REST de solo lectura para categorías.
 * Expone endpoints bajo /api/categorias.
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Lista las categorías activas ordenadas por nombre.
     * GET /api/categorias -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    /**
     * Crea una categoría por nombre o devuelve la existente (find-or-create).
     * POST /api/categorias -> 200 OK
     */
    @PostMapping
    public ResponseEntity<CategoriaResponse> crearOObtener(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.ok(categoriaService.crearOObtener(req.getNombre()));
    }

}
