package com.meaningfulplaylists.infrastructure.redis.repository;

import com.meaningfulplaylists.infrastructure.redis.RedisNamespace;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class ClientRedisRepository extends AbstractRedisRepository {
    private static final String CLIENT_KEY = "client";
    private static final Long CLIENT_TTL = 3600L;

    ClientRedisRepository(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    public void save(SpotifyTokenResponse value) {
        super.save(RedisNamespace.CLIENT, CLIENT_KEY, value, CLIENT_TTL);
    }

    public Optional<SpotifyTokenResponse> find() {
        return super.find(RedisNamespace.CLIENT, CLIENT_KEY, SpotifyTokenResponse.class);
    }
}
