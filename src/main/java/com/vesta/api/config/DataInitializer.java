package com.vesta.api.config;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Producto;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.ProductoRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private PolizaRepository polizaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🌱 INICIANDO CARGA DE DATOS DEMO...");

        // 1. Crear Usuario Demo (si no existe)
        Usuario usuario = usuarioRepository.findByEmail("demo@vesta.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setNombreCompleto("Usuario Demo");
                    u.setEmail("demo@vesta.com");
                    u.setPassword(passwordEncoder.encode("123456"));
                    u.setRol("USUARIO");
                    u.setMovil("+34600000000");
                    return usuarioRepository.save(u);
                });

        // 2. Crear Productos (si no existen)
        if (productoRepository.count() == 0) {
            // Seguro de Viaje
            Producto viaje = new Producto();
            viaje.setNombre("Seguro de Viaje");
            viaje.setDescripcion(
                    "Cobertura integral para tus viajes incluyendo asistencia médica, cancelación de vuelos, pérdida de equipaje y más. Actívalo solo cuando lo necesites.");
            viaje.setPrecioBase(new BigDecimal("15.99"));
            viaje.setCategoria("Viaje");
            viaje.setImagenUrl("https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=400&h=300&fit=crop");
            productoRepository.save(viaje);

            // Seguro de Dispositivos
            Producto dispositivos = new Producto();
            dispositivos.setNombre("Seguro de Dispositivos");
            dispositivos.setDescripcion(
                    "Protege tu smartphone y gadgets contra daños accidentales, robos y averías. Cobertura inmediata desde el momento de activación.");
            dispositivos.setPrecioBase(new BigDecimal("9.99"));
            dispositivos.setCategoria("Tecnología");
            dispositivos
                    .setImagenUrl("https://images.unsplash.com/photo-1592286927505-2fd0f8fc9c36?w=400&h=300&fit=crop");
            productoRepository.save(dispositivos);

            // Seguro de Eventos
            Producto eventos = new Producto();
            eventos.setNombre("Seguro de Eventos");
            eventos.setDescripcion(
                    "Asegura tu entrada a conciertos y eventos. Protección contra cancelaciones, pérdidas y accidentes durante el evento.");
            eventos.setPrecioBase(new BigDecimal("5.99"));
            eventos.setCategoria("Entretenimiento");
            eventos.setImagenUrl("https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=400&h=300&fit=crop");
            productoRepository.save(eventos);

            // Seguro de Bicicleta
            Producto bicicleta = new Producto();
            bicicleta.setNombre("Seguro de Bicicleta");
            bicicleta.setDescripcion(
                    "Protección para tu medio de transporte ecológico. Cobertura contra robos, daños y responsabilidad civil.");
            bicicleta.setPrecioBase(new BigDecimal("12.99"));
            bicicleta.setCategoria("Movilidad");
            bicicleta.setImagenUrl("https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400&h=300&fit=crop");
            productoRepository.save(bicicleta);

            // Seguro de Mascotas
            Producto mascotas = new Producto();
            mascotas.setNombre("Seguro de Mascotas");
            mascotas.setDescripcion(
                    "Cuidado veterinario para tu mejor amigo. Cobertura de gastos médicos, cirugías y tratamientos de emergencia.");
            mascotas.setPrecioBase(new BigDecimal("19.99"));
            mascotas.setCategoria("Mascotas");
            mascotas.setImagenUrl("https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&h=300&fit=crop");
            productoRepository.save(mascotas);

            // Seguro de Equipaje
            Producto equipaje = new Producto();
            equipaje.setNombre("Seguro de Equipaje");
            equipaje.setDescripcion(
                    "Protege tus pertenencias en tránsito. Cobertura contra pérdidas, daños y retrasos en la entrega de equipaje.");
            equipaje.setPrecioBase(new BigDecimal("7.99"));
            equipaje.setCategoria("Viaje");
            equipaje.setImagenUrl("https://images.unsplash.com/photo-1565026057447-bc90a3dceb87?w=400&h=300&fit=crop");
            productoRepository.save(equipaje);

            System.out.println("✅ 6 productos creados correctamente.");
        }

        // 3. Crear Póliza #1 (VINCULADA AL USUARIO DEMO)
        if (polizaRepository.count() == 0) {
            Producto prod = productoRepository.findAll().get(0);

            Poliza poliza = new Poliza();
            poliza.setUsuario(usuario);
            poliza.setProducto(prod);
            poliza.setFechaInicio(LocalDate.now().minusMonths(1));
            poliza.setFechaFin(LocalDate.now().plusMonths(11));
            poliza.setPrecioFinal(new BigDecimal("150.00"));
            poliza.setEstado("ACTIVA");

            polizaRepository.save(poliza);
            System.out.println("✅ Póliza DEMO creada correctamente.");
        }

        System.out.println("🌱 CARGA DE DATOS COMPLETADA.");
    }
}