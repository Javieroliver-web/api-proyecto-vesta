package com.vesta.api.repository;

import com.vesta.api.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    @org.springframework.data.jpa.repository.Query("SELECT new map(FUNCTION('MONTH', o.fecha) as mes, SUM(o.total) as total) FROM Orden o GROUP BY FUNCTION('MONTH', o.fecha) ORDER BY FUNCTION('MONTH', o.fecha)")
    List<java.util.Map<String, Object>> obtenerVentasPorMes();
}