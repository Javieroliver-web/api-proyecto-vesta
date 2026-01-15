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

    public List<Auditoria> obtenerUltimosLogs() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }
}
