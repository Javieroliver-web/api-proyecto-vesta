package com.vesta.api.repository;

import com.vesta.api.entity.Logro;
import com.vesta.api.entity.RecompensaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar logros y achievements
 * Ubicación: vesta-api/src/main/java/com/vesta/api/repository/LogroRepository.java
 */
@Repository
public interface LogroRepository extends JpaRepository<Logro, Long> {
    
    // ============================================
    // BÚSQUEDAS BÁSICAS
    // ============================================
    
    Optional<Logro> findByCodigo(String codigo);
    
    boolean existsByCodigo(String codigo);
    
    List<Logro> findByRecompensa(RecompensaUsuario recompensa);
    
    List<Logro> findByRecompensaOrderByFechaDesbloqueoDesc(RecompensaUsuario recompensa);
    
    // ============================================
    // BÚSQUEDAS POR ESTADO
    // ============================================
    
    List<Logro> findByRecompensaAndFechaDesbloqueoIsNotNull(RecompensaUsuario recompensa);
    
    long countByRecompensaAndFechaDesbloqueoIsNotNull(RecompensaUsuario recompensa);
    
    boolean existsByRecompensaAndCodigoAndFechaDesbloqueoIsNotNull(
        RecompensaUsuario recompensa, 
        String codigo
    );
    
    // ============================================
    // BÚSQUEDAS POR PUNTOS
    // ============================================
    
    List<Logro> findByPuntosRecompensaGreaterThanEqual(Integer puntos);
    
    @Query("SELECT l FROM Logro l WHERE l.recompensa IS NULL ORDER BY l.puntosRecompensa DESC")
    List<Logro> findAllLogrosDisponiblesOrderByPuntosDesc();
    
    @Query("SELECT COALESCE(SUM(l.puntosRecompensa), 0) FROM Logro l " +
           "WHERE l.recompensa = :recompensa AND l.fechaDesbloqueo IS NOT NULL")
    Integer sumPuntosByRecompensa(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // BÚSQUEDAS POR FECHA (CORREGIDAS)
    // ============================================
    
    List<Logro> findByRecompensaAndFechaDesbloqueoBetween(
        RecompensaUsuario recompensa,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
    );
    
    /**
     * Buscar logros desbloqueados hoy.
     * CORRECCIÓN: Usamos cast a LocalDate para comparar con current_date en Hibernate 6
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND cast(l.fechaDesbloqueo as LocalDate) = current_date")
    List<Logro> findLogrosDesbloqueadosHoy(@Param("recompensa") RecompensaUsuario recompensa);
    
    /**
     * Buscar logros desbloqueados esta semana.
     * CORRECCIÓN: Usamos Query Nativa para facilitar la aritmética de fechas en PostgreSQL (CURRENT_DATE - 7)
     */
    @Query(value = "SELECT * FROM logros WHERE recompensa_id = :#{#recompensa.id} " +
           "AND fecha_desbloqueo >= CURRENT_DATE - INTERVAL '7 days'", nativeQuery = true)
    List<Logro> findLogrosDesbloqueadosEstaSemana(@Param("recompensa") RecompensaUsuario recompensa);
    
    /**
     * Buscar logros desbloqueados este mes.
     * CORRECCIÓN: Usamos extract(month/year) que es estándar en JPQL/HQL
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND extract(month from l.fechaDesbloqueo) = extract(month from current_date) " +
           "AND extract(year from l.fechaDesbloqueo) = extract(year from current_date)")
    List<Logro> findLogrosDesbloqueadosEsteMes(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // ESTADÍSTICAS Y ANALYTICS
    // ============================================
    
    @Query("SELECT " +
           "(CAST(COUNT(CASE WHEN l.fechaDesbloqueo IS NOT NULL THEN 1 END) AS double) / " +
           "NULLIF(CAST(COUNT(*) AS double), 0)) * 100 " +
           "FROM Logro l WHERE l.recompensa = :recompensa OR l.recompensa IS NULL")
    Double calcularProgresoLogros(@Param("recompensa") RecompensaUsuario recompensa);
    
    Optional<Logro> findFirstByRecompensaAndFechaDesbloqueoIsNotNullOrderByFechaDesbloqueoDesc(
        RecompensaUsuario recompensa
    );
    
    @Query("SELECT l FROM Logro l WHERE l.codigo LIKE :prefijo%")
    List<Logro> findByCategoria(@Param("prefijo") String prefijoCodigo);
    
    @Query("SELECT l.codigo, COUNT(l) as total FROM Logro l " +
           "WHERE l.fechaDesbloqueo IS NOT NULL " +
           "GROUP BY l.codigo " +
           "ORDER BY total DESC")
    List<Object[]> findLogrosMasComunes();
    
    @Query("SELECT l.codigo, COUNT(l) as total FROM Logro l " +
           "WHERE l.fechaDesbloqueo IS NOT NULL " +
           "GROUP BY l.codigo " +
           "ORDER BY total ASC")
    List<Object[]> findLogrosMasRaros();
    
    // ============================================
    // LOGROS DISPONIBLES
    // ============================================
    
    @Query("SELECT l FROM Logro l WHERE l.codigo NOT IN " +
           "(SELECT lo.codigo FROM Logro lo WHERE lo.recompensa = :recompensa " +
           "AND lo.fechaDesbloqueo IS NOT NULL)")
    List<Logro> findLogrosDisponibles(@Param("recompensa") RecompensaUsuario recompensa);
    
    @Query("SELECT COUNT(DISTINCT l.codigo) FROM Logro l WHERE l.codigo NOT IN " +
           "(SELECT lo.codigo FROM Logro lo WHERE lo.recompensa = :recompensa " +
           "AND lo.fechaDesbloqueo IS NOT NULL)")
    long countLogrosDisponibles(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // LEADERBOARD Y RANKINGS
    // ============================================
    
    @Query("SELECT r.usuario.nombreCompleto, COUNT(l) as total " +
           "FROM Logro l JOIN l.recompensa r " +
           "WHERE l.fechaDesbloqueo IS NOT NULL " +
           "GROUP BY r.usuario.id, r.usuario.nombreCompleto " +
           "ORDER BY total DESC")
    List<Object[]> findTop10UsuariosConMasLogros();
    
    @Query(value = "SELECT posicion FROM (" +
           "SELECT r.usu_id, ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC) as posicion " +
           "FROM logros l " +
           "JOIN recompensas_usuario r ON l.recompensa_id = r.id " +
           "WHERE l.fecha_desbloqueo IS NOT NULL " +
           "GROUP BY r.usu_id" +
           ") ranking WHERE usu_id = :usuarioId", 
           nativeQuery = true)
    Integer findRankingUsuario(@Param("usuarioId") Long usuarioId);
    
    // ============================================
    // BÚSQUEDAS AVANZADAS
    // ============================================
    
    @Query("SELECT l FROM Logro l WHERE " +
           "LOWER(l.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Logro> searchLogros(@Param("keyword") String keyword);
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
           "FROM Logro l WHERE l.codigo = :codigo AND l.recompensa = :recompensa " +
           "AND l.fechaDesbloqueo IS NULL")
    boolean puedeDesbloquear(
        @Param("recompensa") RecompensaUsuario recompensa,
        @Param("codigo") String codigo
    );
    
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND l.fechaDesbloqueo IS NULL " +
           "ORDER BY l.puntosRecompensa DESC")
    List<Logro> findLogrosCercanosADesbloquear(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // MANTENIMIENTO
    // ============================================
    
    @Query("DELETE FROM Logro l WHERE l.fechaDesbloqueo IS NULL " +
           "AND l.recompensa IS NOT NULL")
    void eliminarLogrosNoDesbloqueados();
    
    @Query("SELECT COUNT(DISTINCT l.codigo) FROM Logro l")
    long countTotalLogrosSistema();
    
    @Query("SELECT l.puntosRecompensa, COUNT(l) FROM Logro l " +
           "GROUP BY l.puntosRecompensa " +
           "ORDER BY l.puntosRecompensa")
    List<Object[]> findDistribucionPorPuntos();
}