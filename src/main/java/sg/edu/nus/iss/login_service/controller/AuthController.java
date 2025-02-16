package sg.edu.nus.iss.login_service.controller;

import sg.edu.nus.iss.login_service.dto.LoginRequest;
import sg.edu.nus.iss.login_service.dto.RegisterRequest;
import sg.edu.nus.iss.login_service.dto.ForgotPasswordRequest;
import sg.edu.nus.iss.login_service.dto.OtpRequest;
import sg.edu.nus.iss.login_service.service.AuthService;
import sg.edu.nus.iss.login_service.service.OtpService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest request) {
        logger.info("User registration attempt for email: {}", request.getEmail());
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest request) {
        logger.info("User login attempt for email: {}", request.getEmail());
        boolean authenticated = Boolean.parseBoolean(authService.loginUser(request));
        if (authenticated) {
            return ResponseEntity.ok("Login successful!");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials!");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Forgot password request for email: {}", request.getEmail());
        otpService.generateOtp(request.getEmail());
        return ResponseEntity.ok("OTP sent to your email.");
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@Valid @RequestBody OtpRequest request) {
        logger.info("OTP validation attempt for email: {}", request.getEmail());
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (isValid) {
            return ResponseEntity.ok("OTP validated successfully!");
        } else {
            return ResponseEntity.status(400).body("Invalid or expired OTP.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Reset password request for email: {}", request.getEmail());

        // Call resetPassword from AuthService
        String result = authService.resetPassword(request.getEmail(), request.getNewPassword());

        if ("Password updated successfully!".equals(result)) {
            return ResponseEntity.ok("Password updated successfully.");
        } else {
            return ResponseEntity.status(400).body(result);
        }
    }

}
