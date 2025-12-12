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
    
    /**
     * Buscar logro por su código único
     * @param codigo Código del logro (ej: "PRIMERA_POLIZA")
     * @return Optional con el logro si existe
     */
    Optional<Logro> findByCodigo(String codigo);
    
    /**
     * Verificar si existe un logro con ese código
     */
    boolean existsByCodigo(String codigo);
    
    /**
     * Buscar todos los logros de un usuario específico
     * @param recompensa La recompensa asociada al usuario
     * @return Lista de logros del usuario
     */
    List<Logro> findByRecompensa(RecompensaUsuario recompensa);
    
    /**
     * Buscar logros desbloqueados ordenados por fecha
     */
    List<Logro> findByRecompensaOrderByFechaDesbloqueoDesc(RecompensaUsuario recompensa);
    
    // ============================================
    // BÚSQUEDAS POR ESTADO
    // ============================================
    
    /**
     * Buscar logros ya desbloqueados por un usuario
     */
    List<Logro> findByRecompensaAndFechaDesbloqueoIsNotNull(RecompensaUsuario recompensa);
    
    /**
     * Contar cuántos logros ha desbloqueado un usuario
     */
    long countByRecompensaAndFechaDesbloqueoIsNotNull(RecompensaUsuario recompensa);
    
    /**
     * Verificar si un usuario tiene un logro específico desbloqueado
     */
    boolean existsByRecompensaAndCodigoAndFechaDesbloqueoIsNotNull(
        RecompensaUsuario recompensa, 
        String codigo
    );
    
    // ============================================
    // BÚSQUEDAS POR PUNTOS
    // ============================================
    
    /**
     * Buscar logros que otorgan más puntos que un valor dado
     */
    List<Logro> findByPuntosRecompensaGreaterThanEqual(Integer puntos);
    
    /**
     * Buscar logros ordenados por puntos (mayor a menor)
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa IS NULL ORDER BY l.puntosRecompensa DESC")
    List<Logro> findAllLogrosDisponiblesOrderByPuntosDesc();
    
    /**
     * Sumar total de puntos obtenidos por logros de un usuario
     */
    @Query("SELECT COALESCE(SUM(l.puntosRecompensa), 0) FROM Logro l " +
           "WHERE l.recompensa = :recompensa AND l.fechaDesbloqueo IS NOT NULL")
    Integer sumPuntosByRecompensa(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // BÚSQUEDAS POR FECHA
    // ============================================
    
    /**
     * Buscar logros desbloqueados en un rango de fechas
     */
    List<Logro> findByRecompensaAndFechaDesbloqueoBetween(
        RecompensaUsuario recompensa,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
    );
    
    /**
     * Buscar logros desbloqueados hoy
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND DATE(l.fechaDesbloqueo) = CURRENT_DATE")
    List<Logro> findLogrosDesbloqueadosHoy(@Param("recompensa") RecompensaUsuario recompensa);
    
    /**
     * Buscar logros desbloqueados esta semana
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND l.fechaDesbloqueo >= CURRENT_DATE - 7")
    List<Logro> findLogrosDesbloqueadosEstaSemana(@Param("recompensa") RecompensaUsuario recompensa);
    
    /**
     * Buscar logros desbloqueados este mes
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND MONTH(l.fechaDesbloqueo) = MONTH(CURRENT_DATE) " +
           "AND YEAR(l.fechaDesbloqueo) = YEAR(CURRENT_DATE)")
    List<Logro> findLogrosDesbloqueadosEsteMes(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // ESTADÍSTICAS Y ANALYTICS
    // ============================================
    
    /**
     * Obtener progreso de logros (porcentaje completado)
     */
    @Query("SELECT " +
           "(CAST(COUNT(CASE WHEN l.fechaDesbloqueo IS NOT NULL THEN 1 END) AS double) / " +
           "CAST(COUNT(*) AS double)) * 100 " +
           "FROM Logro l WHERE l.recompensa = :recompensa OR l.recompensa IS NULL")
    Double calcularProgresoLogros(@Param("recompensa") RecompensaUsuario recompensa);
    
    /**
     * Obtener el logro más reciente desbloqueado
     */
    Optional<Logro> findFirstByRecompensaAndFechaDesbloqueoIsNotNullOrderByFechaDesbloqueoDesc(
        RecompensaUsuario recompensa
    );
    
    /**
     * Obtener logros por categoría/tipo
     */
    @Query("SELECT l FROM Logro l WHERE l.codigo LIKE :prefijo%")
    List<Logro> findByCategoria(@Param("prefijo") String prefijoCodigo);
    
    /**
     * Logros más comunes (más usuarios los tienen)
     */
    @Query("SELECT l.codigo, COUNT(l) as total FROM Logro l " +
           "WHERE l.fechaDesbloqueo IS NOT NULL " +
           "GROUP BY l.codigo " +
           "ORDER BY total DESC")
    List<Object[]> findLogrosMasComunes();
    
    /**
     * Logros más raros (menos usuarios los tienen)
     */
    @Query("SELECT l.codigo, COUNT(l) as total FROM Logro l " +
           "WHERE l.fechaDesbloqueo IS NOT NULL " +
           "GROUP BY l.codigo " +
           "ORDER BY total ASC")
    List<Object[]> findLogrosMasRaros();
    
    // ============================================
    // LOGROS DISPONIBLES (NO DESBLOQUEADOS)
    // ============================================
    
    /**
     * Obtener logros que el usuario aún no ha desbloqueado
     * Compara todos los logros con los que ya tiene
     */
    @Query("SELECT l FROM Logro l WHERE l.codigo NOT IN " +
           "(SELECT lo.codigo FROM Logro lo WHERE lo.recompensa = :recompensa " +
           "AND lo.fechaDesbloqueo IS NOT NULL)")
    List<Logro> findLogrosDisponibles(@Param("recompensa") RecompensaUsuario recompensa);
    
    /**
     * Contar logros disponibles (no desbloqueados)
     */
    @Query("SELECT COUNT(DISTINCT l.codigo) FROM Logro l WHERE l.codigo NOT IN " +
           "(SELECT lo.codigo FROM Logro lo WHERE lo.recompensa = :recompensa " +
           "AND lo.fechaDesbloqueo IS NOT NULL)")
    long countLogrosDisponibles(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // LEADERBOARD Y RANKINGS
    // ============================================
    
    /**
     * Obtener top 10 usuarios con más logros
     */
    @Query("SELECT r.usuario.nombreCompleto, COUNT(l) as total " +
           "FROM Logro l JOIN l.recompensa r " +
           "WHERE l.fechaDesbloqueo IS NOT NULL " +
           "GROUP BY r.usuario.id, r.usuario.nombreCompleto " +
           "ORDER BY total DESC")
    List<Object[]> findTop10UsuariosConMasLogros();
    
    /**
     * Ranking de un usuario específico (posición global)
     */
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
    // BÚSQUEDAS PERSONALIZADAS AVANZADAS
    // ============================================
    
    /**
     * Buscar logros relacionados con una palabra clave
     */
    @Query("SELECT l FROM Logro l WHERE " +
           "LOWER(l.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Logro> searchLogros(@Param("keyword") String keyword);
    
    /**
     * Verificar si un usuario puede desbloquear un logro específico
     * (basado en condiciones personalizadas)
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
           "FROM Logro l WHERE l.codigo = :codigo AND l.recompensa = :recompensa " +
           "AND l.fechaDesbloqueo IS NULL")
    boolean puedeDesbloquear(
        @Param("recompensa") RecompensaUsuario recompensa,
        @Param("codigo") String codigo
    );
    
    /**
     * Obtener logros cercanos a desbloquear (con progreso > 50%)
     * Nota: Esto requeriría una columna adicional de progreso en la entidad
     */
    @Query("SELECT l FROM Logro l WHERE l.recompensa = :recompensa " +
           "AND l.fechaDesbloqueo IS NULL " +
           "ORDER BY l.puntosRecompensa DESC")
    List<Logro> findLogrosCercanosADesbloquear(@Param("recompensa") RecompensaUsuario recompensa);
    
    // ============================================
    // OPERACIONES DE MANTENIMIENTO
    // ============================================
    
    /**
     * Eliminar logros antiguos no desbloqueados (limpieza)
     */
    @Query("DELETE FROM Logro l WHERE l.fechaDesbloqueo IS NULL " +
           "AND l.recompensa IS NOT NULL")
    void eliminarLogrosNoDesbloqueados();
    
    /**
     * Contar total de logros en el sistema
     */
    @Query("SELECT COUNT(DISTINCT l.codigo) FROM Logro l")
    long countTotalLogrosSistema();
    
    /**
     * Obtener distribución de logros por puntos
     */
    @Query("SELECT l.puntosRecompensa, COUNT(l) FROM Logro l " +
           "GROUP BY l.puntosRecompensa " +
           "ORDER BY l.puntosRecompensa")
    List<Object[]> findDistribucionPorPuntos();
}