package sg.edu.nus.iss.login_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sg.edu.nus.iss.login_service.dto.*;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.service.AuthService;
import sg.edu.nus.iss.login_service.service.OtpService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initializes mocks
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setEmail("test@example.com");

        String mockResponse = "User registered successfully";

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(mockResponse);

        ResponseEntity<AuthController.ApiResponse> response = authController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody().getMessage());
    }

    @Test
    void testRegisterFailure() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setEmail("test@example.com");

        when(authService.registerUser(any(RegisterRequest.class))).thenThrow(new RuntimeException("Registration failed"));

        ResponseEntity<AuthController.ApiResponse> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Registration failed", response.getBody().getMessage());
    }

    @Test
    void testGenerateOtpSuccess() {
        String email = "test@example.com";
        ProfileType profileType = ProfileType.CUSTOMER;
        String mockResponse = "OTP generated successfully";

        when(authService.generateOtp(anyString(), any(ProfileType.class))).thenReturn(mockResponse);

        ResponseEntity<AuthController.ApiResponse> response = authController.generateOtp(email, profileType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OTP generated successfully", response.getBody().getMessage());
    }

    @Test
    void testGenerateOtpFailure() {
        String email = "test@example.com";
        ProfileType profileType = ProfileType.CUSTOMER;

        when(authService.generateOtp(anyString(), any(ProfileType.class))).thenThrow(new RuntimeException("OTP generation failed"));

        ResponseEntity<AuthController.ApiResponse> response = authController.generateOtp(email, profileType);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("OTP generation failed", response.getBody().getMessage());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setOtp("123456");

        String mockResponse = "Login successful";

        when(authService.loginUser(any(LoginRequest.class))).thenReturn(mockResponse);

        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody().getMessage());
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");
        request.setOtp("123456");

        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new OtpException.InvalidCredentialsException("Invalid credentials! Incorrect password."));

        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials! Incorrect password.", response.getBody().getMessage());
    }

    @Test
    void testForgotPasswordSuccess() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        String mockResponse = "Password reset link sent";

        when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(mockResponse);

        ResponseEntity<AuthController.ApiResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset link sent", response.getBody().getMessage());
    }

    @Test
    void testForgotPasswordFailure() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenThrow(new RuntimeException("Failed to send reset link"));

        ResponseEntity<AuthController.ApiResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to send reset link", response.getBody().getMessage());
    }

    @Test
    void testValidateOtpSuccess() {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail("test@example.com");
        otpRequest.setOtp("123456");
        otpRequest.setProfileType(ProfileType.CUSTOMER);

        when(otpService.validateOtp(anyString(), anyString(), any(ProfileType.class))).thenReturn(true);

        ResponseEntity<AuthController.ApiResponse> response = authController.validateOtp(otpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OTP validated successfully", response.getBody().getMessage());
    }


    @Test
    void testValidateOtpFailure() {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail("test@example.com");
        otpRequest.setOtp("123456");

        when(otpService.validateOtp(anyString(), anyString(), any(ProfileType.class))).thenThrow(new OtpException.InvalidOtpException("Invalid OTP"));

        ResponseEntity<AuthController.ApiResponse> response = authController.validateOtp(otpRequest);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid OTP", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordSuccess() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");
        String mockResponse = "Password reset successfully";

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(mockResponse);

        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordFailureOldPasswordIncorrect() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenThrow(new OtpException.OldPasswordIncorrectException("Old password is incorrect"));

        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword(request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Old password is incorrect", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordFailure() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenThrow(new RuntimeException("Failed to reset password"));

        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to reset password", response.getBody().getMessage());
    }

    @Test
    void testValidateOtpFailureWithoutException() {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail("test@example.com");
        otpRequest.setOtp("123456");
        otpRequest.setProfileType(ProfileType.CUSTOMER);

        // Mock service to return false instead of throwing exception
        when(otpService.validateOtp(anyString(), anyString(), any(ProfileType.class))).thenReturn(false);

        ResponseEntity<AuthController.ApiResponse> response = authController.validateOtp(otpRequest);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid OTP", response.getBody().getMessage());
    }

    @Test
    void testRegisterFailureUnexpectedException() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setEmail("test@example.com");

        when(authService.registerUser(any(RegisterRequest.class))).thenThrow(new NullPointerException("Unexpected error"));

        ResponseEntity<AuthController.ApiResponse> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void testGenerateOtpFailureUnexpectedException() {
        String email = "test@example.com";
        ProfileType profileType = ProfileType.CUSTOMER;

        when(authService.generateOtp(anyString(), any(ProfileType.class))).thenThrow(new NullPointerException("Unexpected error"));

        ResponseEntity<AuthController.ApiResponse> response = authController.generateOtp(email, profileType);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void testForgotPasswordFailureUnexpectedException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenThrow(new NullPointerException("Unexpected error"));

        ResponseEntity<AuthController.ApiResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordFailureUnexpectedException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenThrow(new NullPointerException("Unexpected error"));

        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void testLoginFailureAccountLockedException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setOtp("123456");

        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new OtpException.AccountLockedException("Account locked due to failed attempts", request.getEmail()));

        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertEquals("Account locked due to failed attempts", response.getBody().getMessage());
    }

    @Test
    void testForgotPasswordFailureEmailNotFoundException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenThrow(new OtpException.EmailNotFoundException("Email not found"));

        ResponseEntity<AuthController.ApiResponse> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Email not found", response.getBody().getMessage());
    }

    @Test
    void testValidateOtpUnexpectedException() {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail("test@example.com");
        otpRequest.setOtp("123456");
        otpRequest.setProfileType(ProfileType.CUSTOMER);

        when(otpService.validateOtp(anyString(), anyString(), any(ProfileType.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<AuthController.ApiResponse> response = authController.validateOtp(otpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }

    @Test
    void testGenerateOtpFailureInvalidOtpException() {
        String email = "test@example.com";
        ProfileType profileType = ProfileType.CUSTOMER;

        when(authService.generateOtp(anyString(), any(ProfileType.class)))
                .thenThrow(new OtpException.InvalidOtpException("Invalid OTP"));

        ResponseEntity<AuthController.ApiResponse> response = authController.generateOtp(email, profileType);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid OTP", response.getBody().getMessage());
    }


}
