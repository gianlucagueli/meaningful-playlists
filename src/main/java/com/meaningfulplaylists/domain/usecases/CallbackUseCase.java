package com.meaningfulplaylists.domain.usecases;

import com.meaningfulplaylists.domain.repositories.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CallbackUseCase {
    AuthService authService;

    CallbackUseCase(AuthService authService) {
        this.authService = authService;
    }

    public void execute(String code, String state) {
        log.debug("Callback received for state {} with code {} ", state, code);
        authService.handleCallback(code, state);
    }
}
