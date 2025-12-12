package com.vesta.api.controller;

import com.vesta.api.dto.ApiResponse;
import com.vesta.api.entity.Seguro;
import com.vesta.api.service.SeguroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de seguros
 */
@RestController
@RequestMapping("/api/seguros")
public class SeguroController {

    private static final Logger logger = LoggerFactory.getLogger(SeguroController.class);

    @Autowired
    private SeguroService seguroService;

    /**
     * Listar todos los seguros con paginación opcional
     * 
     * @param page Número de página (opcional, default 0)
     * @param size Tamaño de página (opcional, default 10)
     * @param sort Campo para ordenar (opcional, default "nombre")
     * @return Lista de seguros o página de seguros
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> listarSeguros(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "nombre") String sort) {

        try {
            if (page != null && size != null) {
                // Con paginación
                logger.debug("Listando seguros con paginación: page={}, size={}", page, size);
                Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
                Page<Seguro> segurosPage = seguroService.findAll(pageable);
                return ResponseEntity.ok(ApiResponse.success("Seguros obtenidos exitosamente", segurosPage));
            } else {
                // Sin paginación
                logger.debug("Listando todos los seguros");
                List<Seguro> seguros = seguroService.findAll();
                return ResponseEntity.ok(ApiResponse.success("Seguros obtenidos exitosamente", seguros));
            }
        } catch (Exception e) {
            logger.error("Error al listar seguros: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener seguros"));
        }
    }

    /**
     * Obtener seguro por ID
     * 
     * @param id ID del seguro
     * @return Seguro encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seguro>> obtenerSeguro(@PathVariable String id) {
        try {
            logger.debug("Obteniendo seguro con ID: {}", id);
            Seguro seguro = seguroService.findByIdOrThrow(id);
            return ResponseEntity.ok(ApiResponse.success("Seguro encontrado", seguro));
        } catch (RuntimeException e) {
            logger.warn("Seguro no encontrado: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al obtener seguro {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener seguro"));
        }
    }

    /**
     * Buscar seguros por categoría
     * 
     * @param categoria Categoría del seguro
     * @return Lista de seguros de la categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<Seguro>>> buscarPorCategoria(@PathVariable String categoria) {
        try {
            logger.debug("Buscando seguros de categoría: {}", categoria);
            List<Seguro> seguros = seguroService.findByCategoria(categoria);
            return ResponseEntity.ok(ApiResponse.success("Seguros encontrados", seguros));
        } catch (Exception e) {
            logger.error("Error al buscar seguros por categoría {}: {}", categoria, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al buscar seguros"));
        }
    }
}
