package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Pedido;
import com.eltrigal.backend.repository.projection.EstadoCantidadProjection;
import com.eltrigal.backend.repository.projection.MetodoPagoTotalProjection;
import com.eltrigal.backend.repository.projection.VentasPorDiaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Pedido.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Consulta pedidos filtrados por su estado.
     */
    List<Pedido> findByEstado(String estado);

    /**
     * Lista los pedidos de un usuario ordenados por id descendentemente.
     */
    List<Pedido> findByIdUsuarioOrderByIdDesc(Long idUsuario);

    /**
     * Busca un pedido por su número único de pedido.
     */
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    /**
     * Vincula pedidos realizados como invitado (idUsuario IS NULL) a un usuario
     * registrado por correo.
     */
    @Modifying
    @Query("UPDATE Pedido p SET p.idUsuario = :idUsuario " +
            "WHERE p.idUsuario IS NULL AND LOWER(p.clienteCorreo) = :correo")
    int vincularPedidosInvitado(@Param("idUsuario") Long idUsuario, @Param("correo") String correo);

    /**
     * Verifica si existen pedidos asociados a un usuario específico.
     */
    boolean existsByIdUsuario(Long idUsuario);

    /**
     * Suma los montos totales de pedidos no cancelados en un rango de fechas.
     */
    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado <> 'CANCELADO' AND p.creadoEn >= :inicio AND p.creadoEn < :fin")
    BigDecimal sumTotalBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Cuenta el total de pedidos no cancelados en un rango de fechas.
     */
    @Query("SELECT COUNT(p.id) FROM Pedido p WHERE p.estado <> 'CANCELADO' AND p.creadoEn >= :inicio AND p.creadoEn < :fin")
    long countBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Cuenta pedidos agrupados por estado.
     */
    @Query("SELECT p.estado AS estado, COUNT(p.id) AS cantidad FROM Pedido p GROUP BY p.estado")
    List<EstadoCantidadProjection> countPedidosPorEstado();

    /**
     * Agrupa los pedidos no cancelados por fecha a partir de una fecha/hora inicial.
     */
    @Query(value = "SELECT DATE(p.creado_en) AS fecha, COALESCE(SUM(p.total), 0) AS total, COUNT(*) AS cantidad " +
                   "FROM pedido p " +
                   "WHERE p.estado <> 'CANCELADO' AND p.creado_en >= :inicio " +
                   "GROUP BY DATE(p.creado_en)", nativeQuery = true)
    List<VentasPorDiaProjection> sumPedidosPorDia(@Param("inicio") LocalDateTime inicio);

    /**
     * Agrupa los pedidos no cancelados por método de pago.
     */
    @Query("SELECT p.metodoPago AS metodo, COALESCE(SUM(p.total), 0) AS total, COUNT(p.id) AS cantidad " +
           "FROM Pedido p WHERE p.estado <> 'CANCELADO' GROUP BY p.metodoPago")
    List<MetodoPagoTotalProjection> sumPedidosPorMetodoPago();

}
