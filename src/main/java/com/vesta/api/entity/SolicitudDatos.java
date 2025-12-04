package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_datos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDatos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sd_id")
    private Long id;

    @Column(name = "sd_usuario_id")
    private Long usuarioId;

    @Column(name = "sd_tipo_solicitud", length = 50)
    private String tipoSolicitud; // ACCESO, RECTIFICACION, SUPRESION, PORTABILIDAD, OPOSICION

    @Column(name = "sd_estado", length = 30)
    private String estado = "PENDIENTE"; // PENDIENTE, EN_PROCESO, COMPLETADA, RECHAZADA

    @Column(name = "sd_descripcion", length = 500)
    private String descripcion;

    @Column(name = "sd_fecha_solicitud")
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "sd_fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "sd_respuesta", length = 1000)
    private String respuesta;

    @Column(name = "sd_url_descarga")
    private String urlDescarga; // Para datos exportados
}