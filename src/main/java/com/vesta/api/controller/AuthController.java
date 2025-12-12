package com.vesta.api.controller;

import com.vesta.api.dto.ApiResponse;
import com.vesta.api.dto.AuthResponseDTO;
import com.vesta.api.dto.LoginDTO;
import com.vesta.api.dto.RegistroDTO;
import com.vesta.api.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación
 * Maneja login y registro de usuarios
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Endpoint de login
     * 
     * @param loginDTO Credenciales del usuario
     * @return Token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            logger.info("Login request recibido para email: {}", loginDTO.getEmail());

            AuthResponseDTO response = authService.login(loginDTO);

            logger.info("Login exitoso para: {}", loginDTO.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));

        } catch (RuntimeException e) {
            logger.error("Error en login para {}: {}", loginDTO.getEmail(), e.getMessage());
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
            logger.info("Registro request recibido para: {}", registroDTO.getEmail());

            AuthResponseDTO response = authService.registrar(registroDTO);

            logger.info("Registro exitoso para: {}", registroDTO.getEmail());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Usuario registrado exitosamente", response));

        } catch (RuntimeException e) {
            logger.error("Error en registro para {}: {}", registroDTO.getEmail(), e.getMessage());
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
