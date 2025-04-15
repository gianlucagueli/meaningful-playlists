package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTrack
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyMapper
import com.meaningfulplaylists.utils.TestUtils
import spock.lang.Specification

class SpotifyMapperTest extends Specification {

    def "MapToDomain - should map correctly SpotifyTrack in Track"() {
        given:
        SpotifyTrack spotifyTrack = TestUtils.createSpotifyTrack()

        when:
        Track track = SpotifyMapper.mapToDomain(spotifyTrack)

        then:
        track.id() == spotifyTrack.id()
        track.name() == spotifyTrack.name()
        track.uri() == spotifyTrack.uri()
    }
}
