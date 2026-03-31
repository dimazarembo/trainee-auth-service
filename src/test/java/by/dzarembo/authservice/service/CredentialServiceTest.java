package by.dzarembo.authservice.service;

import by.dzarembo.authservice.dto.CreateCredentialRequest;
import by.dzarembo.authservice.dto.CredentialResponse;
import by.dzarembo.authservice.entity.CredentialEntity;
import by.dzarembo.authservice.entity.Role;
import by.dzarembo.authservice.exception.DuplicateCredentialException;
import by.dzarembo.authservice.exception.DuplicateLoginException;
import by.dzarembo.authservice.mapper.CredentialMapper;
import by.dzarembo.authservice.repository.CredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CredentialService credentialService;

    @BeforeEach
    void setUp() {
        CredentialMapper credentialMapper = Mappers.getMapper(CredentialMapper.class);
        credentialService = new CredentialService(credentialRepository, credentialMapper, passwordEncoder);
    }

    @Test
    void create_shouldThrowDuplicateLoginException_whenLoginAlreadyExists() {
        CreateCredentialRequest request = buildCreateCredentialRequest();
        when(credentialRepository.existsByLogin("ivan")).thenReturn(true);

        assertThatThrownBy(() -> credentialService.create(request))
                .isInstanceOf(DuplicateLoginException.class)
                .hasMessage("Login already exists");

        verify(credentialRepository).existsByLogin("ivan");
        verify(credentialRepository, never()).existsByUserId(request.getUserId());
        verify(credentialRepository, never()).save(any(CredentialEntity.class));
    }

    @Test
    void create_shouldThrowDuplicateCredentialException_whenCredentialsAlreadyExistForUser() {
        CreateCredentialRequest request = buildCreateCredentialRequest();
        when(credentialRepository.existsByLogin("ivan")).thenReturn(false);
        when(credentialRepository.existsByUserId(7L)).thenReturn(true);

        assertThatThrownBy(() -> credentialService.create(request))
                .isInstanceOf(DuplicateCredentialException.class)
                .hasMessage("Credentials for user already exist");

        verify(credentialRepository).existsByLogin("ivan");
        verify(credentialRepository).existsByUserId(7L);
        verify(credentialRepository, never()).save(any(CredentialEntity.class));
    }

    @Test
    void create_shouldEncodePasswordPersistEntityAndReturnMappedResponse_whenRequestIsValid() {
        CreateCredentialRequest request = buildCreateCredentialRequest();
        Instant createdAt = Instant.parse("2026-03-26T10:15:30Z");
        when(credentialRepository.existsByLogin("ivan")).thenReturn(false);
        when(credentialRepository.existsByUserId(7L)).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(credentialRepository.save(any(CredentialEntity.class))).thenAnswer(invocation -> {
            CredentialEntity entity = invocation.getArgument(0);
            entity.setId(13L);
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(createdAt);
            return entity;
        });

        CredentialResponse response = credentialService.create(request);

        ArgumentCaptor<CredentialEntity> credentialCaptor = ArgumentCaptor.forClass(CredentialEntity.class);
        verify(credentialRepository).save(credentialCaptor.capture());

        CredentialEntity savedCredential = credentialCaptor.getValue();
        assertThat(savedCredential.getUserId()).isEqualTo(7L);
        assertThat(savedCredential.getLogin()).isEqualTo("ivan");
        assertThat(savedCredential.getRole()).isEqualTo(Role.ADMIN);
        assertThat(savedCredential.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedCredential.getActive()).isTrue();

        assertThat(response.getId()).isEqualTo(13L);
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getLogin()).isEqualTo("ivan");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getActive()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(createdAt);
    }

    private CreateCredentialRequest buildCreateCredentialRequest() {
        return CreateCredentialRequest.builder()
                .userId(7L)
                .login("ivan")
                .password("plain-password")
                .role(Role.ADMIN)
                .build();
    }
}
