package com.meaningfulplaylists.domain.usecases

import com.meaningfulplaylists.domain.models.Playlist
import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.domain.repositories.MusicProvider
import com.meaningfulplaylists.utils.TestUtils
import spock.lang.Specification

class CreatePlaylistUseCaseTest extends Specification {
    CreatePlaylistUseCase useCase
    MusicProvider mockRepository

    String userId = "user-id"
    String playlistName = "playlist-name"

    void setup() {
        mockRepository = Mock(MusicProvider)

        useCase = new CreatePlaylistUseCase(mockRepository)
    }

    def "CreatePlaylist - given a valid input, find the right tracks and updates collectedTracks"() {
        given:
        String title1 = "title1"
        String title2 = "title2"
        String title3 = "title3"
        List<String> titleList = List.of(title1, title2, title3)
        List<Track> trackList = List.of(
                TestUtils.createTrack(title1),
                TestUtils.createTrack(title2),
                TestUtils.createTrack(title3),
        )
        Playlist expectedPlaylist = new Playlist(playlistName, useCase.DEFAULT_DESCRIPTION, userId, true, trackList)

        when:
        useCase.createPlaylist(userId, playlistName, titleList)

        then:
        1 * mockRepository.findTracks(titleList) >> trackList
        1 * mockRepository.createPlaylist(expectedPlaylist)

        and:
        0 * _._
    }
}
