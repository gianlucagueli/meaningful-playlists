package com.meaningfulplaylists.infrastructure.spotify.utils;

import com.meaningfulplaylists.domain.models.Track;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTrack;

public class SpotifyMapper {
    public static Track mapToDomain(SpotifyTrack spotifyTrack) {
        return new Track(
                spotifyTrack.id(),
                spotifyTrack.name(),
                spotifyTrack.uri()
        );
    }

}
