package com.vesta.api.controller;

import com.vesta.api.entity.Producto;
import com.vesta.api.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Listar todos los productos disponibles
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        try {
            List<Producto> productos = productoRepository.findAll();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.err.println("Error al listar productos: " + e.getMessage());
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
            System.err.println("Error al obtener producto: " + e.getMessage());
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
            System.err.println("Error al buscar productos: " + e.getMessage());
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
            System.err.println("Error al filtrar productos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
