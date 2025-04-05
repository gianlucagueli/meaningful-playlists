package com.meaningfulplaylists.infrastructure.spotify.models;

import java.util.List;

public record SpotifyAlbum(
    String id,
    String name,
    String uri,
    List<SpotifyArtist> artists
) {} 