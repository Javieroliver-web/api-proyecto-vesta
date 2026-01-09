package com.vesta.api.repository;

import com.vesta.api.entity.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolizaRepository extends JpaRepository<Poliza, Long> {
    List<Poliza> findByUsuarioId(Long usuarioId); // Para "Mis Seguros"
}