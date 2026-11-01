package com.eltrigal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que desglosa el número de pedidos agrupados por estado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidosEstadoKpiResponse {

    @Builder.Default
    private Long pendientes = 0L;

    @Builder.Default
    private Long confirmados = 0L;

    @Builder.Default
    private Long enPreparacion = 0L;

    @Builder.Default
    private Long enCamino = 0L;

    @Builder.Default
    private Long entregados = 0L;

    @Builder.Default
    private Long cancelados = 0L;

    @Builder.Default
    private Long total = 0L;
}

