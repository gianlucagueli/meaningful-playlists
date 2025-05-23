package com.meaningfulplaylists.infrastructure.interceptors;

import com.meaningfulplaylists.infrastructure.redis.repository.ClientRedisRepository;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRequestFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Setter
@Component
public class SpotifyAuthInterceptor implements Interceptor {
    private final ClientRedisRepository clientRepository;
    private final SpotifyRequestFactory requestFactory;

    public SpotifyAuthInterceptor(ClientRedisRepository clientRepository,
                                  SpotifyRequestFactory requestFactory) {
        this.clientRepository = clientRepository;
        this.requestFactory = requestFactory;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        log.info("Attaching Authorization header to request: {}", original.url());
        Request request = original.newBuilder()
            .header("Authorization", getAccessToken())
            .build();

        return chain.proceed(request);
    }


    private String getAccessToken() {
        SpotifyTokenResponse clientData = clientRepository.find()
                .orElseThrow(() -> new RuntimeException("Error getting client authorization"));

        return requestFactory.generateAuthHeader(clientData.accessToken());
    }
} 