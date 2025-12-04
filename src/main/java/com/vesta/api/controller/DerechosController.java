package com.vesta.api.controller;

import com.vesta.api.entity.SolicitudDatos;
import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.SolicitudDatosRepository;
import com.vesta.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/derechos")
@CrossOrigin(origins = "*")
public class DerechosController {

    @Autowired
    private SolicitudDatosRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Derecho de ACCESO - Art. 15 RGPD
    @PostMapping("/solicitar-acceso")
    public ResponseEntity<?> solicitarAcceso(@RequestBody SolicitudRequest request) {
        SolicitudDatos solicitud = new SolicitudDatos();
        solicitud.setUsuarioId(request.getUsuarioId());
        solicitud.setTipoSolicitud("ACCESO");
        solicitud.setDescripcion("Solicitud de acceso a datos personales");
        solicitud.setEstado("PENDIENTE");
        
        solicitudRepository.save(solicitud);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Solicitud registrada. Responderemos en un plazo máximo de 30 días.");
        response.put("solicitudId", solicitud.getId());
        
        return ResponseEntity.ok(response);
    }

    // Derecho de RECTIFICACIÓN - Art. 16 RGPD
    @PostMapping("/solicitar-rectificacion")
    public ResponseEntity<?> solicitarRectificacion(@RequestBody SolicitudRequest request) {
        SolicitudDatos solicitud = new SolicitudDatos();
        solicitud.setUsuarioId(request.getUsuarioId());
        solicitud.setTipoSolicitud("RECTIFICACION");
        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setEstado("PENDIENTE");
        
        solicitudRepository.save(solicitud);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Solicitud de rectificación registrada.");
        response.put("solicitudId", solicitud.getId());
        
        return ResponseEntity.ok(response);
    }

    // Derecho de SUPRESIÓN (derecho al olvido) - Art. 17 RGPD
    @PostMapping("/solicitar-supresion")
    public ResponseEntity<?> solicitarSupresion(@RequestBody SolicitudRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Soft delete - Marcamos como eliminado pero conservamos para obligaciones legales
        usuario.setDatosEliminados(true);
        usuario.setFechaEliminacion(java.time.LocalDateTime.now());
        usuario.setRazonEliminacion(request.getDescripcion());
        usuarioRepository.save(usuario);

        SolicitudDatos solicitud = new SolicitudDatos();
        solicitud.setUsuarioId(request.getUsuarioId());
        solicitud.setTipoSolicitud("SUPRESION");
        solicitud.setDescripcion("Derecho al olvido ejercido");
        solicitud.setEstado("COMPLETADA");
        solicitud.setFechaRespuesta(java.time.LocalDateTime.now());
        
        solicitudRepository.save(solicitud);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tus datos han sido marcados para eliminación según el Art. 17 RGPD.");
        
        return ResponseEntity.ok(response);
    }

    // Derecho de PORTABILIDAD - Art. 20 RGPD
    @PostMapping("/solicitar-portabilidad")
    public ResponseEntity<?> solicitarPortabilidad(@RequestBody SolicitudRequest request) {
        SolicitudDatos solicitud = new SolicitudDatos();
        solicitud.setUsuarioId(request.getUsuarioId());
        solicitud.setTipoSolicitud("PORTABILIDAD");
        solicitud.setDescripcion("Solicitud de exportación de datos en formato estructurado");
        solicitud.setEstado("PENDIENTE");
        
        solicitudRepository.save(solicitud);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Recibirás tus datos en formato JSON en un plazo de 30 días.");
        response.put("solicitudId", solicitud.getId());
        
        return ResponseEntity.ok(response);
    }

    // Listar solicitudes de un usuario
    @GetMapping("/mis-solicitudes/{usuarioId}")
    public ResponseEntity<List<SolicitudDatos>> misSolicitudes(@PathVariable Long usuarioId) {
        List<SolicitudDatos> solicitudes = solicitudRepository.findByUsuarioIdOrderByFechaSolicitudDesc(usuarioId);
        return ResponseEntity.ok(solicitudes);
    }

    // DTO interno
    public static class SolicitudRequest {
        private Long usuarioId;
        private String descripcion;

        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }
}