package sg.edu.nus.iss.login_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otp") // MongoDB collection
public class Otp {

    @Id
    private String id = UUID.randomUUID().toString();
    @Indexed(unique = true)
    private String email;
    private String code;
    private LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(3);
    private int failedAttempts = 0;
    private boolean blocked;  // Blocked status after maximum attempts
    @Setter
    private LocalDateTime blockedUntil; // Null if not blocked

    public Otp(String email, String code, LocalDateTime expirationTime) {
        this.email = email;
        this.code = code;
        this.expirationTime = expirationTime;
        this.failedAttempts = 0;
        this.blocked = false;  // Not blocked initially
        this.blockedUntil = null;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public boolean isCurrentlyBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    public void incrementAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3) {
            this.blocked = true;
            this.blockedUntil = LocalDateTime.now().plusMinutes(15);  // Block user for 15 minutes
        }
    }

    public void reset() {
        this.failedAttempts = 0;
        this.blocked = false;
        this.blockedUntil = null;
    }
}
