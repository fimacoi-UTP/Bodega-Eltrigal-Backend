package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Venta;
import com.eltrigal.backend.repository.projection.MetodoPagoTotalProjection;
import com.eltrigal.backend.repository.projection.VentasPorDiaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    /**
     * Verifica si existen ventas asociadas a un usuario específico.
     */
    boolean existsByIdUsuario(Long idUsuario);

    /**
     * Suma los montos totales de ventas no anuladas en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.anulada = false AND v.creadoEn >= :inicio AND v.creadoEn < :fin")
    BigDecimal sumTotalBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Cuenta el total de ventas no anuladas en un rango de fechas.
     */
    @Query("SELECT COUNT(v.id) FROM Venta v WHERE v.anulada = false AND v.creadoEn >= :inicio AND v.creadoEn < :fin")
    long countBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Agrupa las ventas no anuladas por fecha a partir de una fecha/hora inicial.
     */
    @Query(value = "SELECT DATE(v.creado_en) AS fecha, COALESCE(SUM(v.total), 0) AS total, COUNT(*) AS cantidad " +
                   "FROM venta v " +
                   "WHERE v.anulada = false AND v.creado_en >= :inicio " +
                   "GROUP BY DATE(v.creado_en)", nativeQuery = true)
    List<VentasPorDiaProjection> sumVentasPorDia(@Param("inicio") LocalDateTime inicio);

    /**
     * Agrupa las ventas no anuladas por método de pago.
     */
    @Query("SELECT v.metodoPago AS metodo, COALESCE(SUM(v.total), 0) AS total, COUNT(v.id) AS cantidad " +
           "FROM Venta v WHERE v.anulada = false GROUP BY v.metodoPago")
    List<MetodoPagoTotalProjection> sumVentasPorMetodoPago();

}
