package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Marca.
 */
@Repository
public interface MarcaRepository extends JpaRepository<Marca, Integer> {
}
