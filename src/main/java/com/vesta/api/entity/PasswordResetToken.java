package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad para tokens de recuperación de contraseña
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String token; // Código de 6 dígitos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean usado = false;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        // Token expira en 5 minutos
        fechaExpiracion = fechaCreacion.plusMinutes(5);
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Verifica si el token es válido (no usado y no expirado)
     */
    public boolean isValid() {
        return !usado && !isExpired();
    }
}
