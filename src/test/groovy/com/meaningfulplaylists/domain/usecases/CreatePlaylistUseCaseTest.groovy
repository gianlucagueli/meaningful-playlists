package com.meaningfulplaylists.domain.usecases

import com.meaningfulplaylists.domain.repositories.MusicProvider
import com.meaningfulplaylists.utils.TestUtils
import spock.lang.Specification

class CreatePlaylistUseCaseTest extends Specification {
    CreatePlaylistUseCase useCase
    MusicProvider mockRepository

    String userId = ""
    String playlistName = ""
    List<String> titleList

    void setup() {
        mockRepository = Mock(MusicProvider)

        useCase = new CreatePlaylistUseCase(mockRepository)
    }

    def "CreatePlaylist - given a valid input, find the right tracks and updates collectedTracks"() {
        given:
        String title1 = "title1"
        String title2 = "title2"
        String title3 = "title3"
        titleList = List.of(title1, title2, title3)

        when:
        useCase.createPlaylist(userId, playlistName, titleList)

        then:
        1 * mockRepository.findByTitle(title1) >> TestUtils.createTrack(title1)
        1 * mockRepository.findByTitle(title2) >> TestUtils.createTrack(title2)
        1 * mockRepository.findByTitle(title3) >> TestUtils.createTrack(title3)
        1 * mockRepository.createPlaylist(_)
    }
}
