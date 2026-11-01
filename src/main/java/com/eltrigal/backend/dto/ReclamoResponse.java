package com.eltrigal.backend.dto;

import com.eltrigal.backend.entity.CanalCompra;
import com.eltrigal.backend.entity.EstadoReclamo;
import com.eltrigal.backend.entity.Motivo;
import com.eltrigal.backend.entity.Reclamo;
import com.eltrigal.backend.entity.RelacionadoA;
import com.eltrigal.backend.entity.TipoDocumento;
import com.eltrigal.backend.entity.TipoRespuesta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta que expone la información completa de un reclamo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReclamoResponse {

    private Long id;
    private String numeroCorrelativo;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private TipoRespuesta tipoRespuesta;
    private String correo;
    private String direccion;
    private String urbanizacion;
    private String departamento;
    private String provincia;
    private String distrito;
    private boolean menorDeEdad;
    private CanalCompra canalCompra;
    private Motivo motivo;
    private RelacionadoA relacionadoA;
    private String numeroPedido;
    private String descripcionBien;
    private String detalle;
    private String requerimiento;
    private BigDecimal montoReclamado;
    private String accionesTomadas;
    private EstadoReclamo estado;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimiteRespuesta;

    public static ReclamoResponse fromEntity(Reclamo r) {
        if (r == null) {
            return null;
        }
        return ReclamoResponse.builder()
                .id(r.getId())
                .numeroCorrelativo(r.getNumeroCorrelativo())
                .nombres(r.getNombres())
                .apellidoPaterno(r.getApellidoPaterno())
                .apellidoMaterno(r.getApellidoMaterno())
                .tipoDocumento(r.getTipoDocumento())
                .numeroDocumento(r.getNumeroDocumento())
                .telefono(r.getTelefono())
                .tipoRespuesta(r.getTipoRespuesta())
                .correo(r.getCorreo())
                .direccion(r.getDireccion())
                .urbanizacion(r.getUrbanizacion())
                .departamento(r.getDepartamento())
                .provincia(r.getProvincia())
                .distrito(r.getDistrito())
                .menorDeEdad(r.isMenorDeEdad())
                .canalCompra(r.getCanalCompra())
                .motivo(r.getMotivo())
                .relacionadoA(r.getRelacionadoA())
                .numeroPedido(r.getNumeroPedido())
                .descripcionBien(r.getDescripcionBien())
                .detalle(r.getDetalle())
                .requerimiento(r.getRequerimiento())
                .montoReclamado(r.getMontoReclamado())
                .accionesTomadas(r.getAccionesTomadas())
                .estado(r.getEstado())
                .usuarioId(r.getUsuarioId())
                .fechaCreacion(r.getFechaCreacion())
                .fechaLimiteRespuesta(r.getFechaLimiteRespuesta())
                .build();
    }
}

