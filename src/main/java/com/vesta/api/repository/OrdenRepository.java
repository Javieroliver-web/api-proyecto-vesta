package com.vesta.api.repository;

import com.vesta.api.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT CAST(EXTRACT(MONTH FROM ord_fecha) AS INTEGER) as mes, SUM(ord_total) as total FROM ordenes GROUP BY mes ORDER BY mes", nativeQuery = true)
    List<java.util.Map<String, Object>> obtenerVentasPorMes();
}