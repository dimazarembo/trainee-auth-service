package by.dzarembo.authservice.dto;

import by.dzarembo.authservice.entity.Role;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialResponse {
    private Long id;
    private Long userId;
    private String login;
    private Role role;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
