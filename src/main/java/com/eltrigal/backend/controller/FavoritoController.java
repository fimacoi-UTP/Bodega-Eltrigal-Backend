package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.FavoritoResponse;
import com.eltrigal.backend.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de productos favoritos del usuario autenticado.
 * Expone endpoints bajo /api/usuarios/me/favoritos.
 */
@RestController
@RequestMapping("/api/usuarios/me/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    /**
     * Lista todos los favoritos del usuario autenticado.
     * GET /api/usuarios/me/favoritos -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<FavoritoResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(favoritoService.listar(authentication.getName()));
    }

    /**
     * Agrega un producto a favoritos (o retorna el existente si ya estaba agregado).
     * POST /api/usuarios/me/favoritos/{productoId} -> 201 Created / 200 OK
     */
    @PostMapping("/{productoId}")
    public ResponseEntity<FavoritoResponse> agregar(
            Authentication authentication,
            @PathVariable Long productoId
    ) {
        FavoritoService.ResultadoFavorito resultado = favoritoService.agregar(authentication.getName(), productoId);
        if (resultado.yaExistia()) {
            return ResponseEntity.ok(resultado.response());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado.response());
    }

    /**
     * Elimina un producto de la lista de favoritos del usuario autenticado.
     * DELETE /api/usuarios/me/favoritos/{productoId} -> 204 No Content
     */
    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> quitar(
            Authentication authentication,
            @PathVariable Long productoId
    ) {
        favoritoService.quitar(authentication.getName(), productoId);
        return ResponseEntity.noContent().build();
    }

}
