package com.vesta.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de caché para la aplicación
 * Habilita el soporte de caché con @Cacheable, @CacheEvict, etc.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // La configuración por defecto usa ConcurrentHashMap
    // Para producción, considerar usar Redis o Caffeine
}
