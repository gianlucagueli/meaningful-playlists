package com.meaningfulplaylists.infrastructure.spotify.models;

import lombok.Getter;

@Getter
public enum SpotifySearchType {
    TRACK("track");

    private final String type;

    SpotifySearchType(String type) {
        this.type = type;
    }
}