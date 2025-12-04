package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Data
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ord_id")
    private Long id;

    @Column(name = "ord_usuario_id")
    private Long usuarioId;

    @Column(name = "ord_fecha")
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "ord_total")
    private BigDecimal total;

    @Column(name = "ord_estado")
    private String estado; // PENDIENTE, COMPLETADA

    @Column(name = "ord_referencia")
    private String referencia; // Código único de pedido

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    private List<OrdenItem> items;
}