package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.infrastructure.spotify.SpotifyAccount
import com.meaningfulplaylists.infrastructure.spotify.SpotifyApi
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyMissingStateException
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRedirectUrlFactory
import com.meaningfulplaylists.utils.TestUtils
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

class SpotifyAuthServiceTest extends Specification {
    SpotifyConfig mockConfigs;
    SpotifyAccount mockSpotifyAccount
    SpotifyApi mockSpotifyApi

    Call mockCall
    SpotifyRedirectUrlFactory urlFactory;

    SpotifyAuthService authService

    String clientId = "client-id"
    String clientSecret = "client-secret"
    String redirectUri = "redirect-uri"

    void setup() {
        mockConfigs = Mock(SpotifyConfig)

        mockSpotifyAccount = Mock(SpotifyAccount)
        mockSpotifyApi = Mock(SpotifyApi)
        mockCall = Mock(Call)

        urlFactory = Mock(SpotifyRedirectUrlFactory)

        authService = new SpotifyAuthService(mockConfigs, urlFactory, clientId, clientSecret, redirectUri)
    }

    def "CreateRedirectUrl - should generate a redirect URL and map the state to a user session"() {
        given:
        Action action = Action.CREATE_PLAYLIST
        String randomState = "12345abcde"
        String generatedUrl = "generated-redirect-url"

        when:
        String result = authService.createRedirectUrl(action)

        then:
        1 * urlFactory.generateRandomState() >> randomState
        1 * urlFactory.generateRedirectUrl(randomState, action) >> generatedUrl

        and:
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
        1 * mockSpotifyApi.getCurrentUserProfile("Bearer " + fakeResponse.accessToken()) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeUserProfile)

        and:
        authService.mapStateUserId.get(state) == fakeUserProfile.id()
        authService.users.containsKey(fakeUserProfile.id())
    }

    def "HandleCallback - should throw an exception if no state is found"() {
        given:
        String missingState = "12345-abcd"

        when:
        authService.handleCallback(_ as String, missingState)

        then:
        thrown(SpotifyMissingStateException)
    }

    def "getUserIdFromState - should return the userId associated the given state"() {
        given:
        String state = "state-user-id"
        String userId = "user-id"
        authService.mapStateUserId.put(state, userId)

        when:
        String result = authService.getUserIdFromState(state)

        then:
        result == userId
    }

    def "getUserAuthorization - should return the Authorization header for the given userId"() {
        given:
        SpotifyTokenResponse fakeResponse = TestUtils.createSpotifyTokenResponse()
        String userId = "user-id"
        authService.users.put(userId, fakeResponse)

        when:
        String result = authService.getUserAuthorization(userId)

        then:
        result == "Bearer ${fakeResponse.accessToken()}"
    }
}
