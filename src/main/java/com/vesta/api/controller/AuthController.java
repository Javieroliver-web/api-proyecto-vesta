package com.vesta.api.controller;

import com.vesta.api.dto.AuthResponseDTO;
import com.vesta.api.dto.LoginDTO;
import com.vesta.api.dto.RegistroDTO;
import com.vesta.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Para permitir peticiones desde el frontend
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            System.out.println("=== INICIO LOGIN ===");
            System.out.println("🔐 Login request recibido");
            System.out.println("📧 Email: " + loginDTO.getEmail());
            System.out.println("🔑 Password recibido: " + (loginDTO.getPassword() != null ? "SÍ" : "NO"));
            System.out.println("📦 DTO completo: " + loginDTO);
            
            AuthResponseDTO response = authService.login(loginDTO);
            
            System.out.println("✅ Login exitoso para: " + loginDTO.getEmail());
            System.out.println("=== FIN LOGIN ===");
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "error");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody RegistroDTO registroDTO) {
        try {
            System.out.println("📝 Registro request recibido para: " + registroDTO.getEmail());
            
            AuthResponseDTO response = authService.registrar(registroDTO);
            
            System.out.println("✅ Registro exitoso para: " + registroDTO.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error en registro: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "error");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Auth API funcionando correctamente");
        return ResponseEntity.ok(response);
    }
}