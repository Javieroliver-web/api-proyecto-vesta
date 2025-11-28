package com.vesta.api.service;

import com.vesta.api.dto.LoginRequest;
import com.vesta.api.dto.LoginResponse;
import com.vesta.api.entity.Usuario; // Asegúrate de que coincida con tu paquete de entidad
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.util.JWTUtil;
import com.vesta.api.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtil jwtUtil;

    /**
     * Autentica al usuario y devuelve un Token JWT si es correcto.
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o credenciales inválidas."));

        // 2. Verificar contraseña usando encriptación segura
        // Nota: Pasamos "" como salt porque PasswordUtil ya lo extrae del formato "salt:hash" guardado en BD.
        boolean passwordMatch = PasswordUtil.verifyUserPassword(
                request.getPassword(),
                usuario.getPassword(),
                "" 
        );

        if (!passwordMatch) {
            throw new RuntimeException("Credenciales inválidas.");
        }

        // 3. Generar Token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());

        // 4. Devolver respuesta limpia para el Frontend
        return new LoginResponse(token, usuario.getRol(), usuario.getNombreCompleto());
    }

    /**
     * MÉTODO ÚTIL PARA TI (ADMIN):
     * Úsalo para generar contraseñas encriptada si quieres insertarlas manualmente en SQL.
     */
    public String encriptarPassword(String passwordPlana) {
        String salt = PasswordUtil.getSalt(30);
        String hash = PasswordUtil.generateSecurePassword(passwordPlana, salt);
        return salt + ":" + hash; // Formato que guardamos en BD
    }
}