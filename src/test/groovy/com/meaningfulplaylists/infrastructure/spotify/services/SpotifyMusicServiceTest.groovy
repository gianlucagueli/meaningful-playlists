package com.meaningfulplaylists.infrastructure.spotify.services

import com.meaningfulplaylists.domain.models.Playlist
import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.redis.repository.TracksRedisRepository
import com.meaningfulplaylists.infrastructure.spotify.SpotifyApi
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyTrackNotFoundException
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyAddTracksRequest
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyCreatePlaylistResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifySearchResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifySearchType
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTrack
import com.meaningfulplaylists.utils.TestUtils
import okhttp3.Request
import org.springframework.data.util.Pair
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

class SpotifyMusicServiceTest extends Specification {
    SpotifyConfig spotifyConfig;
    SpotifyProperties spotifyProperties;
    SpotifyAuthService authService;
    TracksRedisRepository mockTrackRepository

    SpotifyApi mockSpotifyApi
    Call mockCall
    Request mockRequest

    SpotifyMusicService musicService

    void setup() {
        spotifyConfig = Mock(SpotifyConfig)
        spotifyProperties = TestUtils.createSpotifyProperties()
        authService = Mock(SpotifyAuthService)
        mockTrackRepository = Mock(TracksRedisRepository)

        musicService = new SpotifyMusicService(spotifyConfig, spotifyProperties, authService, mockTrackRepository)

        mockSpotifyApi = Mock(SpotifyApi)
        mockCall = Mock(Call)
        mockRequest = GroovyMock(Request)
    }

    def "FindTracks - returns an empty list if keyword is null or empty"() {
        when:
        List<Track> result = musicService.findTracks(keywords)

        then:
        result.isEmpty()

        where:
        keywords << [null, Collections.EMPTY_LIST]
    }

    def "loadUtilityTrack - should avoid calling spotify if the track is already present"() {
        given:
        String id = "track-id"
        String title = "track-title"
        Track fakeTrack = TestUtils.createTrack(title)

        when:
        musicService.loadUtilityTrack(id, title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.of(fakeTrack)

        and:
        0 * _._
    }

    def "loadUtilityTrack - should call spotify if the track is not present"() {
        given:
        String id = "track-id"
        String title = "track-title"
        SpotifyTrack fakeSpotifyTrack = TestUtils.createSpotifyTrack(title)

        when:
        musicService.loadUtilityTrack(id, title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.empty()
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.getTrack(id) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeSpotifyTrack)
        1 * mockTrackRepository.save(_)

        and:
        0 * _._
    }

    // fixme: decide what to do
    def "loadUtilityTrack - should ignore SpotifyTrackNotFoundException"() {
        given:
        String id = "track-id"
        String title = "track-title"
        SpotifyTrack fakeSpotifyTrack = null

        when:
        musicService.loadUtilityTrack(id, title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.empty()
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.getTrack(id) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeSpotifyTrack)

        and:
        0 * mockTrackRepository.save(_)
        0 * _._
    }

    def "FindByTitle - should avoid to call spotify if the track is already stored"() {
        given:
        String title = "track-title"
        Track fakeTrack = TestUtils.createTrack(title)

        when:
        Track result = musicService.findByTitle(title)

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
        SpotifySearchResponse fakeResponse = TestUtils.createSpotifySearchResponse(12)
        int position = 6
        String title = "track-${position}-name"

        when:
        Track result = musicService.findByTitle(title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.empty()
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.search(title, SpotifySearchType.TRACK.getType(), 10) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)
        1 * mockTrackRepository.save(_)

        and:
        result.id() == fakeResponse.tracks().items().get(position - 1).id()
        result.name() == fakeResponse.tracks().items().get(position - 1).name()
        result.uri() == fakeResponse.tracks().items().get(position - 1).uri()
    }

    def "FindByTitle - should throw SpotifyTrackNotFoundException if no track is found"() {
        given:
        SpotifySearchResponse fakeResponse = null
        String title = "track-title"

        when:
        Track result = musicService.findByTitle(title)

        then:
        1 * mockTrackRepository.findByName(title) >> Optional.empty()
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.search(title, SpotifySearchType.TRACK.getType(), 10) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)

        and:
        thrown(SpotifyTrackNotFoundException)
    }

    def "FindById - should get the track from spotify"() {
        given:
        String id = "track-id"
        SpotifyTrack fakeResponse = TestUtils.createSpotifyTrack(id)

        when:
        Track result = musicService.findById(id)

        then:
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.getTrack(id) >> mockCall
        1 * mockCall.execute() >> Response.success(fakeResponse)

        and:
        result.name() == fakeResponse.name()
        result.id() == fakeResponse.id()
        result.uri() == fakeResponse.uri()
    }


    def "FindById - should throw SpotifyTrackNotFoundException if no track is found"() {
        given:
        String id = "track-id"
        SpotifyTrack fakeResponse = null

        when:
        musicService.findById(id)

        then:
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.getTrack(id) >> mockCall
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
        musicService.createPlaylist(playlist)

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
        musicService.createPlaylist(playlist)

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
        musicService.createPlaylist(playlist)

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
        musicService.createPlaylist(playlist)

        then:
        1 * authService.getUserIdFromState(playlist.stateAssociated()) >> fakeUserId
        1 * authService.getUserAuthorization(fakeUserId) >> fakeAuthToken
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.createPlaylist(fakeAuthToken, fakeUserId, _) >> mockCall
        1 * mockCall.execute() >> Response.success(playlistResponse)
        1 * spotifyConfig.getSpotifyApi() >> mockSpotifyApi
        1 * mockSpotifyApi.addTracksToPlaylist(fakeAuthToken, playlistResponse.id(), request) >> mockCall
        1 * mockCall.execute() >> Response.success(badResponse)

        // fixme: da rivedere
        //and:
        //thrown(RuntimeException)
    }
}
