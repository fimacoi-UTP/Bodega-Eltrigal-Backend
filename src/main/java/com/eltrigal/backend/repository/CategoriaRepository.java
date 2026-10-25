package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Categoria.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /**
     * Obtiene las categorías activas ordenadas alfabéticamente por nombre.
     */
    List<Categoria> findByActivoTrueOrderByNombreAsc();

    /**
     * Busca una categoría por nombre ignorando mayúsculas/minúsculas.
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
