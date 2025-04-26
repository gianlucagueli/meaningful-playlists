package com.meaningfulplaylists.infrastructure.spotify.utils

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties
import com.meaningfulplaylists.utils.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

class SpotifyRequestFactoryTest extends Specification {
    SpotifyRequestFactory factory
    SpotifyProperties fakeProperties

    void setup() {
        fakeProperties = TestUtils.createSpotifyProperties()

        factory = new SpotifyRequestFactory(fakeProperties)
    }

    def "GenerateRandomState - should generate a string with length 36"() {
        when:
        String result = factory.generateRandomState()

        then:
        result.length() == 36
    }

    def "GenerateRandomState - should generate random string each time"() {
        given:
        List<String> stateList = (1..100).collect { factory.generateRandomState() }

        when:
        // set doesn't allow duplicates
        Set<String> stateSet = stateList.toSet()

        then:
        stateSet.size() == stateList.size()
    }

    def "GenerateRedirectUrl - should return the redirect url given a valid state and action to perform"() {
        given:
        String state = "state-1234"
        String defaultUrl = "${fakeProperties.accountBaseUrl()}authorize?" +
                "response_type=${SpotifyRequestFactory.RESPONSE_TYPE}" +
                "&client_id=${fakeProperties.clientId()}" +
                "&redirect_uri=${fakeProperties.clientRedirectUri()}" +
                "&state=${state}"

        when:
        String result = factory.generateRedirectUrl(state, action)

        then:
        result == defaultUrl + expectedEnding

        where:
        action                  | expectedEnding
        Action.CREATE_PLAYLIST  | "&scope=user-read-private user-read-email playlist-modify-public playlist-modify-private"
        null                    | "&scope=user-read-private user-read-email"
    }

    def "generateAuthHeader - should correctly format token '#token' to '#expected'"() {
        when:
        String result = factory.generateAuthHeader(token)

        then:
        result == expected

        where:
        token            | expected
        "mytoken"        | "Bearer mytoken"
        "Bearer token"   | "Bearer token"
        "Bearer1"        | "Bearer Bearer1"
        "Bearer "        | "Bearer "
        null             | "Bearer null"
        ""               | "Bearer "
        " "              | "Bearer  "
    }
}
