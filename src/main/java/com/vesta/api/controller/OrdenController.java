package com.vesta.api.controller;

import com.vesta.api.dto.CheckoutDTO;
import com.vesta.api.entity.Orden;
import com.vesta.api.repository.OrdenRepository;
import com.vesta.api.service.OrdenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List; // <--- IMPORTANTE
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrdenController {
    private static final Logger logger = LoggerFactory.getLogger(OrdenController.class);

    private final OrdenService ordenService;
    private final OrdenRepository ordenRepository;

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
            logger.error("Error procesando checkout", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- NUEVO ENDPOINT PARA ADMIN ---
    @GetMapping
    public ResponseEntity<List<Orden>> listarOrdenes() {
        return ResponseEntity.ok(ordenRepository.findAll());
    }
}