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
        if (credentialEntity.getActive().equals(false)) {
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
        String role = jwt.getClaim("role").asString();
        String tokenType = jwt.getClaim("type").asString();

        return new ValidationTokenResponse(true, userId, role, tokenType);
    }

    public TokenPairResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        DecodedJWT jwt;
        try {
            jwt = jwtService.verifyToken(refreshTokenRequest.getRefreshToken());
        } catch (JWTVerificationException ex) {
            throw new InvalidTokenException("Invalid token");
        }

        String tokenType = jwt.getClaim("type").asString();
        if (!"refresh".equals(tokenType)) {
            throw new InvalidTokenTypeException("Invalid token type");
        }

        Long userId = Long.valueOf(jwt.getSubject());

        CredentialEntity credential = credentialRepository
                .findByUserId(userId).orElseThrow(() -> new InvalidCredentialException("Invalid token"));

        if (credential.getActive().equals(false)) {
            throw new InactiveUserException("User is not active.");
        }
        return new TokenPairResponse(
                jwtService.generateAccessToken(credential),
                jwtService.generateRefreshToken(credential)
        );

    }
}
