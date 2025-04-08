package com.meaningfulplaylists.infrastructure.spotify.configs;

import com.meaningfulplaylists.infrastructure.spotify.SpotifyAccount;
import com.meaningfulplaylists.infrastructure.spotify.SpotifyApi;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class SpotifyConfig {
    private final RetrofitUtils retrofitUtils;
    private final SpotifyApi spotifyApi;
    private final SpotifyAccount spotifyAccount;

    public SpotifyConfig(RetrofitUtils retrofitUtils,
                         @Value("${spotify.api.baseUrl}") String apiBaseUrl,
                         @Value("${spotify.account.baseUrl}") String accountBaseUrl) {
        this.retrofitUtils = retrofitUtils;
        this.spotifyAccount = retrofitUtils.buildRetrofit(accountBaseUrl).create(SpotifyAccount.class);
        this.spotifyApi = retrofitUtils.buildRetrofitWithAuthInterceptor(apiBaseUrl).create(SpotifyApi.class);
    }
}
