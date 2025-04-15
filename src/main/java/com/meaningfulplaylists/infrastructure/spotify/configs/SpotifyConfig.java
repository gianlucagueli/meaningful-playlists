package com.meaningfulplaylists.infrastructure.spotify.configs;

import com.meaningfulplaylists.infrastructure.spotify.SpotifyAccount;
import com.meaningfulplaylists.infrastructure.spotify.SpotifyApi;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class SpotifyConfig {
    private final RetrofitUtils retrofitUtils;
    private final SpotifyApi spotifyApi;
    private final SpotifyAccount spotifyAccount;

    public SpotifyConfig(RetrofitUtils retrofitUtils, SpotifyProperties properties) {
        this.retrofitUtils = retrofitUtils;
        this.spotifyAccount = retrofitUtils.buildRetrofit(properties.accountBaseUrl()).create(SpotifyAccount.class);
        this.spotifyApi = retrofitUtils.buildRetrofitWithAuthInterceptor(properties.apiBaseUrl()).create(SpotifyApi.class);
    }
}
