package com.meaningfulplaylists.infrastructure.redis.repository;

import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.infrastructure.redis.RedisNamespace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class TracksRedisRepository extends AbstractRedisRepository {

    TracksRedisRepository(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    public void save(Track track) {
        super.save(RedisNamespace.TRACK, track.name().toLowerCase(), track);
    }

    public Optional<Track> findByName(String name) {
        return super.find(RedisNamespace.TRACK, name.toLowerCase(), Track.class);
    }
}
