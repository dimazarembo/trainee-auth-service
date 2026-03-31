package by.dzarembo.authservice.dto;

import by.dzarembo.authservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCredentialRequest {
    @NotNull
    @Positive
    private Long userId;

    @NotBlank
    @Size(max = 50)
    private String login;

    @NotBlank
    @Size(min = 6, max = 50)
    private String password;

    @NotNull
    private Role role;
}
