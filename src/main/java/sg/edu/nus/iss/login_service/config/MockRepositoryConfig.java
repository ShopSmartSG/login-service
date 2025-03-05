package sg.edu.nus.iss.login_service.config;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import sg.edu.nus.iss.login_service.repository.OtpRepository;
import sg.edu.nus.iss.login_service.repository.UserRepository;

@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "zapscan")
public class MockRepositoryConfig {

    @Bean
    @Primary
    public OtpRepository merchantRepository() {
        return Mockito.mock(OtpRepository.class);
    }

    @Bean
    @Primary
    public UserRepository customerRepository() {
        return Mockito.mock(UserRepository.class);
    }
} 