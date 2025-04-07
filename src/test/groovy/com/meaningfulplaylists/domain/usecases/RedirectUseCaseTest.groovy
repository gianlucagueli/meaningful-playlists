package com.meaningfulplaylists.domain.usecases

import com.meaningfulplaylists.domain.models.Action
import com.meaningfulplaylists.domain.repositories.AuthService
import spock.lang.Specification

class RedirectUseCaseTest extends Specification {
    AuthService mockAuthService
    RedirectUseCase useCase

    def setup() {
        mockAuthService = Mock(AuthService)
        useCase = new RedirectUseCase(mockAuthService)
    }
    def "BuildRedirectUrl - should call createRedirectUrl from the authService and return its result"() {
        given:
        Action inputAction = Action.CREATE_PLAYLIST
        String expected = "expected-result"

        when:
        String result = useCase.execute(inputAction)

        then:
        1 * mockAuthService.createRedirectUrl(inputAction) >> expected
        result == expected
    }
}
