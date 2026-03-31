package by.dzarembo.authservice.service;

import by.dzarembo.authservice.config.JwtProperties;
import by.dzarembo.authservice.entity.CredentialEntity;
import by.dzarembo.authservice.entity.Role;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void generateAccessToken_shouldIncludeCredentialClaimsAndConfiguredExpiration() {
        JwtService jwtService = new JwtService(new JwtProperties("access-secret", 30_000L, 90_000L));
        CredentialEntity credential = buildCredential(7L, Role.ADMIN);
        Instant beforeGeneration = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        String token = jwtService.generateAccessToken(credential);

        Instant afterGeneration = Instant.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        DecodedJWT decodedJwt = jwtService.verifyToken(token);

        assertThat(decodedJwt.getSubject()).isEqualTo("7");
        assertThat(decodedJwt.getClaim("role").asString()).isEqualTo(Role.ADMIN.name());
        assertThat(decodedJwt.getClaim("type").asString()).isEqualTo("access");
        assertThat(decodedJwt.getIssuedAtAsInstant())
                .isAfterOrEqualTo(beforeGeneration)
                .isBeforeOrEqualTo(afterGeneration);
        assertThat(decodedJwt.getExpiresAtAsInstant())
                .isAfterOrEqualTo(beforeGeneration.plusMillis(30_000L))
                .isBeforeOrEqualTo(afterGeneration.plusMillis(30_000L));
    }

    @Test
    void generateRefreshToken_shouldIncludeRefreshTypeAndConfiguredExpiration() {
        JwtService jwtService = new JwtService(new JwtProperties("refresh-secret", 15_000L, 120_000L));
        CredentialEntity credential = buildCredential(15L, Role.USER);
        Instant beforeGeneration = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        String token = jwtService.generateRefreshToken(credential);

        Instant afterGeneration = Instant.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
        DecodedJWT decodedJwt = jwtService.verifyToken(token);

        assertThat(decodedJwt.getSubject()).isEqualTo("15");
        assertThat(decodedJwt.getClaim("role").asString()).isEqualTo(Role.USER.name());
        assertThat(decodedJwt.getClaim("type").asString()).isEqualTo("refresh");
        assertThat(decodedJwt.getIssuedAtAsInstant())
                .isAfterOrEqualTo(beforeGeneration)
                .isBeforeOrEqualTo(afterGeneration);
        assertThat(decodedJwt.getExpiresAtAsInstant())
                .isAfterOrEqualTo(beforeGeneration.plusMillis(120_000L))
                .isBeforeOrEqualTo(afterGeneration.plusMillis(120_000L));
    }

    @Test
    void verifyToken_shouldThrowJwtVerificationException_whenTokenSignedWithDifferentSecret() {
        JwtService tokenIssuer = new JwtService(new JwtProperties("issuer-secret", 30_000L, 60_000L));
        JwtService tokenVerifier = new JwtService(new JwtProperties("different-secret", 30_000L, 60_000L));

        String token = tokenIssuer.generateAccessToken(buildCredential(3L, Role.USER));

        assertThatThrownBy(() -> tokenVerifier.verifyToken(token))
                .isInstanceOf(JWTVerificationException.class);
    }

    private CredentialEntity buildCredential(Long userId, Role role) {
        return CredentialEntity.builder()
                .userId(userId)
                .login("user-" + userId)
                .passwordHash("encoded-password")
                .role(role)
                .active(true)
                .build();
    }
}
