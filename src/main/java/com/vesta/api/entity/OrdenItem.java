package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_items")
@Data
public class OrdenItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itm_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "itm_orden_id")
    @JsonIgnore // Evitar bucles infinitos al serializar
    private Orden orden;

    @Column(name = "itm_seguro_id")
    private String seguroId;

    @Column(name = "itm_nombre_seguro")
    private String nombreSeguro;

    @Column(name = "itm_cantidad")
    private Integer cantidad;

    @Column(name = "itm_precio_unitario")
    private BigDecimal precioUnitario;

    @Column(name = "itm_subtotal")
    private BigDecimal subtotal;
}