package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.DetalleVenta;
import com.eltrigal.backend.repository.projection.CategoriaTotalProjection;
import com.eltrigal.backend.repository.projection.ProductoTotalProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad DetalleVenta.
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Agrupa el monto vendido por categoría de producto para ventas no anuladas.
     */
    @Query("SELECT c.nombre AS categoria, COALESCE(SUM(dv.subtotal), 0) AS total " +
           "FROM DetalleVenta dv JOIN dv.producto p JOIN p.categoria c " +
           "WHERE dv.venta.anulada = false " +
           "GROUP BY c.nombre")
    List<CategoriaTotalProjection> sumVentasPorCategoria();

    /**
     * Agrupa la cantidad y monto vendido por producto para ventas no anuladas.
     */
    @Query("SELECT p.id AS productoId, p.nombre AS producto, " +
           "COALESCE(SUM(dv.cantidad), 0) AS cantidad, " +
           "COALESCE(SUM(dv.subtotal), 0) AS total " +
           "FROM DetalleVenta dv JOIN dv.producto p " +
           "WHERE dv.venta.anulada = false " +
           "GROUP BY p.id, p.nombre")
    List<ProductoTotalProjection> sumVentasPorProducto();

}
