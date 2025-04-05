package com.meaningfulplaylists.infrastructure.spotify.models;

import java.util.List;

public record SpotifyPlaylist(
    String id,
    String name,
    String description,
    boolean isPublic,
    String ownerId,
    String snapshotId,
    int totalTracks,
    String imageUrl,
    List<SpotifyTrack> tracks
) {} 