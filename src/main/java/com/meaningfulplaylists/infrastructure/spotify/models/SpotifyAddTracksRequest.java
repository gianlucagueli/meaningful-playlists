package com.meaningfulplaylists.infrastructure.spotify.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record SpotifyAddTracksRequest(
        @SerializedName("uris") List<String> uris,
        @SerializedName("position") Integer position
) {}
