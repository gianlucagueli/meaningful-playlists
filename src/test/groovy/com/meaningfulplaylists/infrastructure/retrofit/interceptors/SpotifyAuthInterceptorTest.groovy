package com.meaningfulplaylists.infrastructure.retrofit.interceptors

import com.meaningfulplaylists.infrastructure.interceptors.SpotifyAuthInterceptor
import okhttp3.*
import spock.lang.Specification

class SpotifyAuthInterceptorTest extends Specification {
    final String AUTHORIZATION_HEADER = "Authorization"

    SpotifyAuthInterceptor interceptor
    String accessToken

    Interceptor.Chain mockChain
    Request originalRequest

    void setup() {
        accessToken = "fake-access-token"
        interceptor = new SpotifyAuthInterceptor(accessToken)

        mockChain = Mock(Interceptor.Chain)
    }

    def "Intercept - should attach the Authorization header if not present"() {
        given:
        originalRequest = new Request.Builder()
                .url("http://localhost")
                .build()

        Request capturedRequest

        when:
        interceptor.intercept(mockChain)

        then:
        1 * mockChain.request() >> originalRequest
        1 * mockChain.proceed(_ as Request) >> { Request req ->
            capturedRequest = req
            return null
        }

        and:
        capturedRequest.header(AUTHORIZATION_HEADER) == "Bearer ${accessToken}"
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
