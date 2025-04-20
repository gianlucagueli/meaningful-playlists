package com.meaningfulplaylists.infrastructure.spotify.utils

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties
import com.meaningfulplaylists.utils.TestUtils
import org.apache.logging.log4j.util.Strings
import spock.lang.Specification

class SpotifyRedirectUrlFactoryTest extends Specification {
    SpotifyRedirectUrlFactory factory
    SpotifyProperties fakeProperties

    void setup() {
        fakeProperties = TestUtils.createSpotifyProperties()

        factory = new SpotifyRedirectUrlFactory(fakeProperties)
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
        String defaultUrl = "${fakeProperties.accountBaseUrl()}authorize?" +
                "response_type=${SpotifyRedirectUrlFactory.RESPONSE_TYPE}" +
                "&client_id=${fakeProperties.clientId()}" +
                "&redirect_uri=${fakeProperties.clientRedirectUri()}" +
                "&state=${state}"

        when:
        String result = factory.generateRedirectUrl(state, action)

        then:
        result == defaultUrl + expectedEnding

        where:
        action                  | expectedEnding
        Action.CREATE_PLAYLIST  | "&scope=playlist-modify-public playlist-modify-private user-read-private user-read-email"
        null                    | Strings.EMPTY
    }
}
