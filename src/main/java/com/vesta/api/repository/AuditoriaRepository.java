package com.vesta.api.repository;

import com.vesta.api.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findAllByOrderByFechaDesc();

    List<Auditoria> findByUsuarioEmailOrderByFechaDesc(String usuarioEmail);

    org.springframework.data.domain.Page<Auditoria> findByUsuarioEmailContainingIgnoreCaseOrAccionContainingIgnoreCaseOrDetalleContainingIgnoreCaseOrIpContainingIgnoreCaseOrderByFechaDesc(
            String usuarioEmail, String accion, String detalle, String ip,
            org.springframework.data.domain.Pageable pageable);
}
