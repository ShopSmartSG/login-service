package sg.edu.nus.iss.login_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "otp")
public class Otp {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String email;
    private String code;
    private LocalDateTime expirationTime;
    private int attemptCount;
    private boolean blocked;
    private LocalDateTime blockedUntil;

    public Otp() {}

    public Otp(String email, String code, LocalDateTime expirationTime) {
        this.email = email;
        this.code = code;
        this.expirationTime = expirationTime;
        this.attemptCount = 0;
        this.blocked = false;
        this.blockedUntil = null;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationTime);
    }

    public boolean isCurrentlyBlocked() {
        return this.blockedUntil != null && LocalDateTime.now().isBefore(this.blockedUntil);
    }

    public void incrementAttempts() {
        this.attemptCount++;
        if (this.attemptCount >= 3) {
            this.blocked = true;
            this.blockedUntil = LocalDateTime.now().plusMinutes(15);
        }
    }
}
