package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Pedido.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Consulta pedidos filtrados por su estado.
     */
    List<Pedido> findByEstado(String estado);

}
