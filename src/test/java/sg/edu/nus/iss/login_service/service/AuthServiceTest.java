package sg.edu.nus.iss.login_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.nus.iss.login_service.dto.*;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.repository.UserRepository;

import java.util.Optional;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");

        String result = authService.registerUser(request);
        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        String result = authService.registerUser(request);
        assertEquals("Email already registered!", result);
    }

    @Test
    void testLoginUser_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        User user = new User();
        user.setPassword("hashedPassword");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        String result = authService.loginUser(request);
        assertEquals("Login successful!", result);
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        String result = authService.loginUser(request);
        assertEquals("Invalid credentials!", result);
    }

    @Test
    void testGenerateOtp_EmailNotRegistered() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        String result = authService.generateOtp("john@example.com");
        assertEquals("Email not registered!", result);
    }

    @Test
    void testResetPassword_Success() {
        User user = new User();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("hashedNewPassword");

        String result = authService.resetPassword("john@example.com", "newpassword123");
        assertEquals("Password updated successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testResetPassword_EmailNotRegistered() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        String result = authService.resetPassword("john@example.com", "newpassword123");
        assertEquals("Email not registered!", result);
    }

    @Test
    void testValidateOtp_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("newpassword123");
        when(otpService.validateOtp("john@example.com", "123456")).thenReturn(true);
        User user = new User();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("hashedNewPassword");

        String result = authService.validateOtp("john@example.com", "123456", request);
        assertEquals("Password reset successful!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        ResetPasswordRequest request = new ResetPasswordRequest("newpassword123");
        when(otpService.validateOtp("john@example.com", "123456")).thenReturn(false);

        String result = authService.validateOtp("john@example.com", "123456", request);
        assertEquals("Invalid or expired OTP!", result);
    }
}
