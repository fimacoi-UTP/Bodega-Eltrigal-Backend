package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Producto.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Obtiene únicamente los productos que se encuentran con activo = true.
     *
     * @return Lista de productos activos
     */
    List<Producto> findByActivoTrue();

}
