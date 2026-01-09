package com.vesta.api.repository;

import com.vesta.api.entity.SolicitudDatos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudDatosRepository extends JpaRepository<SolicitudDatos, Long> {
    List<SolicitudDatos> findByUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId);
    List<SolicitudDatos> findByEstado(String estado);
    List<SolicitudDatos> findByTipoSolicitud(String tipoSolicitud);
}