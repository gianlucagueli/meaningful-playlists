package com.meaningfulplaylists.infrastructure.spotify.services;

import com.meaningfulplaylists.domain.models.Action;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SpotifyRedirectUrlFactory {
    private static final String RESPONSE_TYPE = "code";
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int STATE_LENGTH = 10;

    Random random;

    String spotifyAccountBaseUrl;
    String clientId;
    String redirectUrl;

    SpotifyRedirectUrlFactory(@Value("${spotify.account.baseUrl}") String spotifyAccountBaseUrl,
                              @Value("${spotify.client.id}") String clientId,
                              @Value("${spotify.client.redirectUri}") String redirectUrl) {
        random = new Random();
        this.spotifyAccountBaseUrl = spotifyAccountBaseUrl;
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
    }

    public String generateRandomState() {
        StringBuilder sb = new StringBuilder(STATE_LENGTH);

        for (int i = 0; i < STATE_LENGTH; i++) {
            int index = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }

        return sb.toString();
    }

    public String generateRedirectUrl(String state, Action action) {
        return spotifyAccountBaseUrl +
                "authorize?response_type=" + RESPONSE_TYPE +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUrl +
                "&state=" + state +
                buildScope(action);
    }

    // todo: da sistemare, un po brutto
    private String buildScope(Action action) {
        if (Action.CREATE_PLAYLIST == action) {
            return "&scope=playlist-modify-public playlist-modify-private user-read-private user-read-email";
        }

        return "";
    }
}
