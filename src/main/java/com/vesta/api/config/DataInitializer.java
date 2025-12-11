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

        // 2. Crear Producto (Seguro Móvil)
        if (productoRepository.count() == 0) {
            Producto prod = new Producto();
            prod.setNombre("Seguro Móvil Premium");
            prod.setDescripcion("Cobertura total contra golpes y robos.");
            prod.setPrecioBase(new BigDecimal("12.50"));
            prod.setCategoria("Tecnología");
            productoRepository.save(prod);
            System.out.println("✅ Producto creado.");
        }

        // 3. Crear Póliza #1 (VINCULADA AL USUARIO)
        // IMPORTANTE: Esto asegura que exista una póliza con ID para el botón del frontend
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