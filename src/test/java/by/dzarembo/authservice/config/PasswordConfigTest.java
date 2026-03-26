package by.dzarembo.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigTest {

    @Test
    void passwordEncoder_shouldEncodeAndMatchPassword() {
        PasswordEncoder passwordEncoder = new PasswordConfig().passwordEncoder();
        String password = "secret123";
        String encodedPassword = passwordEncoder.encode(password);

        assertThat(encodedPassword).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, encodedPassword)).isTrue();
    }
}
