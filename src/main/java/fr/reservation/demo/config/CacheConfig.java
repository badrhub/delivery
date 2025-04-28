package fr.reservation.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching // <-- Active le système de cache
public class CacheConfig {
}

