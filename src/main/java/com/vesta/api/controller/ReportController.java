package com.vesta.api.controller;

import com.vesta.api.entity.Usuario;
import com.vesta.api.repository.UsuarioRepository;
import com.vesta.api.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/polizas/pdf")
    public ResponseEntity<byte[]> descargarResumenPolizas(Authentication authentication) {
        try {
            // Verificar usuario autenticado
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            byte[] pdfBytes = pdfService.generarResumenPolizas(usuario.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // "attachment" fuerza la descarga, "inline" lo muestra en el navegador
            // (preview)
            // Requisito dice "Descarga", así que usaremos attachment o dejaremos que el
            // navegador decida.
            // Para mejor UX, attachment con nombre.
            headers.setContentDispositionFormData("attachment", "Vesta_Resumen_Polizas.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
