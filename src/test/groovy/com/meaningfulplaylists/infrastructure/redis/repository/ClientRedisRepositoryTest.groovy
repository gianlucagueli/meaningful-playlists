package com.meaningfulplaylists.infrastructure.redis.repository

import com.meaningfulplaylists.infrastructure.redis.RedisNamespace
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.utils.TestUtils
import org.springframework.data.redis.core.RedisTemplate

import java.time.Duration

class ClientRedisRepositoryTest extends FakeRedisRepository {
    ClientRedisRepository clientRepository

    void setup() {
        setupFakeRepository()
        clientRepository = createRepository(mockRedisTemplate)
    }

    @Override
    def createRepository(RedisTemplate<String, Object> redisTemplate) {
        return new ClientRedisRepository(redisTemplate)
    }

    def "Save - should correctly store the client with the specified TTL"() {
        given:
        SpotifyTokenResponse clientData = TestUtils.createSpotifyTokenResponse()

        when:
        clientRepository.save(clientData)

        then:
        1 * actualRepository.set(
                RedisNamespace.CLIENT.key(ClientRedisRepository.CLIENT_KEY),
                clientData,
                Duration.ofSeconds(ClientRedisRepository.CLIENT_TTL)
        )

        and:
        0 * _._
    }

    def "Find - should return an optional of the clientData stored"() {
        given:
        SpotifyTokenResponse clientData = TestUtils.createSpotifyTokenResponse()
        String expectedKey = RedisNamespace.CLIENT.key(ClientRedisRepository.CLIENT_KEY)

        when:
        Optional<SpotifyTokenResponse> result = clientRepository.find()

        then:
        1 * actualRepository.get(expectedKey) >> clientData
        0 * _._

        and:
        result.get() == clientData
    }

    def "Find - should return an empty optional if nothing is found"() {
        given:
        String expectedKey = RedisNamespace.CLIENT.key(ClientRedisRepository.CLIENT_KEY)

        when:
        Optional<SpotifyTokenResponse> result = clientRepository.find()

        then:
        1 * actualRepository.get(expectedKey) >> null
        0 * _._

        and:
        !result.isPresent()
    }
}
