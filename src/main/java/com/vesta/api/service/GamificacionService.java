package com.vesta.api.service;

import com.vesta.api.entity.Logro;
import com.vesta.api.entity.RecompensaUsuario;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.LogroRepository;
import com.vesta.api.repository.RecompensaUsuarioRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GamificacionService {

    @Autowired
    private LogroRepository logroRepository;

    @Autowired
    private RecompensaUsuarioRepository recompensaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Asegurar que el usuario tiene una entrada en la tabla de recompensas
    public RecompensaUsuario obtenerOInicializarRecompensa(Long usuarioId) {
        return recompensaRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    RecompensaUsuario nueva = new RecompensaUsuario();
                    nueva.setUsuario(usuario);
                    nueva.setPuntos(0);
                    nueva.setNivel("BRONCE");
                    return recompensaRepository.save(nueva);
                });
    }

    // Verificar y desbloquear logros automáticamente
    @Transactional
    public void verificarLogros(Long usuarioId) {
        RecompensaUsuario recompensa = obtenerOInicializarRecompensa(usuarioId);

        // Lógica de ejemplo: Si tiene puntos, desbloquear "PRIMERA_POLIZA" (Simulado)
        // En un caso real, aquí comprobarías si tiene pólizas en el PolizaRepository
        if (!tieneLogro(recompensa, "PRIMERA_POLIZA")) {
            // Aquí iría la condición real: if (polizaRepository.countByUsuario(...) > 0)
            desbloquearLogro(recompensa, "PRIMERA_POLIZA");
        }
    }

    private boolean tieneLogro(RecompensaUsuario recompensa, String codigo) {
        return logroRepository.existsByRecompensaAndCodigoAndFechaDesbloqueoIsNotNull(recompensa, codigo);
    }

    @Transactional
    public void desbloquearLogro(RecompensaUsuario recompensa, String codigoLogro) {
        // Buscar la plantilla del logro (normalmente tendrías una tabla de 'Definición de Logros', 
        // aquí simplificamos buscando si ya existe uno igual o creándolo al vuelo si es el sistema de plantillas)
        
        // Para este ejemplo, asumimos que DataInitializer crea las plantillas con recompensa=null
        Logro plantilla = logroRepository.findByCodigo(codigoLogro)
                .stream().filter(l -> l.getRecompensa() == null).findFirst()
                .orElse(null);

        if (plantilla != null) {
            Logro nuevoLogroUsuario = new Logro();
            nuevoLogroUsuario.setCodigo(plantilla.getCodigo());
            nuevoLogroUsuario.setNombre(plantilla.getNombre());
            nuevoLogroUsuario.setDescripcion(plantilla.getDescripcion());
            nuevoLogroUsuario.setPuntosRecompensa(plantilla.getPuntosRecompensa());
            nuevoLogroUsuario.setIconoUrl(plantilla.getIconoUrl());
            nuevoLogroUsuario.setRecompensa(recompensa);
            nuevoLogroUsuario.setFechaDesbloqueo(LocalDateTime.now());
            
            logroRepository.save(nuevoLogroUsuario);

            // Sumar puntos
            recompensa.setPuntos(recompensa.getPuntos() + plantilla.getPuntosRecompensa());
            actualizarNivel(recompensa);
            recompensaRepository.save(recompensa);
        }
    }
    
    private void actualizarNivel(RecompensaUsuario r) {
        if (r.getPuntos() >= 1000) r.setNivel("DIAMANTE");
        else if (r.getPuntos() >= 500) r.setNivel("ORO");
        else if (r.getPuntos() >= 200) r.setNivel("PLATA");
        else r.setNivel("BRONCE");
    }

    public Map<String, Object> obtenerProgreso(Long usuarioId) {
        RecompensaUsuario recompensa = obtenerOInicializarRecompensa(usuarioId);
        
        long desbloqueados = logroRepository.countByRecompensaAndFechaDesbloqueoIsNotNull(recompensa);
        // Simplificación: Asumimos 10 logros totales del sistema
        long totalLogros = 10; 
        
        Map<String, Object> progreso = new HashMap<>();
        progreso.put("puntos", recompensa.getPuntos());
        progreso.put("nivel", recompensa.getNivel());
        progreso.put("desbloqueados", desbloqueados);
        progreso.put("total", totalLogros);
        progreso.put("porcentaje", (double) desbloqueados / totalLogros * 100);
        
        return progreso;
    }
}