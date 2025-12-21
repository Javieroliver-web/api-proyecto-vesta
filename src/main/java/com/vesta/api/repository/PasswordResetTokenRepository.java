package com.vesta.api.repository;

import com.vesta.api.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para tokens de recuperación de contraseña
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token por su valor
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina todos los tokens de un usuario
     */
    void deleteByUsuarioId(Long usuarioId);

    /**
     * Elimina tokens expirados
     */
    void deleteByFechaExpiracionBefore(LocalDateTime fecha);

    /**
     * Busca tokens válidos (no usados y no expirados) para un usuario
     */
    Optional<PasswordResetToken> findByUsuarioIdAndUsadoFalseAndFechaExpiracionAfter(
            Long usuarioId, LocalDateTime fecha);
}
