package com.eltrigal.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla 'direccion' de entrega de los clientes.
 */
@Entity
@Table(name = "direccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "etiqueta", length = 50, nullable = false)
    private String etiqueta;

    @Column(name = "destinatario", length = 150)
    private String destinatario;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 255, nullable = false)
    private String direccion;

    @Column(name = "referencia", length = 255)
    private String referencia;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(name = "ciudad", length = 100, nullable = false)
    @Builder.Default
    private String ciudad = "Piura";

    @Column(name = "predeterminada", nullable = false)
    @Builder.Default
    private Boolean predeterminada = false;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
        this.actualizadoEn = LocalDateTime.now();
        if (this.ciudad == null || this.ciudad.isBlank()) {
            this.ciudad = "Piura";
        }
        if (this.predeterminada == null) {
            this.predeterminada = false;
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

}
