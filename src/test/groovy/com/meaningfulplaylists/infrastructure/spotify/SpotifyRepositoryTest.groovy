package com.meaningfulplaylists.infrastructure.spotify

import com.meaningfulplaylists.domain.models.Playlist
import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.redis.repository.TracksRedisRepository
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyTrackNotFoundException
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyAddTracksRequest
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyCreatePlaylistResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifySearchResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifySearchType
import com.meaningfulplaylists.infrastructure.spotify.services.SpotifyAuthService
import com.meaningfulplaylists.infrastructure.spotify.services.SpotifyMusicService
import com.meaningfulplaylists.utils.TestUtils
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

class SpotifyRepositoryTest extends Specification {
    SpotifyConfig spotifyConfig;
    SpotifyAuthService authService;
    TracksRedisRepository mockTrackRepository

    SpotifyApi mockSpotifyApi
    Call mockCall
    Request mockRequest

    SpotifyMusicService repository

    void setup() {
        spotifyConfig = Mock(SpotifyConfig)
        authService = Mock(SpotifyAuthService)
        mockTrackRepository = Mock(TracksRedisRepository)

        repository = new SpotifyMusicService(spotifyConfig, authService, mockTrackRepository)

        mockSpotifyApi = Mock(SpotifyApi)
        mockCall = Mock(Call)
        mockRequest = GroovyMock(Request)
    }

    def "FindByTitle - should avoid to call spotify if the track is already stored"() {
        given:
        String title = "track-title"
        Track fakeTrack = TestUtils.createTrack(title)

        when:
        Track result = repository.findByTitle(title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.of(fakeTrack)
        0 * _._

        and:
        result.id() == fakeTrack.id()
        result.name() == fakeTrack.name()
        result.uri() == fakeTrack.uri()
    }

    def "FindByTitle - should search the repository and call spotify if nothing is found"() {
        given:
        SpotifySearchResponse fakeResponse = TestUtils.createSpotifySearchResponse()
        String title = "track-title"

        when:
        Track result = repository.findByTitle(title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.empty()
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.searchTracks(title, SpotifySearchType.TRACK.getType(), 10) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)

        and:
        result.id() == fakeResponse.tracks().items().get(0).id()
        result.name() == fakeResponse.tracks().items().get(0).name()
        result.uri() == fakeResponse.tracks().items().get(0).uri()
    }


    def "FindByTitle - should throw SpotifyTrackNotFoundException if no track is found"() {
        given:
        SpotifySearchResponse fakeResponse = null
        String title = "track-title"

        when:
        Track result = repository.findByTitle(title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.empty()
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.searchTracks(title, SpotifySearchType.TRACK.getType(), 10) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)

        and:
        thrown(SpotifyTrackNotFoundException)
    }

    def "CreatePlaylist - should correctly calls Spotify to create the playlist and then add the tracks"() {
        given:
        Playlist playlist = TestUtils.createPlaylist(5)
        SpotifyCreatePlaylistResponse playlistResponse = TestUtils.createSpotifyCreatePlaylistResponse()
        SpotifyAddTracksRequest request = TestUtils.createSpotifyAddTracksRequest(playlist)
        String fakeUserId = "fake-user-id"
        String fakeAuthToken = "fake-auth-token"

        when:
        repository.createPlaylist(playlist)

        then:
        1 * authService.getUserIdFromState(playlist.stateAssociated()) >> fakeUserId
        1 * authService.getUserAuthorization(fakeUserId) >> fakeAuthToken
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.addTracksToPlaylist(fakeAuthToken, playlistResponse.id(), request) >> mockCall
        1 * mockCall.execute() >> Response.success(Void)
    }

    def "CreatePlaylist - should throw exception and stop execution if it fails to create the playlist"() {
        given:
        Playlist playlist = TestUtils.createPlaylist(0)
        playlist.tracks().removeAll()

        SpotifyCreatePlaylistResponse badResponse = null
        SpotifyAddTracksRequest request = TestUtils.createSpotifyAddTracksRequest(playlist)
        String fakeUserId = "fake-user-id"
        String fakeAuthToken = "fake-auth-token"

        when:
        repository.createPlaylist(playlist)

        then:
        1 * authService.getUserIdFromState(playlist.stateAssociated()) >> fakeUserId
        1 * authService.getUserAuthorization(fakeUserId) >> fakeAuthToken
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(badResponse)
        0 * spotifyConfig.getSpotifyApi()
        0 * mockSpotifyApi.addTracksToPlaylist(_, _, _)
        0 * mockCall.execute()

        and:
        thrown(RuntimeException)
    }

    def "CreatePlaylist - should correctly calls Spotify to create the playlist and avoid unnecessary calls if no track is present"() {
        given:
        Playlist playlist = TestUtils.createPlaylist(0)
        playlist.tracks().removeAll()

        SpotifyCreatePlaylistResponse playlistResponse = TestUtils.createSpotifyCreatePlaylistResponse()
        SpotifyAddTracksRequest request = TestUtils.createSpotifyAddTracksRequest(playlist)
        String fakeUserId = "fake-user-id"
        String fakeAuthToken = "fake-auth-token"

        when:
        repository.createPlaylist(playlist)

        then:
        1 * authService.getUserIdFromState(playlist.stateAssociated()) >> fakeUserId
        1 * authService.getUserAuthorization(fakeUserId) >> fakeAuthToken
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        0 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        0 * mockSpotifyApi.addTracksToPlaylist(_, _, _) >> mockCall
        0 * mockCall.execute() >> Response.success(Void)
    }

    def "CreatePlaylist - should throw RuntimeExeption if it fails to add the tracks to the playlist just created"() {
        given:
        Playlist playlist = TestUtils.createPlaylist(5)
        SpotifyCreatePlaylistResponse playlistResponse = TestUtils.createSpotifyCreatePlaylistResponse()
        SpotifyAddTracksRequest request = TestUtils.createSpotifyAddTracksRequest(playlist)
        String fakeUserId = "fake-user-id"
        String fakeAuthToken = "fake-auth-token"
        Object badResponse = null

        when:
        repository.createPlaylist(playlist)

        then:
        1 * authService.getUserIdFromState(playlist.stateAssociated()) >> fakeUserId
        1 * authService.getUserAuthorization(fakeUserId) >> fakeAuthToken
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.addTracksToPlaylist(fakeAuthToken, playlistResponse.id(), request) >> mockCall
        1 * mockCall.execute() >> Response.success(badResponse)

        and:
        thrown(RuntimeException)
    }
}
