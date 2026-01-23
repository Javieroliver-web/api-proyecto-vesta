package com.vesta.api.controller;

import com.vesta.api.dto.CheckoutDTO;
import com.vesta.api.dto.CheckoutTPVDTO;
import com.vesta.api.entity.Orden;
import com.vesta.api.repository.OrdenRepository; // <--- IMPORTANTE
import com.vesta.api.service.OrdenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List; // <--- IMPORTANTE
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "*")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private OrdenRepository ordenRepository; // <--- AÑADIR ESTO

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDTO checkoutDTO) {
        // ... (Mantén el código existente del checkout tal cual está) ...
        try {
            Orden orden = ordenService.procesarCompra(checkoutDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Compra realizada con éxito");
            response.put("referencia", orden.getReferencia());
            response.put("total", orden.getTotal());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/checkout-tpv")
    public ResponseEntity<?> checkoutConTPV(@RequestBody CheckoutTPVDTO checkoutDTO) {
        try {
            Orden orden = ordenService.procesarCompraConTPV(checkoutDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Compra procesada");
            response.put("referencia", orden.getReferencia());
            response.put("total", orden.getTotal());
            response.put("estado", orden.getEstado());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- NUEVO ENDPOINT PARA ADMIN ---
    @GetMapping
    public ResponseEntity<List<Orden>> listarOrdenes() {
        return ResponseEntity.ok(ordenRepository.findAll());
    }
    
    // --- ENDPOINT PARA ÓRDENES PENDIENTES DE USUARIO ---
    @GetMapping("/usuario/{usuarioId}/pendientes")
    public ResponseEntity<List<Orden>> obtenerOrdenesPendientesUsuario(@PathVariable Long usuarioId) {
        try {
            List<Orden> ordenesPendientes = ordenRepository.findByUsuarioIdAndEstado(usuarioId, "PENDIENTE");
            return ResponseEntity.ok(ordenesPendientes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    // Endpoint para actualizar estado de orden (solo admin)
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoOrden(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
                
            String nuevoEstado = request.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado requerido"));
            }
            
            // Validar estados permitidos
            List<String> estadosPermitidos = List.of("PENDIENTE", "PROCESANDO", "COMPLETADA", "FALLIDA", "CANCELADA");
            if (!estadosPermitidos.contains(nuevoEstado.toUpperCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado no válido"));
            }
            
            orden.setEstado(nuevoEstado.toUpperCase());
            ordenRepository.save(orden);
            
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado correctamente"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al actualizar estado: " + e.getMessage()));
        }
    }
}