package com.meaningfulplaylists.infrastructure.spotify.models;

public record SpotifyCreatePlaylistRequest(
    String name,
    String description,
    boolean isPublic
) {}
