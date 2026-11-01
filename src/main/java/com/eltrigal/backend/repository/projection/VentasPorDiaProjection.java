package com.eltrigal.backend.repository.projection;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Proyección para agregación de ventas por día.
 */
public interface VentasPorDiaProjection {
    Date getFecha();
    BigDecimal getTotal();
    Long getCantidad();
}

