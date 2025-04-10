package com.meaningfulplaylists.infrastructure.redis.repository;

import com.meaningfulplaylists.infrastructure.redis.RedisNamespace;
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyMissingStateException;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UserRedisRepository extends AbstractRedisRepository {
    UserRedisRepository(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    public void saveUser(String userId, SpotifyTokenResponse value) {
        super.save(RedisNamespace.USER, userId, value);
    }

    public void saveState(String state, String userId) {
        super.save(RedisNamespace.STATE, state, userId);
    }

    public SpotifyTokenResponse findTokenByUserId(String userId) {
        return super.find(RedisNamespace.USER, userId, SpotifyTokenResponse.class)
                .orElseThrow(() -> new RuntimeException("Error finding user with id: " + userId));
    }

    public String findUserIdByState(String state) {
        return super.find(RedisNamespace.STATE, state, String.class)
                .orElseThrow(() -> new SpotifyMissingStateException(state));
    }
}
