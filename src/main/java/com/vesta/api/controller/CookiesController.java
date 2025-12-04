package com.vesta.api.controller;

import com.vesta.api.entity.ConsentimientoCookies;
import com.vesta.api.repository.ConsentimientoCookiesRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cookies")
@CrossOrigin(origins = "*")
public class CookiesController {

    @Autowired
    private ConsentimientoCookiesRepository consentimientoRepository;

    @PostMapping("/consentimiento")
    public ResponseEntity<?> guardarConsentimiento(
            @RequestBody ConsentimientoRequest request,
            HttpServletRequest httpRequest) {
        
        ConsentimientoCookies consentimiento = new ConsentimientoCookies();
        consentimiento.setUsuarioId(request.getUsuarioId());
        consentimiento.setCookiesEsenciales(true); // Siempre activas
        consentimiento.setCookiesAnaliticas(request.getCookiesAnaliticas());
        consentimiento.setCookiesMarketing(request.getCookiesMarketing());
        
        // Guardar IP y User-Agent para auditoría RGPD
        consentimiento.setIpAddress(getClientIP(httpRequest));
        consentimiento.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        consentimientoRepository.save(consentimiento);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Consentimiento guardado correctamente");
        response.put("consentimientoId", consentimiento.getId());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/consentimiento/{usuarioId}")
    public ResponseEntity<?> obtenerConsentimiento(@PathVariable Long usuarioId) {
        return consentimientoRepository.findTopByUsuarioIdOrderByFechaConsentimientoDesc(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // DTO interno
    public static class ConsentimientoRequest {
        private Long usuarioId;
        private Boolean cookiesAnaliticas;
        private Boolean cookiesMarketing;

        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public Boolean getCookiesAnaliticas() { return cookiesAnaliticas; }
        public void setCookiesAnaliticas(Boolean cookiesAnaliticas) { this.cookiesAnaliticas = cookiesAnaliticas; }
        public Boolean getCookiesMarketing() { return cookiesMarketing; }
        public void setCookiesMarketing(Boolean cookiesMarketing) { this.cookiesMarketing = cookiesMarketing; }
    }
}