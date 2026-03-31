package by.dzarembo.authservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
}
