package com.vesta.api.controller;

import com.vesta.api.dto.CheckoutDTO;
import com.vesta.api.entity.Orden;
import com.vesta.api.service.OrdenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "*")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutDTO checkoutDTO) {
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
}