package by.dzarembo.authservice.web;

import by.dzarembo.authservice.dto.ErrorResponse;
import by.dzarembo.authservice.exception.DuplicateCredentialException;
import by.dzarembo.authservice.exception.DuplicateLoginException;
import by.dzarembo.authservice.exception.InactiveUserException;
import by.dzarembo.authservice.exception.InvalidCredentialException;
import by.dzarembo.authservice.exception.InvalidTokenException;
import by.dzarembo.authservice.exception.InvalidTokenTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handlers_shouldReturnExpectedErrorResponses() {
        assertResponse(
                globalExceptionHandler.handleDuplicateLogin(new DuplicateLoginException("Login already exists")),
                HttpStatus.CONFLICT,
                "Bad Request",
                "Login already exists"
        );
        assertResponse(
                globalExceptionHandler.handleDuplicateCredential(
                        new DuplicateCredentialException("Credentials for user already exist")
                ),
                HttpStatus.CONFLICT,
                "Bad Request",
                "Credentials for user already exist"
        );
        assertResponse(
                globalExceptionHandler.handleInvalidCredential(
                        new InvalidCredentialException("Invalid login or password")
                ),
                HttpStatus.UNAUTHORIZED,
                "Access Denied",
                "Invalid login or password"
        );
        assertResponse(
                globalExceptionHandler.handleInactiveUser(new InactiveUserException("User is not active.")),
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "User is not active."
        );
        assertResponse(
                globalExceptionHandler.handleInvalidToken(new InvalidTokenException("Invalid token")),
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid token"
        );
        assertResponse(
                globalExceptionHandler.handleInvalidTokenType(
                        new InvalidTokenTypeException("Invalid token type")
                ),
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid token type"
        );
    }

    private void assertResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedError,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo(expectedError);
        assertThat(response.getBody().getMessage()).isEqualTo(expectedMessage);
    }
}
