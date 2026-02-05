package com.vesta.api.repository; // ANTES: repositories

import com.vesta.api.entity.Usuario; // ANTES: models
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método mágico: Spring crea el SQL automáticamente al leer el nombre
    Optional<Usuario> findByEmail(String email);

    // Método case-insensitive para email
    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByConfirmationToken(String confirmationToken);

    // Búsqueda por móvil para Login con teléfono
    Optional<Usuario> findByMovil(String movil);

    // Para verificar si existe al registrar
    boolean existsByEmail(String email);

    // Contar usuarios por rol (para protección de último admin)
    long countByRol(String rol);

    // Búsqueda por ID de proveedor (OAuth support)
    Optional<Usuario> findByProviderId(String providerId);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuario u WHERE " +
            "(:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            +
            "(:role IS NULL OR u.rol = :role) AND " +
            "(:status IS NULL OR (:status = 'true' AND u.activo = true) OR (:status = 'false' AND u.activo = false))")
    org.springframework.data.domain.Page<Usuario> findAllFiltered(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("role") String role,
            @org.springframework.data.repository.query.Param("status") String status,
            org.springframework.data.domain.Pageable pageable);
}