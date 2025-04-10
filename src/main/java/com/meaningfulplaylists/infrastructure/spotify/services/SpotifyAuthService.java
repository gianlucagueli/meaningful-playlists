package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.repositories.AuthService;
import com.meaningfulplaylists.infrastructure.redis.repository.ClientRedisRepository;
import com.meaningfulplaylists.infrastructure.redis.repository.UserRedisRepository;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.*;

@Slf4j
@Component
public class SpotifyAuthService implements AuthService {
    private static final String SPOTIFY_AUTH_CREDENTIALS = "authorization_code";
    private static final String SPOTIFY_CLIENT_CREDENTIALS = "client_credentials";

    private final SpotifyConfig configs;
    private final SpotifyRedirectUrlFactory urlFactory;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final UserRedisRepository userRepository;
    private final ClientRedisRepository clientRepository;

    SpotifyAuthService(SpotifyConfig configs,
                       SpotifyRedirectUrlFactory urlFactory,
                       UserRedisRepository userRepository,
                       ClientRedisRepository clientRepository,
                       @Value("${spotify.client.id}") String clientId,
                       @Value("${spotify.client.secret}") String clientSecret,
                       @Value("${spotify.client.redirectUri}") String redirectUri) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.configs = configs;
        this.urlFactory = urlFactory;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;

        this.getClientToken();
    }

    @Override
    public String createRedirectUrl(Action action) {
        String state = urlFactory.generateRandomState();
        userRepository.saveState(state, "");

        log.info("Creating url for state: {}", state);

        return urlFactory.generateRedirectUrl(state, action);
    }

    @Override
    // todo: aggiungere meccanismo di controllo scadenza e recupero (catch eccezione e retry?)
    public void handleCallback(String code, String state) {
        SpotifyTokenResponse response = exchangeCodeForToken(code);
        String userId = getCurrentUserId(state, response.accessToken());

        userRepository.saveState(state, userId);
        userRepository.saveUser(userId, response);
    }

    public String getUserIdFromState(String state) {
        return userRepository.findUserIdByState(state);
    }

    public String getUserAuthorization(String userId) {
        SpotifyTokenResponse tokenResponse = userRepository.findTokenByUserId(userId);

        return "Bearer " + tokenResponse.accessToken();
    }


    private SpotifyTokenResponse exchangeCodeForToken(String code) {
        Call<SpotifyTokenResponse> call = configs.getSpotifyAccount().getAccessToken(
                SPOTIFY_AUTH_CREDENTIALS,
                code,
                redirectUri,
                clientId,
                clientSecret
        );

        return RetrofitUtils.safeExecute(call)
                .orElseThrow(() -> new RuntimeException("Error retrieving access token for code: " + code));
    }

    private void getClientToken() {
        log.info("Getting client token...");
        Call<SpotifyTokenResponse> call = configs.getSpotifyAccount().getAccessToken(
                SPOTIFY_CLIENT_CREDENTIALS,
                null,
                null,
                clientId,
                clientSecret
        );

        SpotifyTokenResponse response = RetrofitUtils.safeExecute(call)
                .orElseThrow(() -> new RuntimeException("Error retrieving client auth token"));

        clientRepository.save(response);
        log.info("Client token retrieved successfully.");
    }

    private String getCurrentUserId(String state, String accessToken) {
        Call<SpotifyUserProfile> call = configs.getSpotifyApi().getCurrentUserProfile("Bearer " + accessToken);

        return RetrofitUtils.safeExecute(call)
                .map(SpotifyUserProfile::id)
                .orElseThrow(() -> new RuntimeException("Error retrieving user id associated with the state: " + state));
    }
}
