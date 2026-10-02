package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.AuthResponse;
import com.eltrigal.backend.dto.LoginRequest;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.UsuarioRepository;
import com.eltrigal.backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para operaciones de autenticación de usuarios.
 * Expone endpoints bajo /api/auth.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    /**
     * Endpoint para iniciar sesión y obtener un token JWT.
     * POST /api/auth/login
     *
     * @param req Credenciales de inicio de sesión
     * @return 200 OK con AuthResponse o 401 Unauthorized si las credenciales son inválidas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas: email o contraseña inválidos"));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "La cuenta de usuario se encuentra deshabilitada o inactiva"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error de autenticación: " + e.getMessage()));
        }

        Usuario usuario = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + req.getEmail()));

        String token = jwtService.generarToken(usuario);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : "")
                .nombres(usuario.getNombres())
                .build();

        return ResponseEntity.ok(response);
    }

}
