package com.meaningfulplaylists.domain.models;

import java.util.List;

public record Playlist(
    String id,
    String name,
    String description,
    Owner owner,
    boolean isPublic,
    List<Track> tracks
) {
    public Playlist(String name, String description, Owner owner, boolean isPublic, List<Track> tracks) {
        this(null, name, description, owner, isPublic, tracks);
    }
}