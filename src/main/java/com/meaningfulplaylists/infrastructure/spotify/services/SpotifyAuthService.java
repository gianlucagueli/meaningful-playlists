package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.repositories.AuthService;
import com.meaningfulplaylists.infrastructure.redis.repository.ClientRedisRepository;
import com.meaningfulplaylists.infrastructure.redis.repository.UserRedisRepository;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRedirectUrlFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;

@Slf4j
@Component
public class SpotifyAuthService implements AuthService {
    private static final String SPOTIFY_AUTH_CREDENTIALS = "authorization_code";
    private static final String SPOTIFY_CLIENT_CREDENTIALS = "client_credentials";

    private final SpotifyConfig configs;
    private final SpotifyProperties properties;
    private final ClientRedisRepository clientRepository;
    private final UserRedisRepository userRepository;
    private final SpotifyRedirectUrlFactory urlFactory;


    SpotifyAuthService(SpotifyConfig configs,
                       SpotifyProperties properties,
                       ClientRedisRepository clientRepository,
                       UserRedisRepository userRepository,
                       SpotifyRedirectUrlFactory urlFactory) {
        this.configs = configs;
        this.properties = properties;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.urlFactory = urlFactory;
    }

    @PostConstruct
    private void init() {
        clientRepository.find()
                .orElseGet(() -> {
                    getClientToken();
                    return null;
                }) ;
    }

    @Override
    public String createRedirectUrl(Action action) {
        String state = urlFactory.generateRandomState();
        userRepository.saveState(state, "");

        log.info("Creating url for state: {}", state);

        return urlFactory.generateRedirectUrl(state, action);
    }

    @Override
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
                properties.redirectUri(),
                properties.clientId(),
                properties.clientSecret()
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
                properties.clientId(),
                properties.clientSecret()
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
