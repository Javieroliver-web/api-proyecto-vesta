package com.vesta.api.controller;

import com.vesta.api.entity.Seguro;
import com.vesta.api.repository.SeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/seguros")
public class SeguroController {

    @Autowired(required = false) // Opcional por si no has creado el repo aún
    private SeguroRepository seguroRepository;

    @GetMapping
    public ResponseEntity<List<Seguro>> listarSeguros() {
        // MOCK DATA: Si no hay base de datos, devolvemos datos falsos para que el front funcione
        if (seguroRepository == null || seguroRepository.count() == 0) {
            List<Seguro> mocks = new ArrayList<>();
            Seguro s1 = new Seguro();
            s1.setId("ins-001");
            s1.setNombre("Seguro de Viaje");
            s1.setCategoria("Viaje");
            s1.setPrecioBase(new BigDecimal("15.99"));
            s1.setDescripcionCorta("Protección completa");
            s1.setDuracion("Día");
            s1.setImageUrl("https://images.unsplash.com/photo-1513258728326-30cde8cf1071");
            mocks.add(s1);
            return ResponseEntity.ok(mocks);
        }
        return ResponseEntity.ok(seguroRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Seguro> obtenerSeguro(@PathVariable String id) {
        // Mock simple
        Seguro s1 = new Seguro();
        s1.setId(id);
        s1.setNombre("Seguro Simulado");
        s1.setPrecioBase(new BigDecimal("10.00"));
        s1.setDescripcion("Descripción detallada simulada");
        return ResponseEntity.ok(s1);
    }
}