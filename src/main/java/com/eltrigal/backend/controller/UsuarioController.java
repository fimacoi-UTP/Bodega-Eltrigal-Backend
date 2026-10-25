package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.CambiarPasswordRequest;
import com.eltrigal.backend.dto.PerfilRequest;
import com.eltrigal.backend.dto.UsuarioResponse;
import com.eltrigal.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para la gestión del perfil del usuario autenticado.
 * Expone endpoints bajo /api/usuarios.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene los datos del perfil del usuario autenticado.
     * GET /api/usuarios/me -> 200 OK
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.obtenerPerfil(email));
    }

    /**
     * Actualiza la información del perfil del usuario autenticado.
     * PUT /api/usuarios/me -> 200 OK
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody PerfilRequest req
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.actualizarPerfil(email, req));
    }

    /**
     * Cambia la contraseña del usuario autenticado previa validación de la actual.
     * PUT /api/usuarios/me/password -> 200 OK
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<UsuarioResponse> actualizarAvatar(
            Authentication authentication,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String email = authentication.getName();
        String avatarUrl = body != null ? body.get("avatarUrl") : null;
        return ResponseEntity.ok(usuarioService.actualizarAvatar(email, avatarUrl));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            Authentication authentication,
            @Valid @RequestBody CambiarPasswordRequest req
    ) {
        String email = authentication.getName();
        usuarioService.cambiarPassword(email, req);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }

}
