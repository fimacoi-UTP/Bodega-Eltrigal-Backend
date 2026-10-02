package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.HistorialEstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad HistorialEstadoPedido.
 */
@Repository
public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Long> {
}
