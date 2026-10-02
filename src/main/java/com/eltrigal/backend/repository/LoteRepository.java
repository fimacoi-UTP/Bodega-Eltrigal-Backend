package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Lote.
 */
@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    /**
     * Obtiene los lotes activos de un producto.
     */
    List<Lote> findByProductoIdAndActivoTrue(Long productoId);

    /**
     * Obtiene los lotes activos cuya fecha de vencimiento sea menor o igual a la fecha indicada.
     */
    List<Lote> findByActivoTrueAndFechaVencimientoLessThanEqual(LocalDate fecha);

}
