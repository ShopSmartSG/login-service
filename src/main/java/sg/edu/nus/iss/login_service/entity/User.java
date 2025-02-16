package sg.edu.nus.iss.login_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private UUID id;
    private String email;
    private String password; // Hashed password

    public User() {}

    public User(String email, String rawPassword) {
        this.email = email;
        this.password = hashPassword(rawPassword);
    }

    private String hashPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, this.password);
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
