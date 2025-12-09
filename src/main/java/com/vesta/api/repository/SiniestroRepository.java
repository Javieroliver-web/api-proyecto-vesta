package com.vesta.api.repository;

import com.vesta.api.entity.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiniestroRepository extends JpaRepository<Siniestro, Long> {
}