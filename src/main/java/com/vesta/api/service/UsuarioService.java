package com.vesta.api.service;

import com.vesta.api.entity.Poliza;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.PasswordResetTokenRepository;
import com.vesta.api.repository.PolizaRepository;
import com.vesta.api.repository.SiniestroRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private SiniestroRepository siniestroRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Page<Usuario> findAllFiltered(Pageable pageable, String keyword, String role, String status) {
        // Normalizar strings vacíos a null para el Query
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String r = (role != null && !role.trim().isEmpty() && !"ALL".equals(role)) ? role : null;
        String s = (status != null && !status.trim().isEmpty() && !"ALL".equals(status)) ? status : null;

        return usuarioRepository.findAllFiltered(k, r, s, pageable);
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Usuario updateUser(Long id, Map<String, Object> updates, String clientIp) {
        return usuarioRepository.findById(id).map(usuario -> {
            // VALIDACIÓN DE SEGURIDAD (OWNER)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwner = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

            // Protección: Solo OWNER puede modificar a otro OWNER (y a sí mismo)
            if ("OWNER".equals(usuario.getRol()) && !isOwner) {
                throw new SecurityException("No tienes permisos para modificar al propietario.");
            }

            // 1. Validar Cambio de Contraseña
            if (updates.containsKey("newPassword")) {
                String currentPassword = (String) updates.get("currentPassword");
                String newPassword = (String) updates.get("newPassword");

                if (currentPassword == null || currentPassword.isEmpty()) {
                    throw new IllegalArgumentException("Debes ingresar tu contraseña actual.");
                }

                if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
                    throw new IllegalArgumentException("La contraseña actual es incorrecta.");
                }

                usuario.setPassword(passwordEncoder.encode(newPassword));
            }

            // 2. Actualizar otros campos
            if (updates.containsKey("nombreCompleto")) {
                String nombre = (String) updates.get("nombreCompleto");
                if (nombre != null && nombre.length() > 50) {
                    throw new IllegalArgumentException("El nombre completo no puede superar los 50 caracteres.");
                }
                usuario.setNombreCompleto(nombre);
            }
            if (updates.containsKey("movil")) {
                String movil = (String) updates.get("movil");
                if (movil != null && movil.length() > 15) {
                    throw new IllegalArgumentException("El teléfono móvil no puede superar los 15 caracteres.");
                }
                usuario.setMovil(movil);
            }

            // SEGURIDAD: Cambio de ROL
            if (updates.containsKey("rol")) {
                // Solo OWNER puede cambiar roles
                if (!isOwner) {
                    throw new SecurityException("Solo el propietario puede cambiar roles.");
                }

                String nuevoRol = (String) updates.get("rol");
                // Prevenir que se quite el rol OWNER a sí mismo por error
                if ("OWNER".equals(usuario.getRol()) && !"OWNER".equals(nuevoRol)) {
                    long ownerCount = usuarioRepository.countByRol("OWNER");
                    if (ownerCount <= 1) {
                        throw new IllegalArgumentException("No puedes eliminar el último Owner del sistema.");
                    }
                }

                usuario.setRol(nuevoRol);
            }

            if (updates.containsKey("ciudad")) {
                String nuevaCiudad = (String) updates.get("ciudad");
                try {
                    if (!recommendationService.validarCiudad(nuevaCiudad)) {
                        throw new IllegalArgumentException("No se pudo obtener información climática para "
                                + nuevaCiudad + ". Intenta con otra búsqueda.");
                    }
                } catch (Exception e) {
                    // Ignorar error de validación clima si servicio falla
                }
                usuario.setCiudad(nuevaCiudad);
            }
            if (updates.containsKey("tema")) {
                usuario.setTema((String) updates.get("tema"));
            }
            if (updates.containsKey("email")) {
                String email = (String) updates.get("email");
                if (email != null && email.length() > 100) {
                    throw new IllegalArgumentException("El correo electrónico no puede superar los 100 caracteres.");
                }
                usuario.setEmail(email);
            }
            if (updates.containsKey("activo")) {
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

            auditoriaService.registrarAccion(auth.getName(), accionLog, detalleLog, clientIp);

            return actualizado;
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    public void linkOAuth(Long id, Map<String, String> payload, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String provider = payload.get("provider");
        String googleEmail = payload.get("googleEmail");
        String providerId = payload.get("providerId");

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
                clientIp);
    }

    @Transactional
    public void unlinkOAuth(Long id, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar que el usuario tenga una contraseña configurada
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            throw new IllegalArgumentException("No puedes desvincular Google sin configurar una contraseña primero.");
        }

        // Desvincular OAuth
        usuario.setProvider(null);
        usuarioRepository.save(usuario);

        // Log Auditoría
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditoriaService.registrarAccion(auth.getName(), "UNLINK_OAUTH",
                "Usuario desvinculó cuenta OAuth: " + usuario.getEmail(), clientIp);
    }

    @Transactional
    public void deleteUser(Long id, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        // Protección: Solo OWNER puede eliminar a otro OWNER
        if ("OWNER".equals(usuario.getRol()) && !isOwner) {
            throw new SecurityException("No tienes permisos para eliminar al propietario.");
        }

        // Protección: No eliminar al último administrador o owner
        if ("ADMIN".equals(usuario.getRol()) || "OWNER".equals(usuario.getRol())) {
            long adminCount = usuarioRepository.countByRol("ADMIN") + usuarioRepository.countByRol("OWNER");
            if (adminCount <= 1) {
                throw new IllegalArgumentException("No se puede eliminar al último administrador/owner.");
            }
        }

        // Protección: ADMIN no puede eliminar a otro ADMIN
        boolean esRequesterAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (esRequesterAdmin && "ADMIN".equals(usuario.getRol())) {
            throw new SecurityException("Los administradores no pueden eliminar a otros administradores.");
        }

        // === CASCADING DELETE: Eliminar entidades relacionadas ===
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
                "Usuario eliminado: " + usuario.getEmail(), clientIp);
    }
}
