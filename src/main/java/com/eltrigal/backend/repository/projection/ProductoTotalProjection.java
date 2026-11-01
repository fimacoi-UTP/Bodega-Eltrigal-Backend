package com.eltrigal.backend.repository.projection;

import java.math.BigDecimal;

/**
 * Proyección para agregación de top productos vendidos.
 */
public interface ProductoTotalProjection {
    Long getProductoId();
    String getProducto();
    Long getCantidad();
    BigDecimal getTotal();
}

