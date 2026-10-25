package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
