package sg.edu.nus.iss.login_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String id = UUID.randomUUID().toString();
    private String email;
    private String password; // Hashed password
    private String lastOtpCode; // Stores last generated OTP
    private LocalDateTime otpExpiry; // Expiry time for OTP
    private ProfileType profileType;

    private int failedAttempts;
    private boolean locked;
    private LocalDateTime lockExpiry;

    public User() {}

    public User(String email, String rawPassword) {
        this.email = email;
        this.password = hashPassword(rawPassword);
        this.failedAttempts = 0;
        this.locked = false;
    }

    private String hashPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, this.password);
    }

    public boolean validateOtp(String otp) {
        return this.lastOtpCode != null && this.lastOtpCode.equals(otp) &&
                this.otpExpiry != null && LocalDateTime.now().isBefore(otpExpiry);
    }

    public boolean validateLogin(String rawPassword, String otp) {
        return checkPassword(rawPassword) && validateOtp(otp);
    }

    public void updateOtp(String otp) {
        this.lastOtpCode = otp;
        this.otpExpiry = LocalDateTime.now().plusMinutes(5);
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3) {
            lockAccount();
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.locked = false;
        this.lockExpiry = null;
    }

    public void lockAccount() {
        this.locked = true;
        this.lockExpiry = LocalDateTime.now().plusHours(24);
    }
}
