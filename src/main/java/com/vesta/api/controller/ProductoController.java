package com.vesta.api.controller;

import com.vesta.api.entity.Producto;
import com.vesta.api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoController {
    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoRepository productoRepository;

    /**
     * Listar todos los productos disponibles
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        try {
            List<Producto> productos = productoRepository.findAll();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            logger.error("Error al listar productos", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener detalles de un producto específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
        try {
            Producto producto = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            return ResponseEntity.ok(producto);
        } catch (Exception e) {
            logger.error("Error al obtener producto con id: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Buscar productos por nombre o descripción
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarProductos(@RequestParam String q) {
        try {
            List<Producto> productos = productoRepository
                    .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(q, q);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            logger.error("Error al buscar productos con query: {}", q, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Filtrar productos por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> filtrarPorCategoria(@PathVariable String categoria) {
        try {
            List<Producto> productos = productoRepository.findByCategoria(categoria);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            logger.error("Error al filtrar productos por categoría: {}", categoria, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
