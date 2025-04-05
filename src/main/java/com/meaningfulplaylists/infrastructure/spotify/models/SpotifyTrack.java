package com.meaningfulplaylists.infrastructure.spotify.models;

import java.util.List;

public record SpotifyTrack(
        SpotifyAlbum album,
        List<SpotifyArtist> artists,
        String href,
        String id,
        String name,
        String type,
        String uri
) {}
