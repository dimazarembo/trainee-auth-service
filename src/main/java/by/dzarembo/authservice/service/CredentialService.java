package by.dzarembo.authservice.service;

import by.dzarembo.authservice.dto.CreateCredentialRequest;
import by.dzarembo.authservice.dto.CredentialResponse;
import by.dzarembo.authservice.exception.DuplicateCredentialException;
import by.dzarembo.authservice.exception.DuplicateLoginException;
import by.dzarembo.authservice.mapper.CredentialMapper;
import by.dzarembo.authservice.repository.CredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CredentialService {
    private final CredentialRepository credentialRepository;
    private final CredentialMapper credentialMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CredentialResponse create(CreateCredentialRequest createCredentialRequest) {
        if (credentialRepository.existsByLogin(createCredentialRequest.getLogin())) {
            throw new DuplicateLoginException("Login already exists");
        }
        if (credentialRepository.existsByUserId(createCredentialRequest.getUserId())) {
            throw new DuplicateCredentialException("Credentials for user already exist");
        }
        var createEntity = credentialMapper.toEntity(createCredentialRequest);
        createEntity.setPasswordHash(passwordEncoder.encode(createCredentialRequest.getPassword()));
        createEntity.setActive(true);
        var savedEntity = credentialRepository.save(createEntity);
        return credentialMapper.toResponse(savedEntity);
    }
}
