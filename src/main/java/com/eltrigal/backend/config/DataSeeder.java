package com.eltrigal.backend.config;

import com.eltrigal.backend.entity.Rol;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.repository.RolRepository;
import com.eltrigal.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeder inicial de datos para garantizar la disponibilidad y contraseñas cifradas
 * de los usuarios estándar del sistema.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Iniciando DataSeeder para verificar y actualizar usuarios estándar...");

        sembrarUsuario("admin@eltrigal.pe", "admin123", "ADMIN", "Administrador", "Sistema");
        sembrarUsuario("cajero@eltrigal.pe", "cajero123", "CAJERO", "Cajero", "Principal");
        sembrarUsuario("cliente@eltrigal.pe", "cliente123", "CLIENTE", "Cliente", "Frecuente");

        log.info("DataSeeder finalizado exitosamente.");
    }

    private void sembrarUsuario(String email, String passwordPlana, String nombreRol, String nombres, String apellidos) {
        usuarioRepository.findByEmail(email).ifPresentOrElse(
                usuario -> {
                    usuario.setPasswordHash(passwordEncoder.encode(passwordPlana));
                    usuario.setActivo(true);
                    usuarioRepository.save(usuario);
                    log.info("Usuario '{}' actualizado con contraseña cifrada.", email);
                },
                () -> {
                    Rol rol = rolRepository.findByNombre(nombreRol)
                            .orElseThrow(() -> new IllegalStateException("El rol '" + nombreRol + "' no existe en la base de datos"));

                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(email);
                    nuevo.setPasswordHash(passwordEncoder.encode(passwordPlana));
                    nuevo.setRol(rol);
                    nuevo.setNombres(nombres);
                    nuevo.setApellidos(apellidos);
                    nuevo.setActivo(true);

                    usuarioRepository.save(nuevo);
                    log.info("Usuario '{}' creado exitosamente con rol '{}'.", email, nombreRol);
                }
        );
    }

}
