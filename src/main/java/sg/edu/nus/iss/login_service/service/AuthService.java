package sg.edu.nus.iss.login_service.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.nus.iss.login_service.dto.LoginRequest;
import sg.edu.nus.iss.login_service.dto.RegisterRequest;
import sg.edu.nus.iss.login_service.dto.ResetPasswordRequest;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.repository.UserRepository;
import sg.edu.nus.iss.login_service.security.PasswordEncoderConfig;
import sg.edu.nus.iss.login_service.service.OtpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpUtil;

    @Autowired
    private EmailService emailService;

    public String registerUser(RegisterRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
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
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Incorrect password for {}", request.getEmail());
            return "Invalid credentials!";
        }

        logger.info("User {} logged in successfully", request.getEmail());
        return "Login successful!";
    }

    public String generateOtp(String email) {
        logger.info("Generating OTP for email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("OTP request failed: Email {} not found", email);
            return "Email not registered!";
        }

        String otp = String.valueOf(otpUtil.generateOtp(email));
        emailService.sendOtpEmail(email, otp);

        logger.info("OTP sent successfully to {}", email);
        return "OTP sent to email!";
    }

    public String validateOtp(String email, String otp, ResetPasswordRequest resetRequest) {
        logger.info("Validating OTP for email: {}", email);

        boolean isValid = otpUtil.validateOtp(email, otp);
        if (!isValid) {
            logger.warn("OTP validation failed for {}", email);
            return "Invalid or expired OTP!";
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Reset password failed: Email {} not found", email);
            return "Email not registered!";
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
        userRepository.save(user);

        logger.info("Password reset successfully for {}", email);
        return "Password reset successful!";
    }

    // Method to initiate the password reset process (send OTP)
    // Method to initiate the password reset process (send OTP and reset password)
    public String resetPassword(String email, String newPassword) {
        logger.info("Initiating password reset process for email: {}", email);

        // Check if the email exists in the system
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Password reset failed: Email {} not found", email);
            return "Email not registered!";
        }

        // Update the user's password
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password successfully updated for {}", email);
        return "Password updated successfully!";
    }


    // Method to validate OTP and set new password
    public String getNewPassword(String email, String otp, String newPassword) {
        logger.info("Getting new password for email: {}", email);

        // Validate the OTP
        boolean isValidOtp = otpUtil.validateOtp(email, otp);
        if (!isValidOtp) {
            logger.warn("OTP validation failed for {}", email);
            return "Invalid or expired OTP!";
        }

        // Fetch the user from the database
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("Password reset failed: Email {} not found", email);
            return "Email not registered!";
        }

        // Update the user's password
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password successfully updated for {}", email);
        return "Password updated successfully!";
    }
}
