package org.example.cv.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
    public CaffeineCacheManager caffeineCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
        );
        log.info("✅ Caffeine Cache Manager initialized");
        return cacheManager;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory){
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisDefaultTtl))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheConfiguration projectDetailConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(projectDetailTtl))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(factory)
                .withCacheConfiguration("projects", defaultConfig)
                .withCacheConfiguration("projectDetail", projectDetailConfig)
                .cacheDefaults(defaultConfig);

        log.info("✅ Redis Cache Manager initialized with TTL default={}s", redisDefaultTtl);
        return builder.build();
    }

    @Bean
    @Primary
    public CacheManager compositeCacheManager(CaffeineCacheManager caffeineCacheManager, RedisCacheManager redisCacheManager){
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
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.warn("⚠️ Cache GET error on {} for key {}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                log.warn("⚠️ Cache PUT error on {} for key {}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.warn("⚠️ Cache EVICT error on {} for key {}: {}", cache.getName(), key, exception.getMessage());
            }
        };
    }

    @Bean
    public Config redissionConfig(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return config;
    }

    @Bean
    // THAY ĐỔI 5: Đổi tên Bean và kiểu trả về
    public javax.cache.CacheManager jCacheManagerForBucket4j(Config redissonConfig) {
        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = provider.getCacheManager();

        javax.cache.configuration.Configuration<Object, Object> jcacheConfig =
                RedissonConfiguration.fromConfig(redissonConfig);

        // Tên cache này phải khớp với application.yml
        String cacheName = bucket4jCacheName;
        if (cacheManager.getCache(cacheName) == null) {
            // Bây giờ lời gọi này hoàn toàn hợp lệ vì `cacheManager` là kiểu javax.cache.CacheManager
            cacheManager.createCache(cacheName, jcacheConfig);
        }

        log.info("✅ JCache Manager for Bucket4j initialized (via Redisson)");
        return cacheManager;
    }

}
