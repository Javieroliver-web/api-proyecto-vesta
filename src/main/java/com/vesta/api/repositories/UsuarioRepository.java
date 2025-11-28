package com.vesta.api.repositories;

import com.vesta.api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método mágico: Spring crea el SQL automáticamente al leer el nombre
    Optional<Usuario> findByEmail(String email);
    
    // Para verificar si existe al registrar
    boolean existsByEmail(String email);
}