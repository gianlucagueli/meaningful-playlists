package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.infrastructure.spotify.SpotifyAccount
import com.meaningfulplaylists.infrastructure.spotify.SpotifyApi
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile
import com.meaningfulplaylists.utils.TestUtils
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

class SpotifyAuthServiceTest extends Specification {
    SpotifyConfig mockConfigs;
    SpotifyAccount mockSpotifyAccount
    SpotifyApi mockSpotifyApi

    Call mockCall
    SpotifyRedirectUrlBuilder redirectUrlBuilder;

    SpotifyAuthService authService

    String clientId = "client-id"
    String clientSecret = "client-secret"
    String redirectUri = "redirect-uri"

    void setup() {
        mockConfigs = Mock(SpotifyConfig)

        mockSpotifyAccount = Mock(SpotifyAccount)
        mockSpotifyApi = Mock(SpotifyApi)
        mockCall = Mock(Call)

        redirectUrlBuilder = Mock(SpotifyRedirectUrlBuilder)

        authService = new SpotifyAuthService(mockConfigs, redirectUrlBuilder, clientId, clientSecret, redirectUri)
    }

    def "CreateRedirectUrl - should generate a redirect URL and map the state to a user session"() {
        given:
        Action action = Action.CREATE_PLAYLIST
        String randomState = "12345abcde"
        String generatedUrl = "generated-redirect-url"

        when:
        String result = authService.createRedirectUrl(action)

        then:
        1 * redirectUrlBuilder.generateRandomState() >> randomState
        1 * redirectUrlBuilder.generateRedirectUrl(randomState, action) >> generatedUrl
        generatedUrl == result
        authService.mapStateUserId.containsKey(randomState)
    }

    def "HandleCallback -"() {
        given:
        SpotifyTokenResponse fakeResponse = TestUtils.createSpotifyTokenResponse()
        SpotifyUserProfile fakeUserProfile = TestUtils.createSpotifyUserProfile()
        String code = "code-1234-567-890"
        String state = "state-1234"

        authService.mapStateUserId.put(state, null)

        when:
        authService.handleCallback(code, state)

        then:
        1 * mockConfigs.getSpotifyAccount() >> mockSpotifyAccount
        1 * mockSpotifyAccount.getAccessToken(_, code, redirectUri, clientId, clientSecret) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)
        1 * mockConfigs.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.getCurrentUserProfile(fakeResponse.accessToken()) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeUserProfile)

        authService.mapStateUserId.get(state) == fakeUserProfile.id()
        authService.users.containsKey(fakeUserProfile.id())
    }


}
