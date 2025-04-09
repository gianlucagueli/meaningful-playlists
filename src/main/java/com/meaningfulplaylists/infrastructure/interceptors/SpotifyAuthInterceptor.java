package com.meaningfulplaylists.infrastructure.interceptors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Setter
@Component
public class SpotifyAuthInterceptor implements Interceptor {
    private String accessToken;

    //todo: piu avanti spostare su redis e togliere sta roba
    public SpotifyAuthInterceptor(@Value("${spotify.client.authorization}") String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        log.info("Attaching Authorization header to request: {}", original.url());
        Request request = original.newBuilder()
            .header("Authorization", "Bearer " + accessToken)
            .build();

        return chain.proceed(request);
    }
} 