package by.dzarembo.authservice.mapper;

import by.dzarembo.authservice.dto.CreateCredentialRequest;
import by.dzarembo.authservice.dto.CredentialResponse;
import by.dzarembo.authservice.entity.CredentialEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps credential entities to API DTOs and back.
 */
@Mapper(componentModel = "spring")
public interface CredentialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CredentialEntity toEntity(CreateCredentialRequest request);

    CredentialResponse toResponse(CredentialEntity entity);
}
