package by.dzarembo.authservice.mapper;

import by.dzarembo.authservice.dto.CreateCredentialRequest;
import by.dzarembo.authservice.dto.CredentialResponse;
import by.dzarembo.authservice.entity.CredentialEntity;
import by.dzarembo.authservice.entity.Role;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialMapperTest {

    private final CredentialMapper credentialMapper = Mappers.getMapper(CredentialMapper.class);

    @Test
    void toEntity_shouldMapRequestFieldsAndIgnoreGeneratedFields() {
        CreateCredentialRequest request = CreateCredentialRequest.builder()
                .userId(17L)
                .login("petya")
                .password("plain-password")
                .role(Role.USER)
                .build();

        CredentialEntity entity = credentialMapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getUserId()).isEqualTo(17L);
        assertThat(entity.getLogin()).isEqualTo("petya");
        assertThat(entity.getPasswordHash()).isNull();
        assertThat(entity.getRole()).isEqualTo(Role.USER);
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void toResponse_shouldMapEntityFieldsAndReturnNullForNullSources() {
        Instant createdAt = Instant.parse("2026-03-26T10:15:30Z");
        Instant updatedAt = Instant.parse("2026-03-26T10:16:30Z");
        CredentialEntity entity = CredentialEntity.builder()
                .id(17L)
                .userId(25L)
                .login("petya")
                .passwordHash("encoded-password")
                .role(Role.USER)
                .active(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        CredentialResponse response = credentialMapper.toResponse(entity);

        assertThat(response.getId()).isEqualTo(17L);
        assertThat(response.getUserId()).isEqualTo(25L);
        assertThat(response.getLogin()).isEqualTo("petya");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getActive()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
