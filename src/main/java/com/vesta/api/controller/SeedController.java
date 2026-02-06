package com.vesta.api.controller;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Producto;
import com.vesta.api.entity.Siniestro;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.ProductoRepository;
import com.vesta.api.repository.SiniestroRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/debug")
public class SeedController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private SiniestroRepository siniestroRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/seed")
    public ResponseEntity<?> seedData() {
        try {
            // 1. Crear Productos si no existen
            if (productoRepository.count() == 0) {
                crearProductos();
            }

            // 2. Crear Usuarios si hay pocos (menos de 5 aparte del admin)
            if (usuarioRepository.count() < 5) {
                crearUsuarios();
            }

            // 3. Crear Pólizas y Siniestros
            crearPolizasYSiniestros();

            return ResponseEntity.ok(Map.of("message", "Datos de prueba generados correctamente."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private void crearProductos() {
        // ... (Implementación simplificada para el ejemplo)
        // Asumiendo que ya podrían existir o no ser críticos para probar Siniestros si
        // ya hay alguno
        // Pero crearemos uno básico por si acaso
        Producto p = new Producto();
        p.setNombre("Seguro Hogar Premium");
        p.setDescripcion("Cobertura total para tu vivienda");
        p.setPrecioBase(new BigDecimal("250.00"));
        p.setCategoria("HOGAR");
        p.setImagenUrl("assets/images/products/house.png");
        p.setActivo(true);
        productoRepository.save(p);
    }

    private void crearUsuarios() {
        String[] nombres = { "Juan Pérez", "Maria Garcia", "Carlos Lopez", "Ana Martinez", "Luis Rodriguez" };

        for (int i = 0; i < nombres.length; i++) {
            String email = "test" + (i + 1) + "@vesta.com";
            if (usuarioRepository.findByEmail(email).isEmpty()) {
                Usuario u = new Usuario();
                u.setNombreCompleto(nombres[i]);
                u.setEmail(email);
                u.setPassword(passwordEncoder.encode("password123"));
                u.setRol("USER");
                u.setActivo(true);
                u.setEmailConfirmado(true);
                u.setAceptaTerminos(true);
                u.setAceptaPrivacidad(true);
                u.setCiudad("Madrid, ES");
                u.setPais("España");
                usuarioRepository.save(u);
            }
        }
    }

    private void crearPolizasYSiniestros() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Producto> productos = productoRepository.findAll();

        if (productos.isEmpty()) {
            crearProductos();
            productos = productoRepository.findAll();
        }

        Random rand = new Random();
        Producto producto = productos.get(0);

        for (Usuario u : usuarios) {
            if ("ADMINISTRADOR".equals(u.getRol()) || "OWNER".equals(u.getRol()))
                continue;

            // Verificar si ya tiene póliza
            List<Poliza> misPolizas = polizaRepository.findByUsuario(u);
            Poliza poliza;

            if (misPolizas.isEmpty()) {
                poliza = new Poliza();
                poliza.setUsuario(u);
                poliza.setProducto(producto);
                poliza.setFechaInicio(LocalDate.now().minusMonths(rand.nextInt(12)));
                poliza.setFechaFin(poliza.getFechaInicio().plusYears(1));
                poliza.setPrecioFinal(producto.getPrecioBase());
                poliza.setEstado("ACTIVA");
                poliza = polizaRepository.save(poliza);
            } else {
                poliza = misPolizas.get(0);
            }

            // Crear siniestros si no tiene
            if (siniestroRepository.count() < 10) { // Limite global simple
                Siniestro s = new Siniestro();
                s.setPoliza(poliza);
                s.setFecha(LocalDate.now().minusDays(rand.nextInt(30)));
                s.setDescripcion("Daño accidental en la cocina - Prueba " + rand.nextInt(100));

                String[] estados = { "PENDIENTE_REVISION", "APROBADO", "RECHAZADO" };
                s.setEstado(estados[rand.nextInt(estados.length)]);

                s.setImagenUrl("uploads/test.jpg");
                s.setAnalisisIA("Análisis simulado: Daños detectados.");
                s.setFraudeScore(rand.nextInt(20));

                siniestroRepository.save(s);
            }
        }
    }
}
