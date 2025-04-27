package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.repositories.AuthService;
import com.meaningfulplaylists.infrastructure.redis.repository.ClientRedisRepository;
import com.meaningfulplaylists.infrastructure.redis.repository.UserRedisRepository;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyConfig;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties;
import com.meaningfulplaylists.infrastructure.spotify.exceptions.SpotifyAuthException;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyTokenResponse;
import com.meaningfulplaylists.infrastructure.spotify.models.SpotifyUserProfile;
import com.meaningfulplaylists.infrastructure.retrofit.RetrofitUtils;
import com.meaningfulplaylists.infrastructure.spotify.utils.SpotifyRequestFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SpotifyAuthService implements AuthService {
    private static final String SPOTIFY_AUTH_CREDENTIALS = "authorization_code";
    private static final String SPOTIFY_CLIENT_CREDENTIALS = "client_credentials";

    private final SpotifyConfig configs;
    private final SpotifyProperties properties;
    private final ClientRedisRepository clientRepository;
    private final UserRedisRepository userRepository;
    private final SpotifyRequestFactory requestFactory;


    SpotifyAuthService(SpotifyConfig configs,
                       SpotifyProperties properties,
                       ClientRedisRepository clientRepository,
                       UserRedisRepository userRepository,
                       SpotifyRequestFactory requestFactory) {
        this.configs = configs;
        this.properties = properties;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.requestFactory = requestFactory;
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
        String state = requestFactory.generateRandomState();
        userRepository.saveState(state, "");

        log.info("Creating url for state: {}", state);

        return requestFactory.generateRedirectUrl(state, action);
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
        return Optional.of(userRepository.findTokenByUserId(userId))
                .map(SpotifyTokenResponse::accessToken)
                .map(requestFactory::generateAuthHeader)
                .orElseThrow();
    }


    private SpotifyTokenResponse exchangeCodeForToken(String code) {
        return executeGetAccessToken(SPOTIFY_AUTH_CREDENTIALS, code, properties.clientRedirectUri())
                .orElseThrow(() -> new SpotifyAuthException("Error retrieving access token for code: " + code));
    }

    @Scheduled(fixedRate = 3000, timeUnit = TimeUnit.SECONDS)
    private void getClientToken() {
        log.info("Getting client token...");
        SpotifyTokenResponse response = executeGetAccessToken(SPOTIFY_CLIENT_CREDENTIALS, null, null)
                .orElseThrow(() -> new SpotifyAuthException("Error retrieving client auth token"));

        clientRepository.save(response);
        log.info("Client token retrieved successfully.");
    }

    private Optional<SpotifyTokenResponse> executeGetAccessToken(String credentials, String code, String redirectUri) {
        Call<SpotifyTokenResponse> call = configs.getSpotifyAccount().getAccessToken(
                credentials,
                code,
                redirectUri,
                properties.clientId(),
                properties.clientSecret()
        );

        return RetrofitUtils.safeExecute(call);
    }

    private String getCurrentUserId(String state, String accessToken) {
        String authHeader = requestFactory.generateAuthHeader(accessToken);

        Call<SpotifyUserProfile> call = configs.getSpotifyApi().getCurrentUserProfile(authHeader);

        return RetrofitUtils.safeExecute(call)
                .map(SpotifyUserProfile::id)
                .orElseThrow(() -> new RuntimeException("Error retrieving user id associated with the state: " + state));
    }
}
