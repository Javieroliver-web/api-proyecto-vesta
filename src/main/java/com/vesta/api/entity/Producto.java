package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data @NoArgsConstructor @AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_id")
    private Long id;

    @Column(name = "prod_nombre", nullable = false)
    private String nombre;

    @Column(name = "prod_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "prod_precio_base", nullable = false)
    private BigDecimal precioBase;

    @Column(name = "prod_categoria") // Tecnología, Movilidad, Viajes...
    private String categoria;

    @Column(name = "prod_imagen")
    private String imagenUrl;
}