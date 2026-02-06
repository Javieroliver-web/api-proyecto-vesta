package com.vesta.api.repository;

import com.vesta.api.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Orden> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    List<Orden> findByEstado(String estado, org.springframework.data.domain.Sort sort);

    @org.springframework.data.jpa.repository.Query("SELECT YEAR(o.fecha) as year, MONTH(o.fecha) as month, SUM(o.total) as total FROM Orden o GROUP BY YEAR(o.fecha), MONTH(o.fecha) ORDER BY year, month")
    List<java.util.Map<String, Object>> findSalesStatsByYearMonth();
}