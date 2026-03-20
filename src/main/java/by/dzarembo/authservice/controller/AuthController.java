package by.dzarembo.authservice.controller;

import by.dzarembo.authservice.dto.*;
import by.dzarembo.authservice.service.AuthService;
import by.dzarembo.authservice.service.CredentialService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final CredentialService credentialService;


    @PostMapping("/credentials")
    public ResponseEntity<CredentialResponse> create(@Valid @RequestBody CreateCredentialRequest createCredentialRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(credentialService.create(createCredentialRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationTokenResponse> validate(@Valid @RequestBody ValidationTokenRequest validationTokenRequest) {
        return ResponseEntity.ok(authService.validate(validationTokenRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }
}
