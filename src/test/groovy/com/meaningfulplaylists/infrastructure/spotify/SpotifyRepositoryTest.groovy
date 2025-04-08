package com.meaningfulplaylists.infrastructure.spotify

import com.meaningfulplaylists.domain.models.Playlist
import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyTrackNotFoundException
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyAddTracksRequest
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyCreatePlaylistResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifySearchResponse
import com.meaningfulplaylists.infrastructure.spotify.services.SpotifyAuthService
import com.meaningfulplaylists.utils.TestUtils
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

class SpotifyRepositoryTest extends Specification {
    SpotifyConfig spotifyConfig;
    SpotifyApi mockSpotifyApp

    Call mockCall
    Request mockRequest

    SpotifyAuthService authService;

    SpotifyRepository repository

    void setup() {
        spotifyConfig = Mock(SpotifyConfig)
        authService = Mock(SpotifyAuthService)

        repository = new SpotifyRepository(spotifyConfig, authService)

        mockSpotifyApp = Mock(SpotifyApi)
        mockCall = Mock(Call)
        mockRequest = GroovyMock(Request)
    }

    def "FindByTitle - should return the Track found"() {
        given:
        SpotifySearchResponse fakeResponse = TestUtils.createSpotifySearchResponse()
        String title = "track-title"

        when:
        Track result = repository.findByTitle(title)

        then:
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.searchTracks(title, "track", 1) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)

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
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.searchTracks(title, "track", 1) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)
        1 * mockCall.request() >> mockRequest

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
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.addTracksToPlaylist(fakeAuthToken, playlistResponse.id(), request) >> mockCall
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
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(badResponse)
        1 * mockCall.request() >> mockRequest
        0 * spotifyConfig.getSpotifyApi()
        0 * mockSpotifyApp.addTracksToPlaylist(_, _, _)
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
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        0 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        0 * mockSpotifyApp.addTracksToPlaylist(_, _, _) >> mockCall
        0 * mockCall.execute() >> Response.success(Void)
    }

    def "CreatePlaylist - should correctly calls Spotify to create the playlist and then add the tracks"() {
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
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApp
        1 * mockSpotifyApp.addTracksToPlaylist(fakeAuthToken, playlistResponse.id(), request) >> mockCall
        1 * mockCall.execute() >> Response.success(badResponse)
        1 * mockCall.request() >> mockRequest

        and:
        thrown(RuntimeException)
    }
}
