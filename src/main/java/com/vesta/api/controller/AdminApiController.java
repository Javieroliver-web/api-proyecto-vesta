package com.vesta.api.controller;

import com.vesta.api.repository.OrdenRepository;
import com.vesta.api.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "${cors.allowed.origins:*}")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private SiniestroRepository siniestroRepository;

    // Log logger
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AdminApiController.class);

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Obtener estadísticas de ventas (DB Aggregation)
        // Expected structure from DB: [{year=2024, month=1, total=100.0}, ...]
        List<Map<String, Object>> dbVentas = ordenRepository.findSalesStatsByYearMonth();

        List<Map<String, Object>> ventasStats = new ArrayList<>();
        if (dbVentas != null) {
            for (Map<String, Object> row : dbVentas) {
                // Ensure order is preserved as DB query has ORDER BY
                // We just sanitize/format the output if needed
                Map<String, Object> mesStat = new HashMap<>();
                // Extract values carefully as they might be Long, BigInteger, Double depending
                // on DB
                Object monthObj = row.get("month");
                Object totalObj = row.get("total");

                mesStat.put("mes", monthObj);
                mesStat.put("total", totalObj);
                ventasStats.add(mesStat);
            }
        }
        stats.put("ventas", ventasStats);

        // 2. Obtener estadísticas de siniestros por categoría (DB Aggregation)
        List<Map<String, Object>> dbSiniestros = siniestroRepository.countSiniestrosByCategoria();

        List<Map<String, Object>> siniestrosStats = new ArrayList<>();
        if (dbSiniestros != null) {
            for (Map<String, Object> row : dbSiniestros) {
                Map<String, Object> catStat = new HashMap<>();
                catStat.put("categoria", row.get("categoria"));
                catStat.put("cantidad", row.get("cantidad"));
                siniestrosStats.add(catStat);
            }
        }
        stats.put("siniestros", siniestrosStats);

        // 3. Log excluded records (Quality Check)
        Long excludedCount = siniestroRepository.countSiniestrosSinCategoria();
        if (excludedCount != null && excludedCount > 0) {
            logger.warn(
                    "Data Quality: {} siniestros were excluded from statistics due to missing Policy or Product data.",
                    excludedCount);
            stats.put("siniestros_excluded", excludedCount);
        }

        return ResponseEntity.ok(stats);
    }
}
