package by.dzarembo.authservice.service;

import by.dzarembo.authservice.dto.LoginRequest;
import by.dzarembo.authservice.dto.RefreshTokenRequest;
import by.dzarembo.authservice.dto.TokenPairResponse;
import by.dzarembo.authservice.dto.ValidationTokenRequest;
import by.dzarembo.authservice.dto.ValidationTokenResponse;
import by.dzarembo.authservice.entity.CredentialEntity;
import by.dzarembo.authservice.entity.Role;
import by.dzarembo.authservice.exception.InactiveUserException;
import by.dzarembo.authservice.exception.InvalidCredentialException;
import by.dzarembo.authservice.exception.InvalidTokenException;
import by.dzarembo.authservice.exception.InvalidTokenTypeException;
import by.dzarembo.authservice.repository.CredentialRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokenPair_whenCredentialsAreValidAndUserActive() {
        CredentialEntity credential = buildCredential(7L, true);
        when(credentialRepository.findByLogin("ivan")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken(credential)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(credential)).thenReturn("refresh-token");

        TokenPairResponse response = authService.login(new LoginRequest("ivan", "plain-password"));

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(credentialRepository).findByLogin("ivan");
        verify(passwordEncoder).matches("plain-password", "encoded-password");
        verify(jwtService).generateAccessToken(credential);
        verify(jwtService).generateRefreshToken(credential);
    }

    @Test
    void login_shouldThrowInvalidCredentialException_whenPasswordDoesNotMatch() {
        CredentialEntity credential = buildCredential(7L, true);
        LoginRequest loginRequest = new LoginRequest("ivan", "wrong-password");
        when(credentialRepository.findByLogin("ivan")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialException.class)
                .hasMessage("Invalid login or password");

        verify(credentialRepository).findByLogin("ivan");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_shouldThrowInactiveUserException_whenCredentialIsInactive() {
        CredentialEntity credential = buildCredential(7L, false);
        LoginRequest loginRequest = new LoginRequest("ivan", "plain-password");
        when(credentialRepository.findByLogin("ivan")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InactiveUserException.class)
                .hasMessage("User is not active.");

        verify(credentialRepository).findByLogin("ivan");
        verify(passwordEncoder).matches("plain-password", "encoded-password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void validate_shouldReturnValidationResult_whenTokenIsValid() {
        DecodedJWT decodedJwt = mock(DecodedJWT.class);
        Claim roleClaim = stringClaim("ADMIN");
        Claim tokenTypeClaim = stringClaim("access");
        when(jwtService.verifyToken("valid-token")).thenReturn(decodedJwt);
        when(decodedJwt.getSubject()).thenReturn("11");
        when(decodedJwt.getClaim("role")).thenReturn(roleClaim);
        when(decodedJwt.getClaim("type")).thenReturn(tokenTypeClaim);

        ValidationTokenResponse response = authService.validate(new ValidationTokenRequest("valid-token"));

        assertThat(response.getValid()).isTrue();
        assertThat(response.getUserId()).isEqualTo(11L);
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getTokenType()).isEqualTo("access");
        verify(jwtService).verifyToken("valid-token");
    }

    @Test
    void validate_shouldThrowInvalidTokenException_whenVerificationFails() {
        ValidationTokenRequest validationTokenRequest = new ValidationTokenRequest("invalid-token");
        when(jwtService.verifyToken("invalid-token"))
                .thenThrow(new JWTVerificationException("Token invalid"));

        assertThatThrownBy(() -> authService.validate(validationTokenRequest))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void refreshToken_shouldReturnNewTokenPair_whenRefreshTokenIsValidAndUserActive() {
        CredentialEntity credential = buildCredential(21L, true);
        DecodedJWT decodedJwt = mock(DecodedJWT.class);
        Claim tokenTypeClaim = stringClaim("refresh");
        when(jwtService.verifyToken("refresh-token")).thenReturn(decodedJwt);
        when(decodedJwt.getSubject()).thenReturn("21");
        when(decodedJwt.getClaim("type")).thenReturn(tokenTypeClaim);
        when(credentialRepository.findByUserId(21L)).thenReturn(Optional.of(credential));
        when(jwtService.generateAccessToken(credential)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(credential)).thenReturn("new-refresh-token");

        TokenPairResponse response = authService.refreshToken(new RefreshTokenRequest("refresh-token"));

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(jwtService).verifyToken("refresh-token");
        verify(credentialRepository).findByUserId(21L);
        verify(jwtService).generateAccessToken(credential);
        verify(jwtService).generateRefreshToken(credential);
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenTypeException_whenTokenTypeIsNotRefresh() {
        DecodedJWT decodedJwt = mock(DecodedJWT.class);
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("access-token");
        Claim tokenTypeClaim = stringClaim("access");
        when(jwtService.verifyToken("access-token")).thenReturn(decodedJwt);
        when(decodedJwt.getClaim("type")).thenReturn(tokenTypeClaim);

        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                .isInstanceOf(InvalidTokenTypeException.class)
                .hasMessage("Invalid token type");
    }

    private Claim stringClaim(String value) {
        Claim claim = mock(Claim.class);
        when(claim.asString()).thenReturn(value);
        return claim;
    }

    private CredentialEntity buildCredential(Long userId, boolean active) {
        return CredentialEntity.builder()
                .userId(userId)
                .login("ivan")
                .passwordHash("encoded-password")
                .role(Role.ADMIN)
                .active(active)
                .build();
    }
}
