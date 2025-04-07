package com.meaningfulplaylists.domain.repositories;

import com.meaningfulplaylists.domain.models.Action;

public interface AuthService {
    String createRedirectUrl(Action action); //todo: se si vuole estendere ad altre funzioni aggiungere le opzioni con gli scope
    void handleCallback(String code, String state);
}
