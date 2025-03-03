package sg.edu.nus.iss.login_service.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.nus.iss.login_service.dto.ForgotPasswordRequest;
import sg.edu.nus.iss.login_service.dto.LoginRequest;
import sg.edu.nus.iss.login_service.dto.RegisterRequest;
import sg.edu.nus.iss.login_service.dto.ResetPasswordRequest;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        String result = authService.registerUser(request);
        assertEquals("User registered successfully!", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        String result = authService.registerUser(request);
        assertEquals("Email already registered!", result);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setOtp("123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(otpService.validateOtp(request.getEmail(), request.getOtp())).thenReturn(true);

        String result = authService.loginUser(request);
        assertEquals("Login successful!", result);
    }

    @Test
    void testLoginUser_InvalidOtp() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setOtp("000000");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(request.getEmail(), request.getOtp())).thenReturn(false);

        String result = authService.loginUser(request);
        assertEquals("Invalid OTP!", result);
    }

    @Test
    void testLoginUser_InvalidPassword() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");
        request.setOtp("123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        String result = authService.loginUser(request);
        assertEquals("Invalid OTP!", result);
    }

    @Test
    void testLoginUser_AccountLocked() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setLocked(true);
        user.setLockExpiry(LocalDateTime.now().plusMinutes(1));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setOtp("123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        String result = authService.loginUser(request);
        assertEquals("Account locked! Try again later.", result);
    }

    @Test
    void testGenerateOtp_Success() throws Exception {
        when(otpService.generateAndStoreOtp("test@example.com")).thenReturn("123456");

        String result = authService.generateOtp("test@example.com");
        assertEquals("123456", result);
    }

    @Test
    void testGenerateOtp_Failure() throws Exception {
        when(otpService.generateAndStoreOtp("test@example.com")).thenThrow(new RuntimeException("Error generating OTP"));

        assertThrows(RuntimeException.class, () -> authService.generateOtp("test@example.com"));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(),user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");

        String result = authService.resetPassword(user.getEmail(), request);
        assertEquals("Password reset successful!", result);
        verify(userRepository, times(1)).save(user);
    }



    @Test
    void testResetPassword_EmailNotFound() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        String result = authService.resetPassword("test@example.com", request);
        assertEquals("Email not registered!", result);
    }

    @Test
    void testResetPassword_UserNotFound() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        String result = authService.resetPassword("test@example.com", request);
        assertEquals("Email not registered!", result);
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(request.getEmail(), request.getOtp())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");

        String result = authService.forgotPassword(request);
        assertEquals("Password reset successful!", result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testForgotPassword_EmailNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        String result = authService.forgotPassword(request);
        assertEquals("Email not registered!", result);
    }

    @Test
    void testForgotPassword_InvalidOtp() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("000000");
        request.setNewPassword("newPassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(request.getEmail(), request.getOtp())).thenReturn(false);

        String result = authService.forgotPassword(request);
        assertEquals("Invalid or expired OTP!", result);
    }
}
