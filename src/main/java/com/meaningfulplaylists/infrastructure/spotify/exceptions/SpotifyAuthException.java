package com.meaningfulplaylists.infrastructure.spotify.exceptions;

public class SpotifyAuthException extends RuntimeException {
    public SpotifyAuthException(String message) {
        super(message);
    }
}
