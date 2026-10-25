package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Marca.
 */
@Repository
public interface MarcaRepository extends JpaRepository<Marca, Integer> {

    /**
     * Obtiene las marcas activas ordenadas alfabéticamente por nombre.
     */
    List<Marca> findByActivoTrueOrderByNombreAsc();

    /**
     * Busca una marca por nombre ignorando mayúsculas/minúsculas.
     */
    Optional<Marca> findByNombreIgnoreCase(String nombre);
}
