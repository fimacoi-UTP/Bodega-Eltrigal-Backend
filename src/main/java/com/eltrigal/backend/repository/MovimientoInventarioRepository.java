package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad MovimientoInventario.
 */
@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Obtiene el historial de movimientos de inventario de un producto ordenados descendentemente por fecha.
     */
    List<MovimientoInventario> findByProductoIdOrderByCreadoEnDesc(Long productoId);

}
