package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "logros")
@Data
public class Logro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo; // PRIMERA_POLIZA, SIN_SINIESTROS_1Y
    private String nombre;
    private String descripcion;
    private Integer puntosRecompensa;
    private String iconoUrl;
    private LocalDateTime fechaDesbloqueo;
    
    @ManyToOne
    @JoinColumn(name = "recompensa_id")
    private RecompensaUsuario recompensa;
}