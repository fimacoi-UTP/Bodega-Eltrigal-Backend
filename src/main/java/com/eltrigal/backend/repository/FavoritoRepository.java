package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Favorito.
 */
@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    /**
     * Lista los favoritos de un usuario ordenados del más reciente al más antiguo.
     */
    List<Favorito> findByUsuarioIdOrderByIdDesc(Long usuarioId);

    /**
     * Verifica si existe un producto guardado como favorito por el usuario.
     */
    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    /**
     * Obtiene el registro de favorito entre un usuario y un producto si existe.
     */
    Optional<Favorito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    /**
     * Elimina el favorito para un usuario y producto específicos.
     */
    @Modifying
    @Transactional
    int deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

}
