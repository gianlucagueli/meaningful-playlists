package com.meaningfulplaylists.infrastructure.redis.repository;

import com.meaningfulplaylists.infrastructure.redis.RedisNamespace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public abstract class AbstractRedisRepository {

    private final ValueOperations<String, Object> repository;

    protected AbstractRedisRepository(RedisTemplate<String, Object> redisTemplate) {
        this.repository = redisTemplate.opsForValue();
    }

    protected void save(RedisNamespace namespace, String id, Object value) {
        this.save(namespace, id, value, null);
    }

    protected void save(RedisNamespace namespace, String id, Object value, Long ttl) {
        log.info("Saving {} with key: {}, value: {} and ttl: {}", namespace, id, value, ttl);
        String key = namespace.key(id);

        if (ttl != null && ttl > 0) {
            repository.set(key, value, Duration.ofSeconds(ttl));
            return;
        }

        repository.set(key, value);
    }

    protected  <T> Optional<T> find(RedisNamespace namespace, String id, Class<T> type) {
        return Optional.of(namespace.key(id))
                .map(repository::get)
                .filter(type::isInstance)
                .map(type::cast);
    }
}
