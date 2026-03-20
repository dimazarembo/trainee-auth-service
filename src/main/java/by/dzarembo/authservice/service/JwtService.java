package by.dzarembo.authservice.service;

import by.dzarembo.authservice.config.JwtProperties;
import by.dzarembo.authservice.entity.CredentialEntity;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        algorithm = Algorithm.HMAC256(jwtProperties.secret());
        verifier = JWT.require(algorithm).build();
    }

    public String generateAccessToken(CredentialEntity credential) {
        return generateToken(credential, jwtProperties.accessTokenExpiration(), "access");
    }

    public String generateRefreshToken(CredentialEntity credential) {
        return generateToken(credential, jwtProperties.refreshTokenExpiration(), "refresh");
    }

    private String generateToken(CredentialEntity credential, long expirationMs, String type) {
        Instant now = Instant.now();

        return JWT.create()
                .withSubject(String.valueOf(credential.getUserId()))
                .withClaim("role", credential.getRole().name())
                .withClaim("type", type)
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(expirationMs))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return verifier.verify(token);
    }

}
