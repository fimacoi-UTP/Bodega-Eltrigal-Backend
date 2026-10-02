package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Venta.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Consulta las ventas registradas dentro de un rango de fechas.
     */
    List<Venta> findByCreadoEnBetween(LocalDateTime inicio, LocalDateTime fin);

}
