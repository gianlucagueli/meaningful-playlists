package com.meaningfulplaylists.infrastructure.spotify;

import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SpotifyAccount {

    @POST("api/token")
    @FormUrlEncoded
    Call<SpotifyTokenResponse> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirectUri,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret
    );
}
