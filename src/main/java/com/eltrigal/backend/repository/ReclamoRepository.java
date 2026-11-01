package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.EstadoReclamo;
import com.eltrigal.backend.entity.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Reclamo.
 */
@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    List<Reclamo> findAllByOrderByFechaCreacionDesc();

    List<Reclamo> findByEstadoOrderByFechaCreacionDesc(EstadoReclamo estado);

    Optional<Reclamo> findByNumeroCorrelativo(String numeroCorrelativo);
}

