package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.DetallePedido;
import com.eltrigal.backend.repository.projection.CategoriaTotalProjection;
import com.eltrigal.backend.repository.projection.ProductoTotalProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad DetallePedido.
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Agrupa el monto vendido por categoría de producto para pedidos no cancelados.
     */
    @Query("SELECT c.nombre AS categoria, COALESCE(SUM(dp.subtotal), 0) AS total " +
           "FROM DetallePedido dp JOIN dp.producto p JOIN p.categoria c " +
           "WHERE dp.pedido.estado <> 'CANCELADO' " +
           "GROUP BY c.nombre")
    List<CategoriaTotalProjection> sumPedidosPorCategoria();

    /**
     * Agrupa la cantidad y monto vendido por producto para pedidos no cancelados.
     */
    @Query("SELECT p.id AS productoId, p.nombre AS producto, " +
           "COALESCE(SUM(dp.cantidad), 0) AS cantidad, " +
           "COALESCE(SUM(dp.subtotal), 0) AS total " +
           "FROM DetallePedido dp JOIN dp.producto p " +
           "WHERE dp.pedido.estado <> 'CANCELADO' " +
           "GROUP BY p.id, p.nombre")
    List<ProductoTotalProjection> sumPedidosPorProducto();

}
