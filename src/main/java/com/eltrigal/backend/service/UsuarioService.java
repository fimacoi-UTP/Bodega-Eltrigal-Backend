package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.CambiarPasswordRequest;
import com.eltrigal.backend.dto.PerfilRequest;
import com.eltrigal.backend.dto.UsuarioResponse;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión del perfil y seguridad del usuario.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene los datos del perfil del usuario por su email.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String email) {
        Usuario usuario = buscarPorEmail(email);
        return toResponse(usuario);
    }

    /**
     * Actualiza nombres, apellidos, teléfono y avatar del usuario.
     * No modifica email ni rol.
     */
    public UsuarioResponse actualizarPerfil(String email, PerfilRequest req) {
        Usuario usuario = buscarPorEmail(email);

        usuario.setNombres(req.getNombres().trim());
        usuario.setApellidos(req.getApellidos().trim());

        String telefono = req.getTelefono() != null ? req.getTelefono().trim() : null;
        usuario.setTelefono((telefono != null && !telefono.isEmpty()) ? telefono : null);

        String avatarUrl = req.getAvatarUrl() != null ? req.getAvatarUrl().trim() : null;
        usuario.setAvatarUrl((avatarUrl != null && !avatarUrl.isEmpty()) ? avatarUrl : null);

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    /**
     * Cambia la contraseña del usuario validando previamente la contraseña actual.
     */
        public UsuarioResponse actualizarAvatar(String email, String avatarUrl) {
        Usuario usuario = buscarPorEmail(email);
        String limpia = avatarUrl != null ? avatarUrl.trim() : null;
        usuario.setAvatarUrl((limpia != null && !limpia.isEmpty() && !"null".equalsIgnoreCase(limpia) && !"undefined".equalsIgnoreCase(limpia)) ? limpia : null);
        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    public void cambiarPassword(String email, CambiarPasswordRequest req) {
        Usuario usuario = buscarPorEmail(email);

        if (!passwordEncoder.matches(req.getPasswordActual(), usuario.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(req.getPasswordNuevo()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .avatarUrl(usuario.getAvatarUrl())
                .build();
    }

}
