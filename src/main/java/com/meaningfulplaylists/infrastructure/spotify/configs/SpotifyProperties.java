package com.meaningfulplaylists.infrastructure.spotify.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("spotify")
public record SpotifyProperties(
        String clientId,
        String clientSecret,
        String clientRedirectUri,
        String apiBaseUrl,
        String accountBaseUrl,
        Map<String, String> utilityTracks
) {}
