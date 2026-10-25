package com.eltrigal.backend.repository;

import com.eltrigal.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * Obtiene los productos activos que además están marcados como visibles en la tienda web.
     *
     * @return Lista de productos públicos para la tienda
     */
    List<Producto> findByActivoTrueAndVisibleWebTrue();

    /**
     * Busca un producto por su código de barras.
     */
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    /**
     * Verifica si existe un producto con el código de barras especificado.
     */
    boolean existsByCodigoBarras(String codigoBarras);

    /**
     * Obtiene todos los productos que no tienen código de barras asignado (NULL).
     */
    List<Producto> findByCodigoBarrasIsNull();

}
