package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "seguros")
@Data
public class Seguro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seg_id")
    private String id; // Usamos UUID como String para compatibilidad con el frontend

    @Column(name = "seg_nombre", nullable = false)
    private String nombre;

    @Column(name = "seg_categoria")
    private String categoria;

    @Column(name = "seg_desc_corta")
    private String descripcionCorta;

    @Column(name = "seg_desc_larga", length = 1000)
    private String descripcion;

    @Column(name = "seg_precio_base")
    private BigDecimal precioBase;

    @Column(name = "seg_duracion")
    private String duracion; // Ej: "Por día", "Por mes"

    @Column(name = "seg_img_url")
    private String imageUrl;
}