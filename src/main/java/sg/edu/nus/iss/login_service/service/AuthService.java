package sg.edu.nus.iss.login_service.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.nus.iss.login_service.dto.ForgotPasswordRequest;
import sg.edu.nus.iss.login_service.dto.LoginRequest;
import sg.edu.nus.iss.login_service.dto.RegisterRequest;
import sg.edu.nus.iss.login_service.dto.ResetPasswordRequest;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    public String registerUser(RegisterRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} is already in use", request.getEmail());
            return "Email already registered!";
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        logger.info("User registered successfully: {}", request.getEmail());
        return "User registered successfully!";
    }

    public String loginUser(LoginRequest request) {
        logger.info("Attempting login for email: {}", request.getEmail());

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            logger.warn("Login failed: Email {} not found", request.getEmail());
            return "Invalid credentials!";
        }

        User user = userOptional.get();
        return validateOtpAndPassword(user, request.getOtp(), request.getPassword());
    }

    private String validateOtpAndPassword(User user, String otp, String password) {
        if (user.isLocked() && user.getLockExpiry().isAfter(LocalDateTime.now())) {
            logger.warn("Login failed: Account locked");
            return "Account locked! Try again later.";
        }

        if (!otpService.validateOtp(user.getEmail(), otp)) {
            handleFailedAttempt(user);
            return "Invalid OTP!";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleFailedAttempt(user);
            return "Invalid credentials!";
        }

        user.resetFailedAttempts();
        userRepository.save(user);
        logger.info("User logged in successfully");
        return "Login successful!";
    }

    private void handleFailedAttempt(User user) {
        user.incrementFailedAttempts();
        if (user.getFailedAttempts() >= 3) {
            user.lockAccount();
            logger.warn("User locked due to multiple failed attempts");
        }
        userRepository.save(user);
    }

    public String generateOtp(String email) {
        logger.info("Generating OTP for email");
        try {
            return otpService.generateAndStoreOtp(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String resetPassword(String email, ResetPasswordRequest request) {
        logger.info("Validating old password for password reset");

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Reset password failed: Email not found");
            return "Email not registered!";
        }

        User user = userOptional.get();

        // Validate the old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return "Old password is incorrect!";
        }

        // Set the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user); // Save the updated user with the new password in one network call

        logger.info("Password reset successfully");
        return "Password reset successful!";
    }

    // Forgot password method to handle OTP validation and reset password
    public String forgotPassword(ForgotPasswordRequest request) {
        logger.info("Verifying OTP for password reset");

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            logger.warn("Forgot password failed: Email not found");
            return "Email not registered!";
        }

        User user = userOptional.get();

        // Validate the OTP
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            return "Invalid or expired OTP!";
        }

        // Set the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);  // Save the updated user with the new password

        logger.info("Password reset successfully");
        return "Password reset successful!";
    }
}
