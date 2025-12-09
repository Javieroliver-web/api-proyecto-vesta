package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "siniestros")
@Data @NoArgsConstructor @AllArgsConstructor
public class Siniestro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sin_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pol_id", nullable = false)
    private Poliza poliza;

    @Column(name = "sin_fecha")
    private LocalDate fecha;

    @Column(name = "sin_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "sin_estado") // Pendiente, Aprobado, Rechazado
    private String estado;

    // === CAMPOS NUEVOS PARA IA ===
    @Column(name = "sin_imagen_url")
    private String imagenUrl;

    @Column(name = "sin_analisis_ia", columnDefinition = "TEXT")
    private String analisisIA; // Aquí guardaremos lo que diga el "Perito Robot"
    
    @Column(name = "sin_fraude_score")
    private Integer fraudeScore; // 0-100 (Probabilidad de estafa)
}