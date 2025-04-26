package com.meaningfulplaylists.infrastructure.retrofit.interceptors

import com.meaningfulplaylists.infrastructure.interceptors.SpotifyAuthInterceptor
import com.meaningfulplaylists.infrastructure.redis.repository.ClientRedisRepository
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRequestFactory
import com.meaningfulplaylists.utils.TestUtils
import okhttp3.*
import spock.lang.Specification

class SpotifyAuthInterceptorTest extends Specification {
    final String AUTHORIZATION_HEADER = "Authorization"

    ClientRedisRepository mockRedis
    SpotifyRequestFactory mockFactory
    SpotifyAuthInterceptor interceptor

    Interceptor.Chain mockChain
    Request originalRequest

    void setup() {
        mockRedis = Mock(ClientRedisRepository)
        mockFactory = Mock(SpotifyRequestFactory)
        interceptor = new SpotifyAuthInterceptor(mockRedis, mockFactory)

        mockChain = Mock(Interceptor.Chain)
    }

    def "Intercept - should attach the Authorization header if not present"() {
        given:
        originalRequest = new Request.Builder()
                .url("http://localhost")
                .build()
        SpotifyTokenResponse fakeTokenResponse = TestUtils.createSpotifyTokenResponse()

        Request capturedRequest

        when:
        interceptor.intercept(mockChain)

        then:
        1 * mockChain.request() >> originalRequest
        1 * mockRedis.find() >> Optional.of(fakeTokenResponse)
        1 * mockFactory.generateAuthHeader(fakeTokenResponse.accessToken()) >> "Bearer ${fakeTokenResponse.accessToken()}"
        1 * mockChain.proceed(_ as Request) >> { Request req ->
            capturedRequest = req
            return null
        }

        and:
        capturedRequest.header(AUTHORIZATION_HEADER) == "Bearer ${fakeTokenResponse.accessToken()}"
    }

    def "Intercept - should do nothing and proceed if the Authorization header is present"() {
        given:
        originalRequest = new Request.Builder()
                .url("http://localhost")
                .header(AUTHORIZATION_HEADER, "already-present")
                .build()

        when:
        interceptor.intercept(mockChain)

        then:
        1 * mockChain.request() >> originalRequest
        1 * mockChain.proceed(originalRequest)
        0 * _
    }
}
