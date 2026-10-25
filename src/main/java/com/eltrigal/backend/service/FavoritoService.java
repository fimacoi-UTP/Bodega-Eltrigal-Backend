package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.FavoritoResponse;
import com.eltrigal.backend.entity.Favorito;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.FavoritoRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import com.eltrigal.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de productos favoritos de los usuarios.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    /**
     * Resultado del registro de un favorito para distinguir si fue creado o ya existía.
     */
    public record ResultadoFavorito(FavoritoResponse response, boolean yaExistia) {}

    /**
     * Lista los favoritos del usuario autenticado ordenados del más reciente al más antiguo.
     */
    @Transactional(readOnly = true)
    public List<FavoritoResponse> listar(String email) {
        Usuario usuario = resolverUsuario(email);
        return favoritoRepository.findByUsuarioIdOrderByIdDesc(usuario.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Agrega un producto a favoritos. Si ya existía, no duplica y devuelve el existente.
     */
    public ResultadoFavorito agregar(String email, Long productoId) {
        Usuario usuario = resolverUsuario(email);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        Optional<Favorito> existente = favoritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), productoId);
        if (existente.isPresent()) {
            return new ResultadoFavorito(toResponse(existente.get()), true);
        }

        Favorito nuevo = Favorito.builder()
                .usuario(usuario)
                .producto(producto)
                .build();

        Favorito guardado = favoritoRepository.save(nuevo);
        return new ResultadoFavorito(toResponse(guardado), false);
    }

    /**
     * Elimina el favorito para el usuario y producto indicados. No falla si no existe.
     */
    public void quitar(String email, Long productoId) {
        Usuario usuario = resolverUsuario(email);
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuario.getId(), productoId);
    }

    private Usuario resolverUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private FavoritoResponse toResponse(Favorito fav) {
        return FavoritoResponse.builder()
                .idFavorito(fav.getId())
                .productoId(fav.getProducto() != null ? fav.getProducto().getId() : null)
                .productoNombre(fav.getProducto() != null ? fav.getProducto().getNombre() : null)
                .creadoEn(fav.getCreadoEn())
                .build();
    }

}
