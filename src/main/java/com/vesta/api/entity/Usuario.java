package com.vesta.api.entity; // ANTES: models

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data @NoArgsConstructor @AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_id")
    private Long id;

    @Column(name = "usu_nombre_completo", length = 50, nullable = false)
    private String nombreCompleto;

    @Column(name = "usu_movil", length = 15, nullable = false)
    private String movil;

    @Column(name = "usu_email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "usu_password", nullable = false)
    private String password;

    @Column(name = "usu_rol", length = 20, nullable = false)
    private String rol;

    @Column(name = "usu_token")
    private String token;
}