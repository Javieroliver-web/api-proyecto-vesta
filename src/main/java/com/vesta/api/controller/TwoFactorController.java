package com.vesta.api.controller;

import com.vesta.api.dto.*;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.service.AuthService;
import com.vesta.api.service.TwoFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/2fa")

public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    // 1. Iniciar Setup (Generar secreto y QR)
    @GetMapping("/setup")
    public ResponseEntity<ApiResponse<TwoFactorSetupDTO>> setup2FA() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String secret = twoFactorService.generateNewSecret();
        // Guardar secreto temporalmente o permanentemente (pero disabled)
        // Guardamos directamente en usuario, pero enabled=false
        usuario.setTwoFactorSecret(secret);
        usuarioRepository.save(usuario);

        String qrUrl = twoFactorService.getQrCodeUrl(secret, email);

        return ResponseEntity
                .ok(ApiResponse.success("Configuración 2FA iniciada", new TwoFactorSetupDTO(secret, qrUrl)));
    }

    // 2. Verificar Setup (Activar 2FA)
    @PostMapping("/verify-setup")
    public ResponseEntity<ApiResponse<String>> verifySetup(@RequestBody TwoFactorRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No se ha iniciado la configuración 2FA"));
        }

        try {
            int code = Integer.parseInt(request.getCode());
            if (twoFactorService.validateCode(usuario.getTwoFactorSecret(), code)) {
                usuario.setTwoFactorEnabled(true);
                usuarioRepository.save(usuario);
                return ResponseEntity.ok(ApiResponse.success("2FA activado correctamente", "ENABLED"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Código inválido"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El código debe ser numérico"));
        }
    }

    // 3. Desactivar 2FA
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<String>> disable2FA() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(ApiResponse.success("2FA desactivado correctamente", "DISABLED"));
    }

    // 4. Validar Login (Paso 2 del Login)
    @PostMapping("/validate-login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> validateLogin(@RequestBody TwoFactorRequestDTO request) {
        // En este punto, el usuario tiene un token con rol PRE_VERIFICATION en el
        // SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Verificar que realmente estamos en estado PRE_VERIFICATION si queremos ser
        // estrictos,
        // pero la validación de código ya protege.

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            int code = Integer.parseInt(request.getCode());
            AuthResponseDTO response = authService.verifyTwoFactorLogin(usuario.getId(), code);
            return ResponseEntity.ok(ApiResponse.success("Login 2FA exitoso", response));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El código debe ser numérico"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    // 5. Estado actual
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> getStatus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(ApiResponse.success("Estado 2FA", Boolean.TRUE.equals(usuario.getTwoFactorEnabled())));
    }
}
