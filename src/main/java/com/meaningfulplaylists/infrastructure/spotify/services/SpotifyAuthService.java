package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.repositories.AuthService;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyMissingStateException;
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
    private final SpotifyRedirectUrlBuilder redirectUrlBuilder;

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    //fixme: rivedere questa parte -> in futuro magari un redis
    Map<String, String> mapStateUserId;
    Map<String, SpotifyTokenResponse> users;

    SpotifyAuthService(SpotifyConfig configs,
                       SpotifyRedirectUrlBuilder redirectUrlBuilder,
                       @Value("${spotify.client.id}") String clientId,
                       @Value("${spotify.client.secret}") String clientSecret,
                       @Value("${spotify.client.redirectUri}") String redirectUri) {
        this.configs = configs;
        this.redirectUrlBuilder = redirectUrlBuilder;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.mapStateUserId = new HashMap<>();
        this.users = new HashMap<>();
    }

    @Override
    public String createRedirectUrl(Action action) {
        String state = redirectUrlBuilder.generateRandomState();
        mapStateUserId.put(state, null); // fixme: meh, rivedere

        log.info("Creating url for state: {}", state);

        return redirectUrlBuilder.generateRedirectUrl(state, action);
    }

    @Override
    // todo: aggiungere meccanismo di controllo scadenza e recupero
    public void handleCallback(String code, String state) {
        if (!mapStateUserId.containsKey(state)) {
            throw new SpotifyMissingStateException(state);
        }

        SpotifyTokenResponse response = exchangeCodeForToken(code);
        String userId = getCurrentUserId(state, response.accessToken());

        mapStateUserId.put(state, userId);
        users.put(userId, response);
    }

    public String getUserIdFromState(String state) {
        return mapStateUserId.get(state);
    }

    public String getUserAuthorization(String userId) {
        SpotifyTokenResponse tokenResponse = users.get(userId);

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

    private String getCurrentUserId(String state, String accessToken) {
        Call<SpotifyUserProfile> call = configs.getSpotifyApi().getCurrentUserProfile("Bearer " + accessToken);

        return RetrofitUtils.safeExecute(call)
                .map(SpotifyUserProfile::id)
                .orElseThrow(() -> new RuntimeException("Error retrieving user id associated with the state: " + state));
    }
}
