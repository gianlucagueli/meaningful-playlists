package com.meaningfulplaylists.infrastructure.spotify.exceptions;

public class SpotifyTrackNotFoundException extends RuntimeException {
    public SpotifyTrackNotFoundException(String name) {
        super("Track not found with name: " + name);
    }
}
