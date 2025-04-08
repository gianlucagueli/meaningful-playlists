package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Action
import spock.lang.Specification

class SpotifyRedirectUrlBuilderTest extends Specification {
    SpotifyRedirectUrlBuilder builder

    String baseUrl = "https://base-url.com/"
    String clientId = 1234567890
    String redirectUrl = "https://meaningful-playlist.com/callback"

    void setup() {
        builder = new SpotifyRedirectUrlBuilder(baseUrl, clientId, redirectUrl)
    }

    def "GenerateRandomState - should generate a string with length 10"() {
        when:
        String result = builder.generateRandomState()

        then:
        result.length() == 10
    }

    def "GenerateRandomState - should generate random string each time"() {
        given:
        List<String> stateList = (1..100).collect { builder.generateRandomState() }

        when:
        // set doesn't allow duplicates
        Set<String> stateSet = stateList.toSet()

        then:
        stateSet.size() == stateList.size()
    }

    def "GenerateRedirectUrl - should return the redirect url given a valid state and action to perform"() {
        given:
        String state = "state-1234"

        when:
        String result = builder.generateRedirectUrl(state, action)

        then:
        result.startsWith("${baseUrl}authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUrl}&state=${state}")
        result.endsWith(expectedEnding)

        where:
        action                  | expectedEnding
        Action.CREATE_PLAYLIST  | "&scope=playlist-modify-public playlist-modify-private user-read-private user-read-email"
        null                    | ""
    }
}
