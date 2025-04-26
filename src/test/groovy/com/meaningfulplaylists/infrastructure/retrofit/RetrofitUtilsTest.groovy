package com.meaningfulplaylists.infrastructure.retrofit

import com.meaningfulplaylists.infrastructure.interceptors.SpotifyAuthInterceptor
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import spock.lang.Specification

class RetrofitUtilsTest extends Specification {
    SpotifyAuthInterceptor mockSpotifyAuthInterceptor

    RetrofitUtils retrofitUtils

    String baseUrl = "https://meaningful-playlists.com/"
    Call mockCall

    def setup() {
        mockSpotifyAuthInterceptor = Mock(SpotifyAuthInterceptor)
        mockCall = Mock(Call)

        retrofitUtils = new RetrofitUtils(mockSpotifyAuthInterceptor)
    }

    def "buildRetrofit - should create Retrofit instance with correct configuration"() {
        when:
        Retrofit result = retrofitUtils.buildRetrofit(baseUrl)

        then:
        result instanceof Retrofit
        result.baseUrl().toString() == baseUrl

        and:
        result.converterFactories().size() > 0
        result.converterFactories()[1] instanceof GsonConverterFactory
    }

    def "buildRetrofitWithAuthInterceptor - should create Retrofit instance with auth interceptor"() {
        when:
        Retrofit result = retrofitUtils.buildRetrofitWithAuthInterceptor(baseUrl)

        then:

        result instanceof Retrofit
        result.baseUrl().toString() == baseUrl

        and:
        result.converterFactories().size() > 0
        result.converterFactories()[1] instanceof GsonConverterFactory
    }

    def "safeExecute should return body when call is successful"() {
        given:
        String body = ""
        Response fakeResponse = Response.success("")


        when:
        Optional result = RetrofitUtils.safeExecute(mockCall)

        then:
        1 * mockCall.execute() >> fakeResponse
        0 * _._

        and:
        result == Optional.of(body)

    }

    def "safeExecute should return empty when call is unsuccessful"() {
        given:
        ResponseBody mockResponseBody = Mock(ResponseBody){
            contentType() >> MediaType.parse("application/json")
            contentLength() >> 12L
            source() >> new Buffer().writeUtf8("Error message")
        }
        Response fakeResponse = Response.error(401, mockResponseBody)

        when:
        Optional result = RetrofitUtils.safeExecute(mockCall)

        then:
        1 * mockCall.execute() >> fakeResponse
        1 * mockCall.request() >> GroovyMock(Request)

        and:
        result == Optional.empty()
    }

    def "safeExecute should return empty when call throws IOException"() {
        when:
        Optional result = RetrofitUtils.safeExecute(mockCall)

        then:
        1 * mockCall.execute() >> { throw new IOException("Network error") }
        0 * _._

        and:
        result == Optional.empty()
    }
}
