package sg.edu.nus.iss.login_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sg.edu.nus.iss.login_service.dto.*;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.repository.UserRepository;
import sg.edu.nus.iss.login_service.service.AuthService;
import sg.edu.nus.iss.login_service.service.EmailService;
import sg.edu.nus.iss.login_service.service.OtpService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setProfileType(ProfileType.CUSTOMER);
        user.setFailedAttempts(0);
        user.setLocked(false);
        user.setLockExpiry(LocalDateTime.now().minusMinutes(5));
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        String result = authService.registerUser(request);

        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.of(user));

        String result = authService.registerUser(request);

        assertEquals("Email already registered!", result);
    }

    @Test
    void testLoginUser_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "password", "123456", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(otpService.validateOtp(anyString(), anyString(), any())).thenReturn(true);

        String result = authService.loginUser(request);

        assertEquals("Login successful!", result);
    }

    @Test
    void testLoginUser_EmailNotFound() {
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("test@example.com", "password", "123456", ProfileType.CUSTOMER);
        assertThrows(OtpException.InvalidCredentialsException.class, () -> authService.loginUser(request));
    }

    @Test
    void testLoginUser_InvalidOtp() {
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(anyString(), anyString(), any())).thenReturn(false);

        LoginRequest request = new LoginRequest("test@example.com", "password", "wrongOtp", ProfileType.CUSTOMER);
        assertThrows(OtpException.InvalidOtpException.class, () -> authService.loginUser(request));
    }

    @Test
    void testLoginUser_WrongPassword() {
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(anyString(), anyString(), any())).thenReturn(true);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword", "123456", ProfileType.CUSTOMER);
        assertThrows(OtpException.InvalidCredentialsException.class, () -> authService.loginUser(request));
    }

    @Test
    void testForgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com", "123456", "newPassword", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(anyString(), any())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(anyString(), anyString(), any())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        String result = authService.forgotPassword(request);

        assertEquals("Password reset successful!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testLoginUser_AccountLocked() {
        user.setLocked(true);
        user.setLockExpiry(LocalDateTime.now().plusMinutes(10));
        LoginRequest request = new LoginRequest("test@example.com", "password", "123456", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType())).thenReturn(Optional.of(user));

        assertThrows(OtpException.AccountLockedException.class, () -> authService.loginUser(request));
    }

    @Test
    void testGenerateOtp_Success() {
        when(otpService.generateAndStoreOtp(user.getEmail(), user.getProfileType())).thenReturn("123456");

        String otp = authService.generateOtp(user.getEmail(), user.getProfileType());

        assertEquals("123456", otp);
    }

    @Test
    void testGenerateOtp_Exception() {
        when(otpService.generateAndStoreOtp(user.getEmail(), user.getProfileType())).thenThrow(new RuntimeException("OTP Error"));

        assertThrows(RuntimeException.class, () -> authService.generateOtp(user.getEmail(), user.getProfileType()));
    }

    @Test
    void testResetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "oldPassword", "newPassword", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("newEncodedPassword");

        String response = authService.resetPassword(request);

        assertEquals("Password reset successful!", response);
    }

    @Test
    void testResetPassword_InvalidOldPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "wrongOldPassword", "newPassword", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        assertThrows(OtpException.UnprocessableEntityException.class, () -> authService.resetPassword(request));
    }

    @Test
    void testForgotPassword_InvalidOtp() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com", "wrongOtp", "newPassword", ProfileType.CUSTOMER);
        when(userRepository.findByEmailAndProfileType(request.getEmail(), request.getProfileType())).thenReturn(Optional.of(user));
        when(otpService.validateOtp(request.getEmail(), request.getOtp(), request.getProfileType())).thenReturn(false);

        assertThrows(OtpException.InvalidOtpException.class, () -> authService.forgotPassword(request));
    }
}
