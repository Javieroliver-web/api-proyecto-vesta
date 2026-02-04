package com.vesta.api.controller;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Importante
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map; // Importante

import org.springframework.security.core.Authentication; // Importante
import org.springframework.security.core.context.SecurityContextHolder; // Importante

import com.vesta.api.entity.Poliza;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.SiniestroRepository;
import com.vesta.api.repository.PasswordResetTokenRepository;
import com.vesta.api.service.AuditoriaService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private com.vesta.api.repository.PolizaRepository polizaRepository;

    @Autowired
    private com.vesta.api.repository.SiniestroRepository siniestroRepository;

    @Autowired
    private com.vesta.api.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private jakarta.servlet.http.HttpServletRequest request;

    /**
     * Helper para obtener la IP real del cliente
     */
    private String getClientIp() {
        if (request == null)
            return "";
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
        }
        if (remoteAddr != null && !remoteAddr.isEmpty() && !"unknown".equalsIgnoreCase(remoteAddr)) {
            return remoteAddr.split(",")[0].trim();
        }
        remoteAddr = request.getHeader("X-Real-IP");
        if (remoteAddr != null && !remoteAddr.isEmpty() && !"unknown".equalsIgnoreCase(remoteAddr)) {
            return remoteAddr;
        }
        return request.getRemoteAddr();
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // VALIDACIÓN DE SEGURIDAD (OWNER)
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    boolean isOwner = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

                    // Protección: Solo OWNER puede modificar a otro OWNER (y a sí mismo)
                    if ("OWNER".equals(usuario.getRol()) && !isOwner) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message", "No tienes permisos para modificar al propietario."));
                    }

                    // 1. Validar Cambio de Contraseña
                    if (updates.containsKey("newPassword")) {
                        String currentPassword = (String) updates.get("currentPassword");
                        String newPassword = (String) updates.get("newPassword");

                        if (currentPassword == null || currentPassword.isEmpty()) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of("message", "Debes ingresar tu contraseña actual."));
                        }

                        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of("message", "La contraseña actual es incorrecta."));
                        }

                        usuario.setPassword(passwordEncoder.encode(newPassword));
                    }

                    // 2. Actualizar otros campos
                    if (updates.containsKey("nombreCompleto")) {
                        String nombre = (String) updates.get("nombreCompleto");
                        if (nombre != null && nombre.length() > 50) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of("message", "El nombre completo no puede superar los 50 caracteres."));
                        }
                        usuario.setNombreCompleto(nombre);
                    }
                    if (updates.containsKey("movil")) {
                        String movil = (String) updates.get("movil");
                        if (movil != null && movil.length() > 15) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of("message", "El teléfono móvil no puede superar los 15 caracteres."));
                        }
                        usuario.setMovil(movil);
                    }

                    // SEGURIDAD: Cambio de ROL
                    if (updates.containsKey("rol")) {
                        // Solo OWNER puede cambiar roles
                        if (!isOwner) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body(Map.of("message", "Solo el propietario puede cambiar roles."));
                        }

                        String nuevoRol = (String) updates.get("rol");
                        // Prevenir que se quite el rol OWNER a sí mismo por error (opcional, pero buena
                        // práctica)
                        if ("OWNER".equals(usuario.getRol()) && !"OWNER".equals(nuevoRol)) {
                            long ownerCount = usuarioRepository.countByRol("OWNER");
                            if (ownerCount <= 1) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("message", "No puedes eliminar el último Owner del sistema."));
                            }
                        }

                        usuario.setRol(nuevoRol);
                    }

                    if (updates.containsKey("ciudad")) {
                        String nuevaCiudad = (String) updates.get("ciudad");
                        try {
                            if (!recommendationService.validarCiudad(nuevaCiudad)) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("message", "No se pudo obtener información climática para "
                                                + nuevaCiudad + ". Intenta con otra búsqueda."));
                            }
                        } catch (Exception e) {
                            // Ignorar error de validación clima si falla servicio externo, permitir update
                        }
                        usuario.setCiudad(nuevaCiudad);
                    }
                    if (updates.containsKey("tema")) {
                        usuario.setTema((String) updates.get("tema"));
                    }
                    if (updates.containsKey("email")) {
                        String email = (String) updates.get("email");
                        if (email != null && email.length() > 100) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of("message",
                                            "El correo electrónico no puede superar los 100 caracteres."));
                        }
                        usuario.setEmail(email);
                    }
                    if (updates.containsKey("activo")) {
                        // Admin puede bloquear User, pero validamos arriba protección de Owner
                        usuario.setActivo((Boolean) updates.get("activo"));
                    }

                    Usuario actualizado = usuarioRepository.save(usuario);

                    // Log Auditoría
                    String accionLog = updates.containsKey("rol") ? "UPDATE_ROLE"
                            : (updates.containsKey("activo") ? "UPDATE_STATUS" : "UPDATE_PROFILE");
                    String detalleLog = "Usuario actualizado: " + usuario.getEmail();
                    if (updates.containsKey("rol"))
                        detalleLog += " (Nuevo rol: " + usuario.getRol() + ")";
                    if (updates.containsKey("activo"))
                        detalleLog += " (Activo: " + usuario.getActivo() + ")";

                    auditoriaService.registrarAccion(auth.getName(), accionLog, detalleLog, getClientIp());

                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/link-oauth")
    public ResponseEntity<?> linkOAuth(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String provider = payload.get("provider");
        String googleEmail = payload.get("googleEmail");
        String providerId = payload.get("providerId");

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // Vincular el proveedor y su ID único
                    usuario.setProvider(provider);
                    if (providerId != null) {
                        usuario.setProviderId(providerId);
                    }
                    usuarioRepository.save(usuario);

                    // Log Auditoría
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    auditoriaService.registrarAccion(auth.getName(), "LINK_OAUTH",
                            "Vinculación con provider: " + provider + " (" + googleEmail + ") a su cuenta: "
                                    + usuario.getEmail(),
                            getClientIp());

                    return ResponseEntity
                            .ok(Map.of("message", "Cuenta de Google vinculada correctamente a tu usuario actual."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/unlink-oauth")
    public ResponseEntity<?> unlinkOAuth(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // Verificar que el usuario tenga una contraseña configurada
                    if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("message",
                                        "No puedes desvincular Google sin configurar una contraseña primero."));
                    }

                    // Desvincular OAuth
                    usuario.setProvider(null);
                    usuarioRepository.save(usuario);

                    // Log Auditoría
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    auditoriaService.registrarAccion(auth.getName(), "UNLINK_OAUTH",
                            "Usuario desvinculó cuenta OAuth: " + usuario.getEmail(), getClientIp());

                    return ResponseEntity.ok(Map.of("message", "Cuenta de Google desvinculada correctamente."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

            // Protección: Solo OWNER puede eliminar a otro OWNER
            if ("OWNER".equals(usuario.getRol()) && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "No tienes permisos para eliminar al propietario."));
            }

            // Protección: No eliminar al último administrador o owner
            if ("ADMIN".equals(usuario.getRol()) || "OWNER".equals(usuario.getRol())) {
                long adminCount = usuarioRepository.countByRol("ADMIN") + usuarioRepository.countByRol("OWNER");
                if (adminCount <= 1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "No se puede eliminar al último administrador/owner."));
                }
            }

            // Protección: ADMIN no puede eliminar a otro ADMIN
            boolean esRequesterAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (esRequesterAdmin && "ADMIN".equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Los administradores no pueden eliminar a otros administradores."));
            }

            // === CASCADING DELETE: Eliminar entidades relacionadas ===
            try {
                // 1. Eliminar tokens de reseteo de contraseña
                passwordResetTokenRepository.deleteByUsuario(usuario);

                // 2. Obtener todas las pólizas del usuario
                List<Poliza> polizas = polizaRepository.findByUsuario(usuario);

                // 3. Para cada póliza, eliminar sus siniestros
                for (Poliza poliza : polizas) {
                    siniestroRepository.deleteByPoliza(poliza);
                }

                // 4. Eliminar las pólizas del usuario
                polizaRepository.deleteAll(polizas);

                // 5. Finalmente, eliminar el usuario
                usuarioRepository.delete(usuario);

                // Log Auditoría
                auditoriaService.registrarAccion(auth.getName(), "DELETE_USER",
                        "Usuario eliminado: " + usuario.getEmail(), getClientIp());

                return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Error al eliminar usuario: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}