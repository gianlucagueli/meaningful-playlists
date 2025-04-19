package com.meaningfulplaylists.infrastructure.redis.repository

import com.meaningfulplaylists.infrastructure.redis.RedisNamespace
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyMissingStateException
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.utils.TestUtils
import org.springframework.data.redis.core.RedisTemplate

class UserRedisRepositoryTest extends FakeRedisRepository {
    UserRedisRepository userRepository

    void setup() {
        setupFakeRepository()
        userRepository = createRepository(mockRedisTemplate)
    }

    @Override
    def createRepository(RedisTemplate<String, Object> redisTemplate) {
        return new UserRedisRepository(redisTemplate)
    }

    def "SaveUser - should correctly save the user"() {
        given:
        String userId = "user-id"
        SpotifyTokenResponse tokenResponse = TestUtils.createSpotifyTokenResponse()

        when:
        userRepository.saveUser(userId, tokenResponse)

        then:
        1 * actualRepository.set(RedisNamespace.USER.key(userId), tokenResponse)

        and:
        0 * _._
    }

    def "SaveState - should correctly save the state"() {
        given:
        String state = "state"
        String userId = "user-id"

        when:
        userRepository.saveState(state, userId)

        then:
        1 * actualRepository.set(RedisNamespace.STATE.key(state), userId)

        and:
        0 * _._
    }

    def "findTokenByUserId - should return the token when user exists"() {
        given:
        String userId = "user-id"
        SpotifyTokenResponse token = TestUtils.createSpotifyTokenResponse()
        String expectedKey = RedisNamespace.USER.key(userId)

        when:
        SpotifyTokenResponse result = userRepository.findTokenByUserId(userId)

        then:
        1 * actualRepository.get(expectedKey) >> token
        0 * _._

        and:
        result == token
    }

    def "findTokenByUserId - should throw RuntimeException when user does not exist"() {
        given:
        String userId = "non-existent-user-id"
        String expectedKey = RedisNamespace.USER.key(userId)

        when:
        userRepository.findTokenByUserId(userId)

        then:
        1 * actualRepository.get(expectedKey) >> null
        0 * _._

        and:
        RuntimeException exception = thrown(RuntimeException)
        exception.message == "Error finding user with id: " + userId
    }

    def "findUserIdByState - should return the userId when state exists"() {
        given:
        String state = "state"
        String userId = "user-id"
        String expectedKey = RedisNamespace.STATE.key(state)

        when:
        String result = userRepository.findUserIdByState(state)

        then:
        1 * actualRepository.get(expectedKey) >> userId
        0 * _._

        and:
        result == userId
    }

    def "findUserIdByState - should throw SpotifyMissingStateException when state does not exist"() {
        given:
        String state = "non-existent-state"
        String expectedKey = RedisNamespace.STATE.key(state)

        when:
        userRepository.findUserIdByState(state)

        then:
        1 * actualRepository.get(expectedKey) >> null
        0 * _._

        and:
        thrown(SpotifyMissingStateException)
    }
}
