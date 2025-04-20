package com.meaningfulplaylists.utils

import com.meaningfulplaylists.domain.models.Playlist
import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyAddTracksRequest
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyCreatePlaylistResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifySearchResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTrack
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTracks
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile

class TestUtils {
    // CONFIGS
    static SpotifyProperties createSpotifyProperties(Map<String, String> utilityTracks = Collections.EMPTY_MAP) {
        return new SpotifyProperties(
                "client-id",
                "client-secret",
                "redirect-uri",
                "http://api-base-url.com/",
                "http://account-base-url.com/",
                utilityTracks
        )
    }

    // DOMAIN
    static Track createTrack(String track = "track") {
        return new Track("${track}-id",
                "${track}-name",
                "${track}-uri"
        )
    }

    static Playlist createPlaylist(int limit = 1) {
        List<Track> tracks = limit == 0
                ? Collections.EMPTY_LIST
                : (1..limit).collect { i -> createTrack("track-$i") }

        return new Playlist(
                "playlist-name",
                "playlist-description",
                "playlist-state-associated",
                true,
                tracks
        )
    }

    // SPOTIFY

    static SpotifyTrack createSpotifyTrack(String track = "spotify-track") {
        return new SpotifyTrack(
                "${track}-href",
                "${track}-id",
                "${track}-name",
                "${track}-type",
                "${track}-uri"
        )
    }

    static SpotifyTokenResponse createSpotifyTokenResponse() {
        return new SpotifyTokenResponse(
                "abcdefghi123456789",
                "Bearer",
                3600,
                "123456789abcdefghi",
                "testing",
        )
    }

    static SpotifyUserProfile createSpotifyUserProfile() {
        return new SpotifyUserProfile("user-id-123456")
    }

    static SpotifySearchResponse createSpotifySearchResponse(int limit = 1) {
        return new SpotifySearchResponse(createSpotifyTracks(limit))
    }

    static SpotifyTracks createSpotifyTracks(int limit) {
        List<SpotifyTrack> tracks = limit == 0
                ? Collections.EMPTY_LIST
                : (1..limit).collect { i -> createSpotifyTrack("track-$i") }

        return new SpotifyTracks(tracks);
    }

    static SpotifyCreatePlaylistResponse createSpotifyCreatePlaylistResponse() {
        return new SpotifyCreatePlaylistResponse("playlist-id")
    }

    static SpotifyAddTracksRequest createSpotifyAddTracksRequest(Playlist playlist = null, int position = 0) {
        if (playlist?.tracks()?.isEmpty()) {
            playlist = createPlaylist(5)
        }
        List<String> uris = playlist.tracks().stream().map(Track::uri).toList()

        return new SpotifyAddTracksRequest(uris, position)
    }
}
