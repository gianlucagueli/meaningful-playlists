package com.meaningfulplaylists.domain.usecases

import com.meaningfulplaylists.domain.exceptions.TrackNotFoundException
import com.meaningfulplaylists.domain.repositories.MusicProviderRepository
import com.meaningfulplaylists.utils.TestUtils
import spock.lang.Specification

class CreatePlaylistUseCaseTest extends Specification {
    CreatePlaylistUseCase useCase
    MusicProviderRepository mockRepository

    String userId = ""
    String playlistName = ""
    List<String> titleList

    void setup() {
        mockRepository = Mock(MusicProviderRepository)

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
        1 * mockRepository.findByTitle(title1) >> Optional.of(TestUtils.createTrack(title1))
        1 * mockRepository.findByTitle(title2) >> Optional.of(TestUtils.createTrack(title2))
        1 * mockRepository.findByTitle(title3) >> Optional.of(TestUtils.createTrack(title3))
        useCase.collectedTracks.size() == 3
        useCase.collectedTracks.containsKey(title1)
        useCase.collectedTracks.get(title1) == TestUtils.createTrack(title1)
        useCase.collectedTracks.containsKey(title2)
        useCase.collectedTracks.get(title2) == TestUtils.createTrack(title2)
        useCase.collectedTracks.containsKey(title3)
        useCase.collectedTracks.get(title3) == TestUtils.createTrack(title3)
    }

//    def "CreatePlaylist - given a valid input, throw TrackNotFoundException repo returns an empty optional"() {
//        String title = "song-title"
//        titleList = List.of(title)
//
//        when:
//        useCase.createPlaylist(userId, playlistName, titleList)
//
//        then:
//        1 * mockRepository.findByTitle(title) >> Optional.empty()
//        thrown(TrackNotFoundException)
//    }

    // todo: test exception, test collectedTrakcs checked before calling repo
}
