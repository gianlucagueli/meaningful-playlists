package com.meaningfulplaylists.domain.usecases

import com.meaningfulplaylists.domain.repositories.AuthService
import spock.lang.Specification

class CallbackUseCaseTest extends Specification {
    AuthService mockAuthService
    CallbackUseCase useCase

    void setup() {
        mockAuthService = Mock(AuthService)
        useCase = new CallbackUseCase(mockAuthService)
    }

    def "Execute - should call the handleCallback from the mockAuthService"() {
        given:
        String code = "code-123"
        String state = "state-1234"

        when:
        useCase.execute(code, state)

        then:
        1 * mockAuthService.handleCallback(code, state)
    }
}
