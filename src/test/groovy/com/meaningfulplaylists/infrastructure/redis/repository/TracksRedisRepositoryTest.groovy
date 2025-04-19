package com.meaningfulplaylists.infrastructure.redis.repository

import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.redis.RedisNamespace
import com.meaningfulplaylists.utils.TestUtils
import org.springframework.data.redis.core.RedisTemplate

class TracksRedisRepositoryTest extends FakeRedisRepository {
    TracksRedisRepository trackRepository

    void setup() {
        setupFakeRepository()
        trackRepository = createRepository(mockRedisTemplate)
    }

    @Override
    def createRepository(RedisTemplate<String, Object> redisTemplate) {
        return new TracksRedisRepository(redisTemplate)
    }

    def "Save - should correctly store the track without any TTL"() {
        given:
        Track track = TestUtils.createTrack()

        when:
        trackRepository.save(track)

        then:
        1 * actualRepository.set(
                RedisNamespace.TRACK.key(track.name()),
                track
        )

        and:
        0 * _._
    }

    def "Find - should return an optional of the track found"() {
        given:
        Track track = TestUtils.createTrack()
        String name = track.name()
        String expectedKey = RedisNamespace.TRACK.key(name)

        when:
        Optional<Track> result = trackRepository.findByName(name)

        then:
        1 * actualRepository.get(expectedKey) >> track
        0 * _._

        and:
        result.get() == track
    }

    def "Find - should return an empty optional if no track with the given title is found"() {
        given:
        Track track = TestUtils.createTrack()
        String name = track.name()
        String expectedKey = RedisNamespace.TRACK.key(name)

        when:
        Optional<Track> result = trackRepository.findByName(name)

        then:
        1 * actualRepository.get(expectedKey) >> null
        0 * _._

        and:
        !result.isPresent()
    }
}
