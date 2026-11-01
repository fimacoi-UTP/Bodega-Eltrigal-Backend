package com.eltrigal.backend.repository.projection;

import java.math.BigDecimal;

/**
 * Proyección para agregación de ventas por categoría de producto.
 */
public interface CategoriaTotalProjection {
    String getCategoria();
    BigDecimal getTotal();
}

