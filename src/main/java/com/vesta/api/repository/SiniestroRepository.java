package com.vesta.api.repository;

import com.vesta.api.entity.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT new map(p.categoria as categoria, COUNT(s) as cantidad) FROM Siniestro s JOIN s.poliza pol JOIN pol.producto p GROUP BY p.categoria")
    java.util.List<java.util.Map<String, Object>> obtenerSiniestrosPorCategoria();

    void deleteByPoliza(com.vesta.api.entity.Poliza poliza); // Para cascading delete
}