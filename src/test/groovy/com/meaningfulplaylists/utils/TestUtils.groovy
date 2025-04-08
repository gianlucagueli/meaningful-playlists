package com.meaningfulplaylists.utils

import com.meaningfulplaylists.domain.models.Track
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile

class TestUtils {
    static Track createTrack(String title) {
        return new Track("id-${title}",
                title,
                "artist-${title}",
                "uri-${title}"
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
}
