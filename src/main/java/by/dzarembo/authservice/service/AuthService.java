package by.dzarembo.authservice.service;

import by.dzarembo.authservice.dto.*;
import by.dzarembo.authservice.entity.CredentialEntity;
import by.dzarembo.authservice.exception.InactiveUserException;
import by.dzarembo.authservice.exception.InvalidCredentialException;
import by.dzarembo.authservice.exception.InvalidTokenException;
import by.dzarembo.authservice.exception.InvalidTokenTypeException;
import by.dzarembo.authservice.repository.CredentialRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static by.dzarembo.authservice.service.JwtConstants.*;

@Service
@AllArgsConstructor
public class AuthService {
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TokenPairResponse login(LoginRequest loginRequest) {
        CredentialEntity credentialEntity = credentialRepository.findByLogin(loginRequest.getLogin())
                .orElseThrow(() -> new InvalidCredentialException("Invalid login or password"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), credentialEntity.getPasswordHash())) {
            throw new InvalidCredentialException("Invalid login or password");
        }
        if (Boolean.FALSE.equals(credentialEntity.getActive())) {
            throw new InactiveUserException("User is not active.");
        }

        return new TokenPairResponse(
                jwtService.generateAccessToken(credentialEntity),
                jwtService.generateRefreshToken(credentialEntity)
        );
    }

    public ValidationTokenResponse validate(ValidationTokenRequest validationTokenRequest) {
        DecodedJWT jwt;
        try {
            jwt = jwtService.verifyToken(validationTokenRequest.getToken());
        } catch (JWTVerificationException ex) {
            throw new InvalidTokenException("Invalid token");
        }
        Long userId = Long.valueOf(jwt.getSubject());
        String role = jwt.getClaim(ROLE).asString();
        String tokenType = jwt.getClaim(TYPE).asString();

        return new ValidationTokenResponse(true, userId, role, tokenType);
    }

    public TokenPairResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        DecodedJWT jwt;
        try {
            jwt = jwtService.verifyToken(refreshTokenRequest.getRefreshToken());
        } catch (JWTVerificationException ex) {
            throw new InvalidTokenException("Invalid token");
        }

        String tokenType = jwt.getClaim(TYPE).asString();
        if (!REFRESH_TOKEN.equals(tokenType)) {
            throw new InvalidTokenTypeException("Invalid token type");
        }

        Long userId = Long.valueOf(jwt.getSubject());

        CredentialEntity credential = credentialRepository
                .findByUserId(userId).orElseThrow(() -> new InvalidCredentialException("Invalid token"));

        if (Boolean.FALSE.equals(credential.getActive())) {
            throw new InactiveUserException("User is not active.");
        }
        return new TokenPairResponse(
                jwtService.generateAccessToken(credential),
                jwtService.generateRefreshToken(credential)
        );

    }
}
