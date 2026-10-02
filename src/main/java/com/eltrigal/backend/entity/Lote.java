package com.eltrigal.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla 'lote'.
 */
@Entity
@Table(name = "lote")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "codigo_lote", length = 40)
    private String codigoLote;

    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "costo_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoUnitario;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @CreationTimestamp
    @Column(name = "fecha_ingreso", updatable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

}
