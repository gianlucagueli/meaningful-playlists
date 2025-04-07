package com.meaningfulplaylists.domain.models;

import java.util.List;

public record Playlist(
    String name,
    String description,
    String stateAssociated,
    boolean isPublic,
    List<Track> tracks
) {}
