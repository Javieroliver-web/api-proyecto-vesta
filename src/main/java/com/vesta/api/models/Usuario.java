package com.vesta.api.models;

import jakarta.persistence.*;
import lombok.Data; // Genera getters y setters automático
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuarios") // El nombre que tendrá en Postgres
@Data @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_id")
    private Long id;

    // REQUISITO: Limitar a 50 caracteres
    @Column(name = "usu_nombre_completo", length = 50, nullable = false)
    private String nombreCompleto;

    // REQUISITO: Formato móvil
    @Column(name = "usu_movil", length = 15, nullable = false)
    private String movil;

    // REQUISITO: Email único
    @Column(name = "usu_email", length = 100, nullable = false, unique = true)
    private String email;

    // REQUISITO: Encriptada (lo guardaremos como hash)
    @Column(name = "usu_password", nullable = false)
    private String password;

    // REQUISITO: Roles (admin, cliente)
    @Column(name = "usu_rol", length = 20, nullable = false)
    private String rol;

    // REQUISITO: Token de recuperación/activación
    @Column(name = "usu_token")
    private String token;
}