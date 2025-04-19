package com.meaningfulplaylists.infrastructure.spotify.models

import spock.lang.Specification

class SpotifySearchTypeTest extends Specification {
    def "getType - should return the correct type for each enum"() {
        given:
        SpotifySearchType enumValue = currentEnum

        when:
        String result = enumValue.getType()

        then:
        result == expected

        where:
        currentEnum                 | expected
        SpotifySearchType.TRACK     | "track"
    }
}
