package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad DetallePedido.
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}
