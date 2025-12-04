package com.vesta.api.repository;

import com.vesta.api.entity.ConsentimientoCookies;
import com.vesta.api.entity.SolicitudDatos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentimientoCookiesRepository extends JpaRepository<ConsentimientoCookies, Long> {
    Optional<ConsentimientoCookies> findTopByUsuarioIdOrderByFechaConsentimientoDesc(Long usuarioId);
    List<ConsentimientoCookies> findByUsuarioId(Long usuarioId);
}

@Repository
public interface SolicitudDatosRepository extends JpaRepository<SolicitudDatos, Long> {
    List<SolicitudDatos> findByUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId);
    List<SolicitudDatos> findByEstado(String estado);
    List<SolicitudDatos> findByTipoSolicitud(String tipoSolicitud);
}