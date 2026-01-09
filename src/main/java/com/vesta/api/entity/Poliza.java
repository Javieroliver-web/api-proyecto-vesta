package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "polizas")
@Data @NoArgsConstructor @AllArgsConstructor
public class Poliza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pol_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usu_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "prod_id", nullable = false)
    private Producto producto;

    @Column(name = "pol_fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "pol_fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "pol_precio_final")
    private BigDecimal precioFinal;

    @Column(name = "pol_estado") // Activa, Vencida
    private String estado;
}