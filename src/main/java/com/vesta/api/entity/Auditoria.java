package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuarioEmail;

    @Column(nullable = false)
    private String accion;

    @Column(length = 1000)
    private String detalle;

    private String ip;

    private LocalDateTime fecha = LocalDateTime.now();

    public Auditoria(String usuarioEmail, String accion, String detalle, String ip) {
        this.usuarioEmail = usuarioEmail;
        this.accion = accion;
        this.detalle = detalle;
        this.ip = ip;
        this.fecha = LocalDateTime.now();
    }
}
