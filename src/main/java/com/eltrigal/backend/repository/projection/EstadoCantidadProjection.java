package com.eltrigal.backend.repository.projection;

/**
 * Proyección para agregación de conteo de pedidos por estado.
 */
public interface EstadoCantidadProjection {
    String getEstado();
    Long getCantidad();
}

