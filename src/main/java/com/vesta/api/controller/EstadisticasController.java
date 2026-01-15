package com.vesta.api.controller;

import com.vesta.api.repository.OrdenRepository;
import com.vesta.api.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/estadisticas")
public class EstadisticasController {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private SiniestroRepository siniestroRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        List<Map<String, Object>> ventas = ordenRepository.obtenerVentasPorMes();
        List<Map<String, Object>> siniestros = siniestroRepository.obtenerSiniestrosPorCategoria();

        Map<String, Object> response = new HashMap<>();
        response.put("ventas", ventas);
        response.put("siniestros", siniestros);

        return ResponseEntity.ok(response);
    }
}
