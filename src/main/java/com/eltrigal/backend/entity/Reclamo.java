package com.eltrigal.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla 'reclamo' para el Libro de Reclamaciones (Indecopi).
 */
@Entity
@Table(name = "reclamo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero_correlativo", length = 30, unique = true)
    private String numeroCorrelativo;

    @Column(name = "nombres", length = 120, nullable = false)
    private String nombres;

    @Column(name = "apellido_paterno", length = 120, nullable = false)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 120)
    private String apellidoMaterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 20, nullable = false)
    @Builder.Default
    private TipoDocumento tipoDocumento = TipoDocumento.DNI;

    @Column(name = "numero_documento", length = 20, nullable = false)
    private String numeroDocumento;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_respuesta", length = 20, nullable = false)
    @Builder.Default
    private TipoRespuesta tipoRespuesta = TipoRespuesta.CORREO;

    @Column(name = "correo", length = 150, nullable = false)
    private String correo;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "urbanizacion", length = 150)
    private String urbanizacion;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(name = "menor_de_edad", nullable = false)
    @Builder.Default
    private boolean menorDeEdad = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_compra", length = 20)
    private CanalCompra canalCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", length = 20, nullable = false)
    private Motivo motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "relacionado_a", length = 20, nullable = false)
    private RelacionadoA relacionadoA;

    @Column(name = "numero_pedido", length = 50)
    private String numeroPedido;

    @Column(name = "descripcion_bien", length = 500)
    private String descripcionBien;

    @Column(name = "detalle", columnDefinition = "TEXT", nullable = false)
    private String detalle;

    @Column(name = "requerimiento", columnDefinition = "TEXT", nullable = false)
    private String requerimiento;

    @Column(name = "monto_reclamado", precision = 10, scale = 2)
    private BigDecimal montoReclamado;

    @Column(name = "acciones_tomadas", columnDefinition = "TEXT")
    private String accionesTomadas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    @Builder.Default
    private EstadoReclamo estado = EstadoReclamo.PENDIENTE;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite_respuesta")
    private LocalDateTime fechaLimiteRespuesta;
}

