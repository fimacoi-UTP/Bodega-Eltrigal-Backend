package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Promocion.
 */
@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    /**
     * Obtiene las promociones activas cuya vigencia incluye la fecha dada (hoy),
     * contemplando fecha_inicio y fecha_fin nulos como sin límite.
     * Ordenadas por prioridad ASC y luego por id ASC.
     */
    @Query("SELECT p FROM Promocion p " +
           "WHERE p.activo = true " +
           "AND (p.fechaInicio IS NULL OR p.fechaInicio <= :hoy) " +
           "AND (p.fechaFin IS NULL OR p.fechaFin >= :hoy) " +
           "ORDER BY p.prioridad ASC, p.id ASC")
    List<Promocion> findVigentes(@Param("hoy") LocalDate hoy);

    /**
     * Lista todas las promociones ordenadas por prioridad ASC e id ASC.
     */
    List<Promocion> findAllByOrderByPrioridadAscIdAsc();

}
