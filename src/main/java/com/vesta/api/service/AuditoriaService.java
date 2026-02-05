package com.vesta.api.service;

import com.vesta.api.entity.Auditoria;
import com.vesta.api.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrarAccion(String email, String accion, String detalle, String ip) {
        Auditoria log = new Auditoria(email, accion, detalle, ip);
        auditoriaRepository.save(log);
    }

    public org.springframework.data.domain.Page<Auditoria> obtenerLogsPaginados(
            org.springframework.data.domain.Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return auditoriaRepository
                    .findByUsuarioEmailContainingIgnoreCaseOrAccionContainingIgnoreCaseOrDetalleContainingIgnoreCaseOrIpContainingIgnoreCaseOrderByFechaDesc(
                            search, search, search, search, pageable);
        }
        return auditoriaRepository.findAll(pageable);
    }

    public List<Auditoria> obtenerUltimosLogs() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }

    public String exportarLogsUsuario(String email) {
        List<Auditoria> logs = auditoriaRepository.findByUsuarioEmailOrderByFechaDesc(email);
        StringBuilder sb = new StringBuilder();
        sb.append("LOGS DE ACTIVIDAD - USUARIO: ").append(email).append("\n");
        sb.append("================================================================\n\n");

        if (logs.isEmpty()) {
            sb.append("No se han encontrado registros para este usuario.\n");
        } else {
            for (Auditoria log : logs) {
                String fecha = log.getFecha()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                sb.append(String.format("[%s] [%s] %s: %s\n",
                        fecha,
                        log.getIp() != null ? log.getIp() : "N/A",
                        log.getAccion(),
                        log.getDetalle() != null ? log.getDetalle() : ""));
            }
        }
        return sb.toString();
    }
}
