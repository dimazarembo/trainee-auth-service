package by.dzarembo.authservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationTokenResponse {
    private Boolean valid;
    private Long userId;
    private String role;
    private String tokenType;
}
