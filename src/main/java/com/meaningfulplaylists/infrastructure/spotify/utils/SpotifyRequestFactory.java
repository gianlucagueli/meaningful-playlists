package com.meaningfulplaylists.infrastructure.spotify.utils;

import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.infrastructure.spotify.configs.SpotifyProperties;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class SpotifyRequestFactory {
    private static final String RESPONSE_TYPE = "code";
    private static final String BEARER = "Bearer ";

    Random random;

    private final SpotifyProperties properties;

    SpotifyRequestFactory(SpotifyProperties properties) {
        this.properties = properties;
        random = new Random();
    }

    public String generateRandomState() {
        return UUID.randomUUID().toString();
    }

    public String generateRedirectUrl(String state, Action action) {
        return properties.accountBaseUrl() +
                "authorize?response_type=" + RESPONSE_TYPE +
                "&client_id=" + properties.clientId() +
                "&redirect_uri=" + properties.clientRedirectUri() +
                "&state=" + state +
                buildScope(action);
    }

    public String generateAuthHeader(String token) {
        if (token != null && token.startsWith(BEARER)) {
            return token;
        }

        return BEARER + token;
    }

    private String buildScope(Action action) {
        String scope = "&scope=user-read-private user-read-email";

        if (Action.CREATE_PLAYLIST == action) {
            scope += " playlist-modify-public playlist-modify-private";
        }

        return scope;
    }
}
