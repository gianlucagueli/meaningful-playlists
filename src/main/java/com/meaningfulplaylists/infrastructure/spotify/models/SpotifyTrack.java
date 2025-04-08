package com.meaningfulplaylists.infrastructure.spotify.models;

public record SpotifyTrack(
        String href,
        String id,
        String name,
        String type,
        String uri
) {}
