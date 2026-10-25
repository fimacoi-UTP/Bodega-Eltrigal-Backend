package com.eltrigal.backend.controller;

import com.eltrigal.backend.dto.AuthResponse;
import com.eltrigal.backend.dto.LoginRequest;
import com.eltrigal.backend.dto.RegistroRequest;
import com.eltrigal.backend.entity.Rol;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.PedidoRepository;
import com.eltrigal.backend.repository.RolRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para operaciones de autenticación y registro de usuarios.
 * Expone endpoints bajo /api/auth.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PedidoRepository pedidoRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        /**
         * Endpoint para iniciar sesión y obtener un token JWT.
         * POST /api/auth/login
         *
         * @param req Credenciales de inicio de sesión
         * @return 200 OK con AuthResponse o 401 Unauthorized si las credenciales son
         *         inválidas
         */
        @PostMapping("/login")
        @Transactional
        public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
                String emailNormalizado = req.getEmail().trim().toLowerCase();
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(emailNormalizado, req.getPassword()));
                } catch (BadCredentialsException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error",
                                                        "Credenciales incorrectas: email o contraseña inválidos"));
                } catch (DisabledException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error",
                                                        "La cuenta de usuario se encuentra deshabilitada o inactiva"));
                } catch (AuthenticationException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Error de autenticación: " + e.getMessage()));
                }

                Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado con email: " + emailNormalizado));

                pedidoRepository.vincularPedidosInvitado(usuario.getId(), emailNormalizado);

                String token = jwtService.generarToken(usuario);

                AuthResponse response = AuthResponse.builder()
                                .id(usuario.getId())
                                .token(token)
                                .email(usuario.getEmail())
                                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : "")
                                .nombres(usuario.getNombres())
                                .apellidos(usuario.getApellidos())
                                .telefono(usuario.getTelefono())
                                .avatarUrl(usuario.getAvatarUrl())
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Endpoint para registrar un nuevo cliente con auto-login.
         * POST /api/auth/registro
         *
         * @param req Datos de registro del cliente
         * @return 201 Created con AuthResponse o 409 Conflict si el email ya existe
         */
        @PostMapping("/registro")
        @Transactional
        public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest req) {
                String emailNormalizado = req.getEmail().trim().toLowerCase();

                if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("error", "El email ya está registrado"));
                }

                Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                                .orElseThrow(() -> new IllegalStateException(
                                                "El rol 'CLIENTE' no existe en la base de datos"));

                Usuario usuario = new Usuario();
                usuario.setNombres(req.getNombres().trim());
                usuario.setApellidos(req.getApellidos().trim());
                usuario.setEmail(emailNormalizado);
                usuario.setTelefono(req.getTelefono() != null && !req.getTelefono().isBlank() ? req.getTelefono().trim()
                                : null);
                usuario.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                usuario.setRol(rolCliente);
                usuario.setActivo(true);

                Usuario usuarioGuardado = usuarioRepository.save(usuario);

                pedidoRepository.vincularPedidosInvitado(usuarioGuardado.getId(), emailNormalizado);

                String token = jwtService.generarToken(usuarioGuardado);

                AuthResponse response = AuthResponse.builder()
                                .id(usuarioGuardado.getId())
                                .token(token)
                                .email(usuarioGuardado.getEmail())
                                .rol(usuarioGuardado.getRol() != null ? usuarioGuardado.getRol().getNombre() : "")
                                .nombres(usuarioGuardado.getNombres())
                                .apellidos(usuarioGuardado.getApellidos())
                                .telefono(usuarioGuardado.getTelefono())
                                .avatarUrl(usuarioGuardado.getAvatarUrl())
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

}
