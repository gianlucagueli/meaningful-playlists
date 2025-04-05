package com.meaningfulplaylists.domain.models;

import java.util.List;

public record Playlist(
    String id,
    String name,
    String description,
    boolean isPublic,
    List<Track> tracks
) {} 