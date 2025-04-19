package com.meaningfulplaylists.infrastructure.redis.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification

abstract class FakeRedisRepository extends Specification {
    protected RedisTemplate<String, Object> mockRedisTemplate
    protected ValueOperations<String, Object> actualRepository

    def setupFakeRepository() {
        mockRedisTemplate = Mock(RedisTemplate)
        actualRepository = Mock(ValueOperations)
        mockRedisTemplate.opsForValue() >> actualRepository
    }

    abstract def createRepository(RedisTemplate<String, Object> redisTemplate)
}