package com.vesta.api.controller;

import com.vesta.api.dto.*;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.service.AuthService;
import com.vesta.api.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de autenticación
 * Maneja login, registro y recuperación de contraseña
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Endpoint de login
     * 
     * @param loginDTO Credenciales del usuario
     * @return Token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            logger.info("Login request recibido para email: {}", loginDTO.getCorreoElectronico());

            AuthResponseDTO response = authService.login(loginDTO);

            logger.info("Login exitoso para: {}", loginDTO.getCorreoElectronico());
            return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));

        } catch (RuntimeException e) {
            logger.error("Error en login para {}: {}", loginDTO.getCorreoElectronico(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Endpoint de registro
     * 
     * @param registroDTO Datos del nuevo usuario
     * @return Token JWT y datos del usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> registrar(@Valid @RequestBody RegistroDTO registroDTO) {
        try {
            logger.info("Registro request recibido para: {}", registroDTO.getCorreoElectronico());

            AuthResponseDTO response = authService.registrar(registroDTO);

            logger.info("Registro exitoso para: {}", registroDTO.getCorreoElectronico());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Usuario registrado exitosamente", response));

        } catch (RuntimeException e) {
            logger.error("Error en registro para {}: {}", registroDTO.getCorreoElectronico(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Endpoint para solicitar recuperación de contraseña
     * Genera un código de 6 dígitos y lo envía por email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        try {
            logger.info("Solicitud de recuperación de contraseña para: {}", dto.getEmail());

            // Generar y enviar token por el método especificado
            passwordResetService.generateResetToken(dto.getEmail(), dto.getMethod());

            String message = "sms".equalsIgnoreCase(dto.getMethod())
                    ? "Se ha enviado un código de verificación a tu número de móvil"
                    : "Se ha enviado un código de verificación a tu correo electrónico";

            return ResponseEntity.ok(
                    ApiResponse.success(message, "CODE_SENT"));
        } catch (RuntimeException e) {
            logger.error("Error en forgot-password para {}: {}", dto.getEmail(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Endpoint para verificar qué métodos de recuperación están disponibles para un
     * usuario
     */
    @PostMapping("/check-recovery-methods")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRecoveryMethods(
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            logger.info("Verificando métodos de recuperación para: {}", email);

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

            Map<String, Object> methods = new HashMap<>();
            methods.put("email", true); // Email siempre disponible
            methods.put("sms", usuario.getMovil() != null && !usuario.getMovil().isEmpty());
            methods.put("userName", usuario.getNombreCompleto());
            methods.put("userEmail", usuario.getEmail());

            return ResponseEntity.ok(ApiResponse.success("Métodos disponibles", methods));
        } catch (RuntimeException e) {
            logger.error("Error verificando métodos: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Endpoint para validar un token de recuperación
     */
    @PostMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestBody String token) {
        try {
            boolean isValid = passwordResetService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success("Token válido", true));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Token inválido o expirado"));
            }
        } catch (Exception e) {
            logger.error("Error validando token: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al validar el token"));
        }
    }

    /**
     * Endpoint para resetear la contraseña con un token válido
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        try {
            logger.info("Solicitud de reset de contraseña con token");

            passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());

            return ResponseEntity.ok(
                    ApiResponse.success("Contraseña actualizada exitosamente", "PASSWORD_RESET"));
        } catch (RuntimeException e) {
            logger.error("Error en reset-password: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Endpoint de prueba para verificar que la API funciona
     * 
     * @return Mensaje de estado
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        logger.debug("Test endpoint called");
        return ResponseEntity.ok(
                ApiResponse.success("Auth API funcionando correctamente", "OK"));
    }
}
