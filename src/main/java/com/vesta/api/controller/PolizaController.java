package com.vesta.api.controller;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Producto;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.ProductoRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

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
            System.err.println("Error al obtener pólizas del usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Contratar un nuevo seguro
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

            // Crear la póliza
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

            // Guardar la póliza
            Poliza polizaGuardada = polizaRepository.save(poliza);

            System.out.println("✅ Póliza contratada: ID=" + polizaGuardada.getId() +
                    " Usuario=" + usuario.getEmail() +
                    " Producto=" + producto.getNombre());

            return ResponseEntity.ok(polizaGuardada);
        } catch (Exception e) {
            System.err.println("Error al contratar seguro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
