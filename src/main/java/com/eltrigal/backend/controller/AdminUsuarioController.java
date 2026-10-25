package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.ActualizarUsuarioRequest;
import com.eltrigal.backend.dto.CrearUsuarioRequest;
import com.eltrigal.backend.dto.ResetPasswordAdminRequest;
import com.eltrigal.backend.dto.UsuarioAdminResponse;
import com.eltrigal.backend.service.AdminUsuarioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión administrativa de usuarios (módulo de administración).
 * Expone endpoints bajo /api/admin/usuarios.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    /**
     * Lista todos los usuarios registrados en el sistema.
     * GET /api/admin/usuarios -> 200 OK
     */
    @GetMapping
    public ResponseEntity<List<UsuarioAdminResponse>> listar() {
        return ResponseEntity.ok(adminUsuarioService.listar());
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * POST /api/admin/usuarios -> 201 Created
     */
    @PostMapping
    public ResponseEntity<UsuarioAdminResponse> crear(@Valid @RequestBody CrearUsuarioRequest req) {
        UsuarioAdminResponse response = adminUsuarioService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza los datos y el rol de un usuario existente.
     * PUT /api/admin/usuarios/{id} -> 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioAdminResponse> actualizar(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest req
    ) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminUsuarioService.actualizar(id, req, adminEmail));
    }

    /**
     * Modifica el estado activo/inactivo de un usuario.
     * PUT /api/admin/usuarios/{id}/estado?activo=true|false -> 200 OK
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<UsuarioAdminResponse> cambiarEstado(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam Boolean activo
    ) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminUsuarioService.cambiarEstado(id, activo, adminEmail));
    }

    /**
     * Resetea la contraseña de un usuario directamente.
     * PUT /api/admin/usuarios/{id}/password -> 200 OK
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordAdminRequest req
    ) {
        adminUsuarioService.resetPassword(id, req.getPasswordNuevo());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida exitosamente"));
    }

    /**
     * Elimina permanentemente un usuario si no tiene pedidos ni ventas asociados.
     * DELETE /api/admin/usuarios/{id} -> 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String adminEmail = authentication.getName();
        adminUsuarioService.eliminar(id, adminEmail);
        return ResponseEntity.noContent().build();
    }

}
