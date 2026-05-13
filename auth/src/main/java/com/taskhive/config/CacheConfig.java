package com.taskhive.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // spring.cache.type=simple (기본) → ConcurrentMapCacheManager 자동 구성
    // spring.cache.type=redis  → RedisCacheManager 자동 구성 (Redis 연결 필요)
    // TTL 및 직렬화는 application.yml spring.cache.redis.* 로 제어
}
