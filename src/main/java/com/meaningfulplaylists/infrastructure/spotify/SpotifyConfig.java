package com.meaningfulplaylists.infrastructure.spotify;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Service
public class SpotifyConfig {
    private Retrofit retrofit;
    @Getter private SpotifyApi spotifyApi;

    public SpotifyConfig(@Value("${spotify.api.baseUrl}") String baseUrl) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        spotifyApi = retrofit.create(SpotifyApi.class);
    }

}
