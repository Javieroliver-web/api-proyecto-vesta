package com.vesta.api.controller;

import com.vesta.api.service.TPVSimulatorService;
import com.vesta.api.service.TPVSimulatorService.PaymentRequest;
import com.vesta.api.service.TPVSimulatorService.PaymentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para el simulador de TPV Virtual
 */
@RestController
@RequestMapping("/api/tpv")

public class TPVController {

    @Autowired
    private TPVSimulatorService tpvService;

    /**
     * Procesar pago con tarjeta
     */
    @PostMapping("/procesar-pago")
    public ResponseEntity<?> procesarPago(@RequestBody PaymentRequest request) {
        try {
            PaymentResult result = tpvService.procesarPago(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "status", "ERROR",
                    "message", "Error interno del TPV: " + e.getMessage(),
                    "errorCode", "INTERNAL_ERROR"));
        }
    }

    /**
     * Obtener tarjetas de prueba disponibles
     */
    @GetMapping("/tarjetas-prueba")
    public ResponseEntity<Map<String, String>> getTarjetasPrueba() {
        return ResponseEntity.ok(tpvService.getTarjetasPrueba());
    }

    /**
     * Endpoint de salud del TPV
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "online",
                "service", "TPV Virtual Simulator",
                "version", "1.0.0",
                "message", "TPV Virtual funcionando correctamente"));
    }
}