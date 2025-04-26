package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.infrastructure.redis.repository.ClientRedisRepository
import com.meaningfulplaylists.infrastructure.redis.repository.UserRedisRepository
import com.meaningfulplaylists.infrastructure.spotify.SpotifyAccount
import com.meaningfulplaylists.infrastructure.spotify.SpotifyApi
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRequestFactory
import com.meaningfulplaylists.utils.TestUtils
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

class SpotifyAuthServiceTest extends Specification {
    SpotifyConfig mockConfigs;
    SpotifyAccount mockSpotifyAccount
    SpotifyApi mockSpotifyApi
    SpotifyProperties fakeProperties
    ClientRedisRepository mockClientRepository
    UserRedisRepository mockUserRepository

    SpotifyRequestFactory mockRequestFactory;
    Call mockCall

    SpotifyAuthService authService

    void setup() {
        mockConfigs = Mock(SpotifyConfig)
        mockRequestFactory = Mock(SpotifyRequestFactory)
        fakeProperties = TestUtils.createSpotifyProperties()
        mockClientRepository = Mock(ClientRedisRepository)
        mockUserRepository = Mock(UserRedisRepository)
        mockSpotifyAccount = Mock(SpotifyAccount)
        mockSpotifyApi = Mock(SpotifyApi)
        mockCall = Mock(Call)

        authService = new SpotifyAuthService(mockConfigs, fakeProperties, mockClientRepository, mockUserRepository, mockRequestFactory)
    }

    def "init - should avoid calling spotify if the token is already stored"() {
        given:
        SpotifyTokenResponse token = TestUtils.createSpotifyTokenResponse()

        when:
        authService.init()

        then:
        1 * mockClientRepository.find() >> Optional.of(token)

        and:
        0 * _._
    }

    def "init - should call spotify to retrieve client data if not present in the repository"() {
        given:
        SpotifyTokenResponse fakeResponse = TestUtils.createSpotifyTokenResponse()

        when:
        authService.init()

        then:
        1 * mockClientRepository.find() >> Optional.empty()
        1 * mockConfigs.getSpotifyAccount() >> mockSpotifyAccount
        1 * mockSpotifyAccount.getAccessToken(
                authService.SPOTIFY_CLIENT_CREDENTIALS,
                null,
                null,
                fakeProperties.clientId(),
                fakeProperties.clientSecret()
        ) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)
        1 * mockClientRepository.save(fakeResponse)

        and:
        0 * _._
    }

    def "CreateRedirectUrl - should generate a redirect URL and map the state to a user session"() {
        given:
        Action action = Action.CREATE_PLAYLIST
        String randomState = "12345abcde"
        String generatedUrl = "generated-redirect-url"

        when:
        String result = authService.createRedirectUrl(action)

        then:
        1 * mockRequestFactory.generateRandomState() >> randomState
        1 * mockUserRepository.saveState(randomState, "")
        1 * mockRequestFactory.generateRedirectUrl(randomState, action) >> generatedUrl

        and:
        generatedUrl == result
    }

    def "HandleCallback - should correctly handle the callback from spotify"() {
        given:
        SpotifyTokenResponse fakeResponse = TestUtils.createSpotifyTokenResponse()
        SpotifyUserProfile fakeUserProfile = TestUtils.createSpotifyUserProfile()
        String code = "code-1234-567-890"
        String state = "state-1234"

        when:
        authService.handleCallback(code, state)

        then:
        1 * mockConfigs.getSpotifyAccount() >> mockSpotifyAccount
        1 * mockSpotifyAccount.getAccessToken(_,
                code,
                fakeProperties.clientRedirectUri(),
                fakeProperties.clientId(),
                fakeProperties.clientSecret()
        ) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)
        1 * mockRequestFactory.generateAuthHeader(fakeResponse.accessToken()) >> "Bearer ${fakeResponse.accessToken()}"
        1 * mockConfigs.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.getCurrentUserProfile("Bearer " + fakeResponse.accessToken()) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeUserProfile)
        1 * mockUserRepository.saveState(state, fakeUserProfile.id())
        1 * mockUserRepository.saveUser(fakeUserProfile.id(), fakeResponse)

        and:
        0 * _._
    }

    def "getUserIdFromState - should return the userId associated the given state"() {
        given:
        String state = "state-user-id"
        String userId = "user-id"

        when:
        String result = authService.getUserIdFromState(state)

        then:
        1 * mockUserRepository.findUserIdByState(state) >> userId

        and:
        result == userId
        0 * _._
    }

    def "getUserAuthorization - should return the Authorization header for the given userId"() {
        given:
        SpotifyTokenResponse fakeResponse = TestUtils.createSpotifyTokenResponse()
        String userId = "user-id"

        when:
        String result = authService.getUserAuthorization(userId)

        then:
        1 * mockUserRepository.findTokenByUserId(userId) >> fakeResponse
        1 * mockRequestFactory.generateAuthHeader(fakeResponse.accessToken()) >> "Bearer ${fakeResponse.accessToken()}"

        and:
        result == "Bearer ${fakeResponse.accessToken()}"
    }
}
