package com.meaningfulplaylists.infrastructure.spotify.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record SpotifyProperties(
        @Value("${spotify.client.id}") String clientId,
        @Value("${spotify.client.secret}") String clientSecret,
        @Value("${spotify.client.redirectUri}") String redirectUri,
        @Value("${spotify.api.baseUrl}") String apiBaseUrl,
        @Value("${spotify.account.baseUrl}") String accountBaseUrl
) {}
