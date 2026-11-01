package com.eltrigal.backend.repository.projection;

import java.math.BigDecimal;

/**
 * Proyección para agregación de ventas por método de pago.
 */
public interface MetodoPagoTotalProjection {
    String getMetodo();
    BigDecimal getTotal();
    Long getCantidad();
}

