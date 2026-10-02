package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad DetalleVenta.
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
}
