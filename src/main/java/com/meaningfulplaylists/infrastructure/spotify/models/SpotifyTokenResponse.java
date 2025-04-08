package com.meaningfulplaylists.infrastructure.spotify.models;

import com.google.gson.annotations.SerializedName;

public record SpotifyTokenResponse(
        @SerializedName("access_token") String accessToken,
        @SerializedName("token_type") String tokenType,
        @SerializedName("expires_in") int expiresIn,
        @SerializedName("refresh_token") String refreshToken,
        @SerializedName("scope") String scope
) {}