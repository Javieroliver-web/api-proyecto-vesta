package com.vesta.api.service;

import com.vesta.api.entity.Seguro;
import com.vesta.api.repository.SeguroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de seguros
 * Contiene la lógica de negocio para operaciones con seguros
 */
@Service
public class SeguroService {

    private static final Logger logger = LoggerFactory.getLogger(SeguroService.class);

    @Autowired
    private SeguroRepository seguroRepository;

    /**
     * Obtener todos los seguros con paginación
     * 
     * @param pageable Configuración de paginación
     * @return Página de seguros
     */
    @Transactional(readOnly = true)
    public Page<Seguro> findAll(Pageable pageable) {
        logger.debug("Obteniendo seguros con paginación: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return seguroRepository.findAll(pageable);
    }

    /**
     * Obtener todos los seguros sin paginación
     * 
     * @return Lista de todos los seguros
     */
    @Transactional(readOnly = true)
    public List<Seguro> findAll() {
        logger.debug("Obteniendo todos los seguros");
        return seguroRepository.findAll();
    }

    /**
     * Buscar seguro por ID
     * 
     * @param id ID del seguro
     * @return Optional con el seguro si existe
     */
    @Transactional(readOnly = true)
    public Optional<Seguro> findById(String id) {
        logger.debug("Buscando seguro con ID: {}", id);
        return seguroRepository.findById(id);
    }

    /**
     * Buscar seguro por ID o lanzar excepción
     * 
     * @param id ID del seguro
     * @return Seguro encontrado
     * @throws RuntimeException si no se encuentra el seguro
     */
    @Transactional(readOnly = true)
    public Seguro findByIdOrThrow(String id) {
        return findById(id)
                .orElseThrow(() -> {
                    logger.warn("Seguro no encontrado con ID: {}", id);
                    return new RuntimeException("Seguro no encontrado con ID: " + id);
                });
    }

    /**
     * Buscar seguros por categoría
     * 
     * @param categoria Categoría del seguro
     * @return Lista de seguros de la categoría
     */
    @Transactional(readOnly = true)
    public List<Seguro> findByCategoria(String categoria) {
        logger.debug("Buscando seguros de categoría: {}", categoria);
        return seguroRepository.findByCategoria(categoria);
    }

    /**
     * Crear nuevo seguro
     * 
     * @param seguro Datos del seguro
     * @return Seguro creado
     */
    @Transactional
    public Seguro create(Seguro seguro) {
        logger.info("Creando nuevo seguro: {}", seguro.getNombre());
        return seguroRepository.save(seguro);
    }

    /**
     * Actualizar seguro existente
     * 
     * @param id     ID del seguro
     * @param seguro Datos actualizados
     * @return Seguro actualizado
     */
    @Transactional
    public Seguro update(String id, Seguro seguro) {
        logger.info("Actualizando seguro con ID: {}", id);

        Seguro existente = findByIdOrThrow(id);

        // Actualizar campos
        existente.setNombre(seguro.getNombre());
        existente.setCategoria(seguro.getCategoria());
        existente.setDescripcion(seguro.getDescripcion());
        existente.setDescripcionCorta(seguro.getDescripcionCorta());
        existente.setPrecioBase(seguro.getPrecioBase());
        existente.setDuracion(seguro.getDuracion());
        existente.setImageUrl(seguro.getImageUrl());

        return seguroRepository.save(existente);
    }

    /**
     * Eliminar seguro
     * 
     * @param id ID del seguro
     */
    @Transactional
    public void delete(String id) {
        logger.info("Eliminando seguro con ID: {}", id);

        if (!seguroRepository.existsById(id)) {
            logger.warn("Intento de eliminar seguro inexistente: {}", id);
            throw new RuntimeException("Seguro no encontrado con ID: " + id);
        }

        seguroRepository.deleteById(id);
    }

    /**
     * Contar total de seguros
     * 
     * @return Número total de seguros
     */
    @Transactional(readOnly = true)
    public long count() {
        return seguroRepository.count();
    }
}
