package by.dzarembo.authservice.controller;

import by.dzarembo.authservice.dto.CreateCredentialRequest;
import by.dzarembo.authservice.dto.CredentialResponse;
import by.dzarembo.authservice.dto.LoginRequest;
import by.dzarembo.authservice.dto.RefreshTokenRequest;
import by.dzarembo.authservice.dto.TokenPairResponse;
import by.dzarembo.authservice.dto.ValidationTokenRequest;
import by.dzarembo.authservice.dto.ValidationTokenResponse;
import by.dzarembo.authservice.entity.Role;
import by.dzarembo.authservice.service.AuthService;
import by.dzarembo.authservice.service.CredentialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private AuthController authController;

    @Test
    void methods_shouldReturnExpectedResponses() {
        CreateCredentialRequest createRequest = CreateCredentialRequest.builder()
                .userId(7L)
                .login("ivan")
                .password("secret123")
                .role(Role.ADMIN)
                .build();
        CredentialResponse credentialResponse = CredentialResponse.builder()
                .id(1L)
                .userId(7L)
                .login("ivan")
                .role(Role.ADMIN)
                .active(true)
                .createdAt(Instant.parse("2026-03-26T10:15:30Z"))
                .updatedAt(Instant.parse("2026-03-26T10:15:30Z"))
                .build();
        LoginRequest loginRequest = new LoginRequest("ivan", "secret123");
        TokenPairResponse loginResponse = new TokenPairResponse("access-token", "refresh-token");
        ValidationTokenRequest validationTokenRequest = new ValidationTokenRequest("access-token");
        ValidationTokenResponse validationTokenResponse = new ValidationTokenResponse(true, 7L, "ADMIN", "access");
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh-token");
        TokenPairResponse refreshResponse = new TokenPairResponse("new-access-token", "new-refresh-token");

        when(credentialService.create(createRequest)).thenReturn(credentialResponse);
        when(authService.login(loginRequest)).thenReturn(loginResponse);
        when(authService.validate(validationTokenRequest)).thenReturn(validationTokenResponse);
        when(authService.refreshToken(refreshTokenRequest)).thenReturn(refreshResponse);

        var createResult = authController.create(createRequest);
        var loginResult = authController.login(loginRequest);
        var validateResult = authController.validate(validationTokenRequest);
        var refreshResult = authController.refresh(refreshTokenRequest);

        assertThat(createResult.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResult.getBody()).isSameAs(credentialResponse);
        assertThat(loginResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResult.getBody()).isSameAs(loginResponse);
        assertThat(validateResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validateResult.getBody()).isSameAs(validationTokenResponse);
        assertThat(refreshResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResult.getBody()).isSameAs(refreshResponse);
    }
}
