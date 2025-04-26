package com.meaningfulplaylists.domain.usecases;

import com.meaningfulplaylists.domain.models.Action;
import com.meaningfulplaylists.domain.repositories.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedirectUseCase {
    private final AuthService authService;

    public RedirectUseCase(AuthService authService) {
        this.authService = authService;
    }

    public String execute(Action actionRequest) {
        log.debug("Generating redirect URL for action {}", actionRequest);
        return this.authService.createRedirectUrl(actionRequest);
    }
}
