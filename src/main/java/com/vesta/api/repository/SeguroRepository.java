package com.vesta.api.repository;

import com.vesta.api.entity.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, String> {

    /**
     * Buscar seguros por categoría
     * 
     * @param categoria Categoría del seguro
     * @return Lista de seguros de la categoría
     */
    List<Seguro> findByCategoria(String categoria);
}
