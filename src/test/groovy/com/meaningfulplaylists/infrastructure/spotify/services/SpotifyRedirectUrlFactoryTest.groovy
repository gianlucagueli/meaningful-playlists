package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRedirectUrlFactory
import spock.lang.Specification

class SpotifyRedirectUrlFactoryTest extends Specification {
    SpotifyRedirectUrlFactory factory

    String baseUrl = "https://base-url.com/"
    String clientId = 1234567890
    String redirectUrl = "https://meaningful-playlist.com/callback"
    String responseType = "code"

    void setup() {
        factory = new SpotifyRedirectUrlFactory(baseUrl, clientId, redirectUrl, responseType)
    }

    def "GenerateRandomState - should generate a string with length 10"() {
        when:
        String result = factory.generateRandomState()

        then:
        result.length() == 10
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
        String defaultUrl = "${baseUrl}authorize?response_type=${responseType}&client_id=${clientId}&redirect_uri=${redirectUrl}&state=${state}"

        when:
        String result = factory.generateRedirectUrl(state, action)

        then:
        result == defaultUrl + expectedEnding

        where:
        action                  | expectedEnding
        Action.CREATE_PLAYLIST  | "&scope=playlist-modify-public playlist-modify-private user-read-private user-read-email"
        null                    | ""
    }
}
