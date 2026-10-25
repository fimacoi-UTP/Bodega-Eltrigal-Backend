package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Direccion.
 */
@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    /**
     * Obtiene las direcciones activas de un usuario ordenadas por predeterminada descendente y luego id descendente.
     */
    List<Direccion> findByUsuarioIdAndActivoTrueOrderByPredeterminadaDescIdDesc(Long usuarioId);

    /**
     * Busca una dirección activa específica por su ID y el ID del usuario propietario.
     */
    Optional<Direccion> findByIdAndUsuarioIdAndActivoTrue(Long id, Long usuarioId);

    /**
     * Cuenta cuántas direcciones activas posee el usuario.
     */
    long countByUsuarioIdAndActivoTrue(Long usuarioId);

    /**
     * Encuentra la dirección activa más reciente del usuario para promoverla a predeterminada si es necesario.
     */
    Optional<Direccion> findFirstByUsuarioIdAndActivoTrueOrderByIdDesc(Long usuarioId);

    /**
     * Desmarca todas las direcciones predeterminadas activas del usuario.
     */
    @Modifying
    @Query("UPDATE Direccion d SET d.predeterminada = false WHERE d.usuario.id = :usuarioId AND d.activo = true")
    int desmarcarPredeterminadas(@Param("usuarioId") Long usuarioId);

}
