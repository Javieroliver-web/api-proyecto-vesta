package com.vesta.api.controller;

import com.vesta.api.entity.Producto;
import com.vesta.api.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")

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

    /**
     * Crear un nuevo producto (Admin)
     */
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        try {
            Producto nuevo = productoRepository.save(producto);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar un producto existente (Admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoDetails) {
        try {
            Producto producto = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            producto.setNombre(productoDetails.getNombre());
            producto.setDescripcion(productoDetails.getDescripcion());
            producto.setPrecioBase(productoDetails.getPrecioBase());
            producto.setCategoria(productoDetails.getCategoria());
            producto.setImagenUrl(productoDetails.getImagenUrl());
            producto.setActivo(productoDetails.getActivo());

            Producto actualizado = productoRepository.save(producto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar un producto (Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        try {
            productoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
