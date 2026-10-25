package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.DireccionRequest;
import com.eltrigal.backend.dto.DireccionResponse;
import com.eltrigal.backend.entity.Direccion;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.DireccionRepository;
import com.eltrigal.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la gestión de direcciones de entrega de los clientes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Lista las direcciones activas del usuario autenticado ordenadas por predeterminada descendente.
     */
    @Transactional(readOnly = true)
    public List<DireccionResponse> listar(String email) {
        Usuario usuario = resolverUsuario(email);
        return direccionRepository.findByUsuarioIdAndActivoTrueOrderByPredeterminadaDescIdDesc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Registra una nueva dirección para el usuario.
     * Si es la primera dirección o viene predeterminada=true, se guarda como predeterminada
     * y se desmarcan las anteriores.
     */
    public DireccionResponse crear(String email, DireccionRequest req) {
        Usuario usuario = resolverUsuario(email);

        long totalActivas = direccionRepository.countByUsuarioIdAndActivoTrue(usuario.getId());
        boolean esPredeterminada = (totalActivas == 0) || Boolean.TRUE.equals(req.getPredeterminada());

        if (esPredeterminada) {
            direccionRepository.desmarcarPredeterminadas(usuario.getId());
        }

        String ciudad = (req.getCiudad() != null && !req.getCiudad().isBlank())
                ? req.getCiudad().trim()
                : "Piura";

        Direccion nueva = Direccion.builder()
                .usuario(usuario)
                .etiqueta(req.getEtiqueta().trim())
                .destinatario(limpiarTexto(req.getDestinatario()))
                .telefono(limpiarTexto(req.getTelefono()))
                .direccion(req.getDireccion().trim())
                .referencia(limpiarTexto(req.getReferencia()))
                .distrito(limpiarTexto(req.getDistrito()))
                .ciudad(ciudad)
                .predeterminada(esPredeterminada)
                .activo(true)
                .build();

        Direccion guardada = direccionRepository.save(nueva);
        return toResponse(guardada);
    }

    /**
     * Actualiza una dirección existente verificando la propiedad del usuario.
     */
    public DireccionResponse actualizar(String email, Long id, DireccionRequest req) {
        Usuario usuario = resolverUsuario(email);
        Direccion dir = direccionRepository.findByIdAndUsuarioIdAndActivoTrue(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada o no pertenece al usuario: " + id));

        dir.setEtiqueta(req.getEtiqueta().trim());
        dir.setDestinatario(limpiarTexto(req.getDestinatario()));
        dir.setTelefono(limpiarTexto(req.getTelefono()));
        dir.setDireccion(req.getDireccion().trim());
        dir.setReferencia(limpiarTexto(req.getReferencia()));
        dir.setDistrito(limpiarTexto(req.getDistrito()));

        if (req.getCiudad() != null && !req.getCiudad().isBlank()) {
            dir.setCiudad(req.getCiudad().trim());
        }

        if (Boolean.TRUE.equals(req.getPredeterminada())) {
            if (!Boolean.TRUE.equals(dir.getPredeterminada())) {
                direccionRepository.desmarcarPredeterminadas(usuario.getId());
                dir.setPredeterminada(true);
            }
        } else if (Boolean.FALSE.equals(req.getPredeterminada()) && Boolean.TRUE.equals(dir.getPredeterminada())) {
            dir.setPredeterminada(false);
            // Si hay otras activas, se promueve una de ellas; si era la única, se mantiene predeterminada
            long totalActivas = direccionRepository.countByUsuarioIdAndActivoTrue(usuario.getId());
            if (totalActivas <= 1) {
                dir.setPredeterminada(true);
            } else {
                direccionRepository.findFirstByUsuarioIdAndActivoTrueOrderByIdDesc(usuario.getId())
                        .filter(otra -> !otra.getId().equals(dir.getId()))
                        .ifPresent(otra -> {
                            otra.setPredeterminada(true);
                            direccionRepository.save(otra);
                        });
            }
        }

        Direccion guardada = direccionRepository.save(dir);
        return toResponse(guardada);
    }

    /**
     * Realiza el borrado lógico de una dirección. Si era la predeterminada,
     * promueve la dirección activa más reciente como predeterminada si existe.
     */
    public void eliminar(String email, Long id) {
        Usuario usuario = resolverUsuario(email);
        Direccion dir = direccionRepository.findByIdAndUsuarioIdAndActivoTrue(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada o no pertenece al usuario: " + id));

        boolean eraPredeterminada = Boolean.TRUE.equals(dir.getPredeterminada());
        dir.setActivo(false);
        dir.setPredeterminada(false);
        direccionRepository.save(dir);

        if (eraPredeterminada) {
            direccionRepository.findFirstByUsuarioIdAndActivoTrueOrderByIdDesc(usuario.getId())
                    .ifPresent(otra -> {
                        otra.setPredeterminada(true);
                        direccionRepository.save(otra);
                    });
        }
    }

    /**
     * Marca una dirección como predeterminada y desmarca las demás del usuario.
     */
    public DireccionResponse marcarPredeterminada(String email, Long id) {
        Usuario usuario = resolverUsuario(email);
        Direccion dir = direccionRepository.findByIdAndUsuarioIdAndActivoTrue(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada o no pertenece al usuario: " + id));

        direccionRepository.desmarcarPredeterminadas(usuario.getId());
        dir.setPredeterminada(true);
        Direccion guardada = direccionRepository.save(dir);

        return toResponse(guardada);
    }

    private Usuario resolverUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private DireccionResponse toResponse(Direccion d) {
        return DireccionResponse.builder()
                .id(d.getId())
                .etiqueta(d.getEtiqueta())
                .destinatario(d.getDestinatario())
                .telefono(d.getTelefono())
                .direccion(d.getDireccion())
                .referencia(d.getReferencia())
                .distrito(d.getDistrito())
                .ciudad(d.getCiudad())
                .predeterminada(d.getPredeterminada())
                .activo(d.getActivo())
                .creadoEn(d.getCreadoEn())
                .actualizadoEn(d.getActualizadoEn())
                .build();
    }

}
