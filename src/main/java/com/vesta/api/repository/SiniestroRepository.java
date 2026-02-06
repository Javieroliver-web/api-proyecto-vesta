package com.vesta.api.repository;

import com.vesta.api.entity.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT p.categoria as categoria, COUNT(s) as cantidad FROM Siniestro s JOIN s.poliza pol JOIN pol.producto p GROUP BY p.categoria")
    List<java.util.Map<String, Object>> countSiniestrosByCategoria();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Siniestro s WHERE s.poliza IS NULL OR s.poliza.producto IS NULL")
    Long countSiniestrosSinCategoria();
}