package by.dzarembo.authservice.web;

import by.dzarembo.authservice.dto.ErrorResponse;
import by.dzarembo.authservice.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateLoginException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLogin(
            DuplicateLoginException ex
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateCredentialException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCredential(
            DuplicateCredentialException ex
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredential(
            InvalidCredentialException ex
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                "Access Denied",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorResponse> handleInactiveUser(
            InactiveUserException ex
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                "Access Denied",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }


    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                "Unauthorized",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }


    @ExceptionHandler(InvalidTokenTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenType(
            InvalidTokenTypeException ex
    ) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                "Unauthorized",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

}
