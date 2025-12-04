package com.vesta.api.repository;

import com.vesta.api.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}