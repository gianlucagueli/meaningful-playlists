package com.meaningfulplaylists.domain.repositories;

import com.meaningfulplaylists.domain.models.Action;

public interface AuthService {
    String createRedirectUrl(Action action);
    void handleCallback(String code, String state);
}
