package com.meaningfulplaylists.infrastructure.spotify.exceptions;

public class SpotifyMissingStateException extends RuntimeException {
    public SpotifyMissingStateException(String state) {
        super("Error handling callback. State " + state + " not found.");
    }
}
