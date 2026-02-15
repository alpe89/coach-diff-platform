package com.coachdiff.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  CacheManager cacheManager() {
    var cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(
        List.of(
            buildCache("account-details", 1),
            buildCache("match-details", 20),
            buildCache("match-timelines", 20)));
    return cacheManager;
  }

  private CaffeineCache buildCache(String name, int maximumSize) {
    return new CaffeineCache(name, Caffeine.newBuilder().maximumSize(maximumSize).build());
  }
}
