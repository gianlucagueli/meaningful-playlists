package com.meaningfulplaylists.domain.exceptions;

public class TrackNotFoundException extends RuntimeException {
    public TrackNotFoundException(String name) {
        super("Track not found with name: " + name);
    }
}
