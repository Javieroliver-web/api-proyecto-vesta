package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_id")
    private Long id;

    @Column(name = "usu_nombre_completo", length = 50, nullable = false)
    private String nombreCompleto;

    @Column(name = "usu_movil", length = 15)
    private String movil;

    @Column(name = "usu_email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "usu_password", nullable = false)
    private String password;

    @Column(name = "usu_rol", length = 20, nullable = false)
    private String rol;

    @Column(name = "usu_email_confirmado")
    private Boolean emailConfirmado = false;

    @Column(name = "usu_fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // === CAMPOS RGPD ===
    
    @Column(name = "usu_acepta_terminos")
    private Boolean aceptaTerminos = false;

    @Column(name = "usu_acepta_privacidad")
    private Boolean aceptaPrivacidad = false;

    @Column(name = "usu_fecha_aceptacion_terminos")
    private LocalDateTime fechaAceptacionTerminos;

    @Column(name = "usu_acepta_comunicaciones")
    private Boolean aceptaComunicaciones = false; // Marketing

    @Column(name = "usu_datos_eliminados")
    private Boolean datosEliminados = false; // Soft delete para cumplir RGPD

    @Column(name = "usu_fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @Column(name = "usu_razon_eliminacion", length = 200)
    private String razonEliminacion;
}