package com.vesta.api.repository;

import com.vesta.api.entity.RecompensaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecompensaUsuarioRepository extends JpaRepository<RecompensaUsuario, Long> {
    Optional<RecompensaUsuario> findByUsuarioId(Long usuarioId);
}