package com.vesta.api.service;

import com.vesta.api.dto.AuthResponseDTO;
import com.vesta.api.dto.LoginDTO;
import com.vesta.api.dto.RegistroDTO;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación
 * Maneja login y registro de usuarios
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Autenticar usuario
     * 
     * @param request Credenciales de login
     * @return Respuesta con token y datos del usuario
     */
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginDTO request) {
        String identifier = request.getCorreoElectronico();
        logger.debug("Intentando login para: {}", identifier);

        Usuario usuario;

        // Determinar si es Email o Teléfono
        if (identifier.contains("@")) {
            usuario = usuarioRepository.findByEmailIgnoreCase(identifier)
                    .orElseThrow(() -> {
                        logger.warn("Usuario no encontrado por email: {}", identifier);
                        return new RuntimeException("Usuario no encontrado");
                    });
        } else {
            // Asumimos que es un teléfono
            usuario = usuarioRepository.findByMovil(identifier)
                    .orElseThrow(() -> {
                        logger.warn("Usuario no encontrado por móvil: {}", identifier);
                        return new RuntimeException("Usuario no encontrado");
                    });
        }

        if (usuario.getBloqueoHasta() != null && usuario.getBloqueoHasta().isAfter(java.time.LocalDateTime.now())) {
            logger.warn("Intento de login en cuenta bloqueada: {}", request.getCorreoElectronico());
            throw new RuntimeException(
                    "Cuenta bloqueada temporalmente por seguridad. Inténtalo de nuevo en unos minutos.");
        }

        // Verificamos la contraseña usando BCrypt
        boolean passwordMatch = passwordEncoder.matches(request.getContrasena(), usuario.getPassword());

        if (!passwordMatch) {
            logger.warn("Contraseña incorrecta para usuario: {}", request.getCorreoElectronico());

            // Incrementar intentos fallidos
            int intentos = usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() + 1 : 1;
            usuario.setIntentosFallidos(intentos);

            if (intentos >= 5) {
                // Bloquear cuenta por 15 minutos
                usuario.setBloqueoHasta(java.time.LocalDateTime.now().plusMinutes(15));
                usuario.setIntentosFallidos(0); // Resetear contador tras bloqueo para nuevo ciclo
                usuarioRepository.save(usuario);

                // Enviar email de alerta
                emailService.sendAccountLockedEmail(usuario.getEmail(), usuario.getNombreCompleto());

                throw new RuntimeException(
                        "Has superado el número de intentos. Tu cuenta ha sido bloqueada por 15 minutos.");
            }

            usuarioRepository.save(usuario);
            throw new RuntimeException("Credenciales inválidas");
        }

        // Login exitoso: Resetear contadores
        if (usuario.getIntentosFallidos() != null && usuario.getIntentosFallidos() > 0) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueoHasta(null);
            usuarioRepository.save(usuario);
        }

        if (!Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
            logger.warn("Intento de login con cuenta no confirmada: {}", request.getCorreoElectronico());
            throw new RuntimeException("Cuenta no verificada. Por favor, revisa tu email para activarla.");
        }

        // 2FA Check
        if (Boolean.TRUE.equals(usuario.getTwoFactorEnabled())) {
            // Generar token con rol limitado
            String tempToken = jwtUtil.generateToken(usuario.getEmail(), "PRE_VERIFICATION");
            return new AuthResponseDTO(
                    tempToken,
                    "PRE_VERIFICATION",
                    usuario.getNombreCompleto(),
                    usuario.getId(),
                    true);
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        logger.info("Login exitoso para usuario: {} con rol: {}", usuario.getEmail(), usuario.getRol());

        return new AuthResponseDTO(
                token,
                usuario.getRol(),
                usuario.getNombreCompleto(),
                usuario.getId(),
                false);
    }

    @Autowired
    private TwoFactorService twoFactorService;

    @Transactional
    public AuthResponseDTO verifyTwoFactorLogin(Long userId, int code) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA no está habilitado para este usuario");
        }

        if (!twoFactorService.validateCode(usuario.getTwoFactorSecret(), code)) {
            // Incrementar intentos fallidos específicos de 2FA si se desea, o usar el
            // global
            throw new RuntimeException("Código 2FA inválido");
        }

        // Éxito, generar token completo
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());
        return new AuthResponseDTO(
                token,
                usuario.getRol(),
                usuario.getNombreCompleto(),
                usuario.getId(),
                false);
    }

    /**
     * Registrar nuevo usuario
     * 
     * @param request Datos del nuevo usuario
     * @return Respuesta con token y datos del usuario creado
     */
    @Transactional
    public AuthResponseDTO registrar(RegistroDTO request) {
        logger.debug("Intentando registrar usuario: {}", request.getCorreoElectronico());

        if (usuarioRepository.existsByEmail(request.getCorreoElectronico())) {
            logger.warn("Intento de registro con email ya existente: {}", request.getCorreoElectronico());
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getCorreoElectronico());
        usuario.setMovil(request.getMovil());

        // Mapeo de nuevos campos
        if (request.getFechaNacimiento() == null) {
            throw new RuntimeException("La fecha de nacimiento es obligatoria");
        }
        // Validación de edad (18+)
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.Period period = java.time.Period.between(request.getFechaNacimiento(), now);
        if (period.getYears() < 18) {
            logger.warn("Intento de registro de menor de edad: {}", request.getCorreoElectronico());
            throw new RuntimeException("Debes tener al menos 18 años para registrarte");
        }

        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setDireccion(request.getDireccion());
        usuario.setCodigoPostal(request.getCodigoPostal());
        if (request.getCiudad() != null && !request.getCiudad().isEmpty()) {
            usuario.setCiudad(request.getCiudad());
        }
        if (request.getPais() != null && !request.getPais().isEmpty()) {
            usuario.setPais(request.getPais());
        }
        // Asignar rol por defecto si viene nulo
        usuario.setRol(request.getTipoUsuario() != null ? request.getTipoUsuario() : "USUARIO");
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getContrasena()));
        usuario.setEmailConfirmado(false); // Requiere confirmación
        String tokenConfirmacion = java.util.UUID.randomUUID().toString();
        usuario.setConfirmationToken(tokenConfirmacion);

        // 2FA Fields Init
        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);

        // Guardamos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        logger.info("Usuario registrado (pendiente confirmar): {} con ID: {}", usuarioGuardado.getEmail(),
                usuarioGuardado.getId());

        // Enviar email
        emailService.sendAccountConfirmationEmail(usuarioGuardado.getEmail(), tokenConfirmacion,
                usuarioGuardado.getNombreCompleto());

        // Devolvemos token nulo para indicar que no está logueado
        return new AuthResponseDTO(
                null,
                usuarioGuardado.getRol(),
                usuarioGuardado.getNombreCompleto(),
                usuarioGuardado.getId(),
                false);
    }

    /**
     * Confirma la cuenta mediante token
     */
    @Transactional
    public void confirmarCuenta(String token) {
        Usuario usuario = usuarioRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de confirmación inválido"));

        usuario.setEmailConfirmado(true);
        usuario.setConfirmationToken(null); // Consumir token
        usuarioRepository.save(usuario);
        logger.info("Cuenta confirmada para usuario: {}", usuario.getEmail());
    }

    /**
     * Reenviar correo de confirmación
     */
    @Transactional
    public void resendConfirmation(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
            throw new RuntimeException("Esta cuenta ya está confirmada. Puedes iniciar sesión.");
        }

        // Generar nuevo token
        String newToken = java.util.UUID.randomUUID().toString();
        usuario.setConfirmationToken(newToken);
        usuarioRepository.save(usuario);

        // Reenviar email
        emailService.sendAccountConfirmationEmail(usuario.getEmail(), newToken, usuario.getNombreCompleto());
        logger.info("Correo de confirmación reenviado a: {}", email);
    }

    /**
     * Login Social (Google/Apple)
     * Recupera o crea el usuario automáticamente
     */
    @Transactional
    public AuthResponseDTO socialLogin(String email, String nombre, String proveedor) {
        logger.info("Procesando login social para: {}", email);

        // Buscar si existe
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        if (usuario == null) {
            // Usuario Nuevo: Registrar automáticamente PERO requerir confirmación
            logger.info("Usuario nuevo detectado en social login. Registrando pendiente de confirmación...");
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombreCompleto(nombre);
            usuario.setRol("USUARIO");
            usuario.setEmailConfirmado(false); // Requiere confirmación manual por email
            usuario.setProvider(proveedor); // Store OAuth provider ("google", "apple", etc.)

            // Generar token de confirmación
            String tokenConfirmacion = java.util.UUID.randomUUID().toString();
            usuario.setConfirmationToken(tokenConfirmacion);

            // Password aleatoria
            usuario.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));

            usuario = usuarioRepository.save(usuario);

            // Enviar email de confirmación
            emailService.sendAccountConfirmationEmail(usuario.getEmail(), tokenConfirmacion,
                    usuario.getNombreCompleto());

            logger.info("Usuario creado y correo enviado. ID: {}", usuario.getId());

            // Retornar sin token para indicar que falta verificar
            return new AuthResponseDTO(
                    null,
                    usuario.getRol(),
                    usuario.getNombreCompleto(),
                    usuario.getId(),
                    false);

        } else {
            // Usuario Existente
            if (!Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
                logger.warn("Usuario existente pero cuenta NO confirmada: {}", email);
                // Opcional: Reenviar correo si ha pasado tiempo? Por ahora lanzamos error
                throw new RuntimeException("Cuenta no verificada. Por favor, revisa tu email para activarla.");
            }
        }

        // Generar JWT si está confirmado
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol());

        return new AuthResponseDTO(
                token,
                usuario.getRol(),
                usuario.getNombreCompleto(),
                usuario.getId(),
                false);
    }
}
