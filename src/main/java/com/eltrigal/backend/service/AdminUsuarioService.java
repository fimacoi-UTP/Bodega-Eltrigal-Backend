package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.ActualizarUsuarioRequest;
import com.eltrigal.backend.dto.CrearUsuarioRequest;
import com.eltrigal.backend.dto.UsuarioAdminResponse;
import com.eltrigal.backend.entity.Rol;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ConflictException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.PedidoRepository;
import com.eltrigal.backend.repository.RolRepository;
import com.eltrigal.backend.repository.UsuarioRepository;
import com.eltrigal.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión administrativa de usuarios (CRUD de administradores).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PedidoRepository pedidoRepository;
    private final VentaRepository ventaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos los usuarios registrados en el sistema ordenados por ID ascendente.
     */
    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listar() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Crea un nuevo usuario validando unicidad de correo y existencia del rol solicitado.
     */
    public UsuarioAdminResponse crear(CrearUsuarioRequest req) {
        String emailNormalizado = req.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ConflictException("El email ya está registrado");
        }

        Rol rol = rolRepository.findByNombre(req.getRol().trim().toUpperCase())
                .orElseThrow(() -> new BadRequestException("El rol '" + req.getRol() + "' no existe en el sistema"));

        Usuario usuario = new Usuario();
        usuario.setNombres(req.getNombres().trim());
        usuario.setApellidos(req.getApellidos().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        usuario.setRol(rol);
        usuario.setTelefono(limpiarTexto(req.getTelefono()));
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    /**
     * Actualiza los datos y el rol de un usuario existente.
     * Incluye salvaguarda para evitar que el admin en sesión se despoje del rol ADMIN.
     */
    public UsuarioAdminResponse actualizar(Long id, ActualizarUsuarioRequest req, String adminEmail) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        Usuario adminActual = resolverAdmin(adminEmail);

        // Salvaguarda anti-bloqueo: un admin no puede removerse el rol ADMIN a sí mismo
        if (usuario.getId().equals(adminActual.getId())) {
            if (!req.getRol().trim().equalsIgnoreCase("ADMIN")) {
                throw new BadRequestException("No puedes quitarte el rol ADMIN a ti mismo");
            }
        }

        Rol rol = rolRepository.findByNombre(req.getRol().trim().toUpperCase())
                .orElseThrow(() -> new BadRequestException("El rol '" + req.getRol() + "' no existe en el sistema"));

        usuario.setNombres(req.getNombres().trim());
        usuario.setApellidos(req.getApellidos().trim());
        usuario.setTelefono(limpiarTexto(req.getTelefono()));
        usuario.setRol(rol);

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    /**
     * Cambia el estado (activo/inactivo) de un usuario.
     * Incluye salvaguarda para evitar que el admin en sesión desactive su propia cuenta.
     */
    public UsuarioAdminResponse cambiarEstado(Long id, Boolean activo, String adminEmail) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        Usuario adminActual = resolverAdmin(adminEmail);

        // Salvaguarda anti-bloqueo: un admin no puede desactivarse a sí mismo
        if (usuario.getId().equals(adminActual.getId()) && Boolean.FALSE.equals(activo)) {
            throw new BadRequestException("No puedes desactivar tu propia cuenta de administrador");
        }

        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    /**
     * Resetea la contraseña de un usuario estableciendo el nuevo hash cifrado.
     */
    public void resetPassword(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        if (nuevaPassword == null || nuevaPassword.trim().length() < 6) {
            throw new BadRequestException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword.trim()));
        usuarioRepository.save(usuario);
    }

    /**
     * Elimina físicamente un usuario si no posee ventas ni pedidos asociados.
     * Incluye salvaguarda para evitar que el admin en sesión elimine su propia cuenta.
     */
    public void eliminar(Long id, String adminEmail) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        Usuario adminActual = resolverAdmin(adminEmail);

        if (usuario.getId().equals(adminActual.getId())) {
            throw new BadRequestException("No puedes eliminar tu propia cuenta.");
        }

        if (pedidoRepository.existsByIdUsuario(id) || ventaRepository.existsByIdUsuario(id)) {
            throw new ConflictException("No se puede eliminar: el usuario tiene pedidos o ventas asociados. Desactívalo en su lugar.");
        }

        usuarioRepository.deleteById(id);
    }

    private Usuario resolverAdmin(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado con email: " + email));
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private UsuarioAdminResponse toResponse(Usuario u) {
        return UsuarioAdminResponse.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .email(u.getEmail())
                .telefono(u.getTelefono())
                .rol(u.getRol() != null ? u.getRol().getNombre() : null)
                .activo(u.getActivo())
                .avatarUrl(u.getAvatarUrl())
                .creadoEn(u.getCreadoEn())
                .build();
    }

}
