package com.meaningfulplaylists.infrastructure.spotify.models;

public record SpotifyAuthorizationCodeResponse(
        String code,
        String error,
        String state
) {}
