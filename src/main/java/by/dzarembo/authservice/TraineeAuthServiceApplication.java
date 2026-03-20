package by.dzarembo.authservice;

import by.dzarembo.authservice.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties.class)
public class TraineeAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraineeAuthServiceApplication.class, args);
	}

}
