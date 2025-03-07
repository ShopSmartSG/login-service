package sg.edu.nus.iss.login_service.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.nus.iss.login_service.dto.*;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.exception.OtpException;
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

        if (userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType()).isPresent()) {
            logger.warn("Registration failed: Email {} is already in use", request.getEmail());
            return "Email already registered!";
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProfileType(request.getProfileType());
        userRepository.save(user);

        logger.info("User registered successfully: {}", request.getEmail());
        return "User registered successfully!";
    }

    public String loginUser(LoginRequest request) {
        logger.info("Attempting login for email: {}", request.getEmail());

        Optional<User> userOptional = userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType());
        if (userOptional.isEmpty()) {
            logger.warn("Login failed: Email {} not found", request.getEmail());
            throw new OtpException.InvalidCredentialsException("Invalid credentials! Email not found.");
        }

        User user = userOptional.get();
        // Ensure the password matches the correct profile type
        if (!user.getProfileType().equals(request.getProfileType())) {
            logger.warn("Login failed: Incorrect profile type for email {}", request.getEmail());
            throw new OtpException.InvalidCredentialsException("Invalid profile type!");
        }
        return validateOtpAndPassword(user, request.getOtp(), request.getPassword());
    }

    private String validateOtpAndPassword(User user, String otp, String password) {
        if (user.isLocked() && user.getLockExpiry().isAfter(LocalDateTime.now())) {
            logger.warn("Login failed: Account locked");
            throw new OtpException.AccountLockedException("Account locked! Try again later.");
        }

        // Validate OTP for the correct profile type
        if (!otpService.validateOtp(user.getEmail(), otp, user.getProfileType())) {
            handleFailedAttempt(user);
            throw new OtpException.InvalidOtpException("Invalid OTP!");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleFailedAttempt(user);
            throw new OtpException.InvalidCredentialsException("Invalid credentials! Incorrect password.");
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

    public String generateOtp(String email, ProfileType profileType) {
        logger.info("Generating OTP for email");
        try {
            return otpService.generateAndStoreOtp(email, profileType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String resetPassword(ResetPasswordRequest request) {
        logger.info("Validating old password for password reset");

        Optional<User> userOptional = userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType());
        if (userOptional.isEmpty()) {
            logger.warn("Reset password failed: Email not found");
            throw new OtpException.EmailNotFoundException("Email not registered!");
        }

        User user = userOptional.get();

        // Ensure the profile type matches
        if (!user.getProfileType().equals(request.getProfileType())) {
            logger.warn("Reset password failed: Incorrect profile type");
            throw new OtpException.InvalidCredentialsException("Invalid profile type!");
        }

        // Validate the old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            logger.warn("Reset password failed: Old password is incorrect");
            throw new OtpException.UnprocessableEntityException("Old password is incorrect!"); // Throwing the custom exception
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

        Optional<User> userOptional = userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType());
        if (userOptional.isEmpty()) {
            logger.warn("Forgot password failed: Email not found");
            throw new OtpException.EmailNotFoundException("Email not registered!");
        }

        User user = userOptional.get();

        // Ensure the profile type matches
        if (!user.getProfileType().equals(request.getProfileType())) {
            logger.warn("Forgot password failed: Incorrect profile type");
            throw new OtpException.InvalidCredentialsException("Invalid profile type!");
        }

        // Validate the OTP
        if (!otpService.validateOtp(request.getEmail(), request.getOtp(), request.getProfileType())) {
            throw new OtpException.InvalidOtpException("Invalid or expired OTP!");
        }

        // Set the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);  // Save the updated user with the new password

        logger.info("Password reset successfully");
        return "Password reset successful!";
    }
}
