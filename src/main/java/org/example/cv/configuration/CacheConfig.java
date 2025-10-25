package org.example.cv.configuration;

import static org.example.cv.constants.CacheConstant.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@Slf4j
public class CacheConfig {

    @Value("${cache.redis.ttl.default:600}") // TTL mặc định 10 phút
    private long redisDefaultTtl;

    @Value("${cache.redis.ttl.project-detail:300}") // TTL riêng cho project detail
    private long projectDetailTtl;

    @Value("${giffing.bucket4j.cache-name:rate-limit-buckets}")
    private String bucket4jCacheName;

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100) // Sức chứa ban đầu
                .expireAfterWrite(10, TimeUnit.MINUTES) // TTL 10 phút
                .maximumSize(500)); // Kích thước tối đa
        log.info("✅ Caffeine Cache Manager initialized");
        return cacheManager;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // ✅ Hỗ trợ Instant, LocalDateTime,...
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Cấu hình riêng cho từng cache name theo chiến lược
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                // Chi tiết task: TTL 10 phút như đề xuất
                CACHE_TASK_DETAILS.getCacheName(),
                defaultConfig.entryTtl(Duration.ofMinutes(10)),

                // Danh sách task: TTL 5 phút như đề xuất (vì nó thay đổi thường xuyên)
                CACHE_TASK_LISTS.getCacheName(),
                defaultConfig.entryTtl(Duration.ofMinutes(5)),

                // project-detail cache với TTL riêng
                PROJECT_DETAIL.getCacheName(),
                defaultConfig.entryTtl(Duration.ofSeconds(projectDetailTtl)),
                PROJECT_LIST.getCacheName(),
                defaultConfig.entryTtl(Duration.ofSeconds(redisDefaultTtl)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig) // Áp dụng config mặc định
                .withInitialCacheConfigurations(cacheConfigurations) // Áp dụng config riêng
                .build();
    }

    @Bean
    @Primary
    public CacheManager compositeCacheManager(
            CaffeineCacheManager caffeineCacheManager, RedisCacheManager redisCacheManager) {
        CompositeCacheManager cacheManager = new CompositeCacheManager(caffeineCacheManager, redisCacheManager);
        cacheManager.setFallbackToNoOpCache(false);
        log.info("✅ CompositeCacheManager initialized (Caffeine + Redis)");
        return cacheManager;
    }

    /**
     * Custom error handler để tránh app crash khi Redis lỗi
     */
    @Bean
    public SimpleCacheErrorHandler cacheErrorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(
                    RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.warn("⚠️ Cache GET error on {} for key {}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                log.warn("⚠️ Cache PUT error on {} for key {}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(
                    RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.warn("⚠️ Cache EVICT error on {} for key {}: {}", cache.getName(), key, exception.getMessage());
            }
        };
    }

    @Bean
    public Config redissionConfig() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return config;
    }

    @Bean
    public javax.cache.CacheManager jCacheManagerForBucket4j(Config redissonConfig) {
        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = provider.getCacheManager();

        javax.cache.configuration.Configuration<Object, Object> jcacheConfig =
                RedissonConfiguration.fromConfig(redissonConfig);

        String cacheName = bucket4jCacheName;
        if (cacheManager.getCache(cacheName) == null) {
            cacheManager.createCache(cacheName, jcacheConfig);
        }

        log.info("✅ JCache Manager for Bucket4j initialized (via Redisson)");
        return cacheManager;
    }
}
