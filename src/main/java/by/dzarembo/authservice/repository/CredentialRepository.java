package by.dzarembo.authservice.repository;

import by.dzarembo.authservice.entity.CredentialEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {
    boolean existsByLogin(String login);

    Optional<CredentialEntity> findByLogin(String login);

    Optional<CredentialEntity> findByUserId(Long userId);

    boolean existsByUserId(@NotNull @Positive Long userId);
}
