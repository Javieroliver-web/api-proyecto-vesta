package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "consentimientos_cookies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentimientoCookies {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cc_id")
    private Long id;

    @Column(name = "cc_usuario_id")
    private Long usuarioId;

    @Column(name = "cc_cookies_esenciales")
    private Boolean cookiesEsenciales = true; // Siempre true (no se puede rechazar)

    @Column(name = "cc_cookies_analiticas")
    private Boolean cookiesAnaliticas = false;

    @Column(name = "cc_cookies_marketing")
    private Boolean cookiesMarketing = false;

    @Column(name = "cc_ip_address")
    private String ipAddress;

    @Column(name = "cc_user_agent")
    private String userAgent;

    @Column(name = "cc_fecha_consentimiento")
    private LocalDateTime fechaConsentimiento = LocalDateTime.now();

    @Column(name = "cc_version_politica")
    private String versionPolitica = "1.0"; // Para tracking de cambios en políticas
}