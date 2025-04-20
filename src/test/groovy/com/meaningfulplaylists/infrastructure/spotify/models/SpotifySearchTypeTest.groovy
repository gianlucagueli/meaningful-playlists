package com.meaningfulplaylists.infrastructure.spotify.models

import spock.lang.Specification
import spock.lang.Unroll

class SpotifySearchTypeTest extends Specification {
    @Unroll
    def "getType - '#expected' for SpotifySearchType.#enumName"() {
        expect:
        enumValue.getType() == expected

        where:
        enumValue               | enumName          | expected
        SpotifySearchType.TRACK | "TRACK"           | "track"
    }
}
