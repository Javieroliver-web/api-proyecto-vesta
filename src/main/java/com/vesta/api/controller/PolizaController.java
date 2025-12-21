package com.vesta.api.controller;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Producto;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.ProductoRepository;
import com.vesta.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/polizas")
@RequiredArgsConstructor
public class PolizaController {
    private static final Logger logger = LoggerFactory.getLogger(PolizaController.class);

    private final PolizaRepository polizaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    /**
     * Obtener TODAS las pólizas (solo para ADMIN)
     * Esto permite al administrador ver todas las ventas
     */
    @GetMapping
    public ResponseEntity<List<Poliza>> obtenerTodasLasPolizas() {
        try {
            List<Poliza> todasLasPolizas = polizaRepository.findAll();
            logger.info("📊 Admin consultando todas las pólizas: {} encontradas", todasLasPolizas.size());
            return ResponseEntity.ok(todasLasPolizas);
        } catch (Exception e) {
            logger.error("Error al obtener todas las pólizas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todas las pólizas del usuario autenticado
     */
    @GetMapping("/usuario")
    public ResponseEntity<List<Poliza>> obtenerPolizasUsuario(Authentication authentication) {
        try {
            // Obtener el email del usuario autenticado desde el token JWT
            String email = authentication.getName();

            // Buscar el usuario
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener las pólizas del usuario
            List<Poliza> polizas = polizaRepository.findByUsuarioId(usuario.getId());

            return ResponseEntity.ok(polizas);
        } catch (Exception e) {
            logger.error("Error al obtener pólizas del usuario", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Contratar un nuevo seguro o extender uno existente
     */
    @PostMapping("/contratar")
    public ResponseEntity<Poliza> contratarSeguro(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            // Obtener datos del request
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer duracion = request.containsKey("duracion")
                    ? Integer.valueOf(request.get("duracion").toString())
                    : 365; // Por defecto 1 año

            // Obtener el usuario autenticado
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Obtener el producto
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Buscar si ya existe una póliza ACTIVA para este usuario y producto
            List<Poliza> polizasExistentes = polizaRepository.findByUsuarioId(usuario.getId());
            Poliza polizaExistente = polizasExistentes.stream()
                    .filter(p -> p.getProducto().getId().equals(productoId))
                    .filter(p -> "ACTIVA".equals(p.getEstado()))
                    .findFirst()
                    .orElse(null);

            Poliza polizaFinal;

            if (polizaExistente != null) {
                // EXTENDER PÓLIZA EXISTENTE
                logger.info("📝 Extendiendo póliza existente ID={}", polizaExistente.getId());

                // Sumar los días a la fecha de fin actual
                LocalDate nuevaFechaFin = polizaExistente.getFechaFin().plusDays(duracion);
                polizaExistente.setFechaFin(nuevaFechaFin);

                // Calcular el precio adicional
                BigDecimal precioAdicional = producto.getPrecioBase()
                        .multiply(BigDecimal.valueOf(duracion))
                        .divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP);

                // Sumar al precio final existente
                BigDecimal nuevoPrecioFinal = polizaExistente.getPrecioFinal().add(precioAdicional);
                polizaExistente.setPrecioFinal(nuevoPrecioFinal);

                polizaFinal = polizaRepository.save(polizaExistente);

                logger.info("✅ Póliza extendida: ID={}, Nueva fecha fin={}, Días añadidos={}",
                        polizaFinal.getId(), nuevaFechaFin, duracion);
            } else {
                // CREAR NUEVA PÓLIZA
                logger.info("📝 Creando nueva póliza");

                Poliza poliza = new Poliza();
                poliza.setUsuario(usuario);
                poliza.setProducto(producto);
                poliza.setFechaInicio(LocalDate.now());
                poliza.setFechaFin(LocalDate.now().plusDays(duracion));

                // Calcular precio: precioBase * (duracion / 30) para mensualizar
                BigDecimal precioTotal = producto.getPrecioBase()
                        .multiply(BigDecimal.valueOf(duracion))
                        .divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP);
                poliza.setPrecioFinal(precioTotal);
                poliza.setEstado("ACTIVA");

                polizaFinal = polizaRepository.save(poliza);

                logger.info("✅ Póliza creada: ID={}, Usuario={}, Producto={}",
                        polizaFinal.getId(), usuario.getEmail(), producto.getNombre());
            }

            return ResponseEntity.ok(polizaFinal);
        } catch (Exception e) {
            logger.error("Error al contratar seguro", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
