package sg.edu.nus.iss.login_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sg.edu.nus.iss.login_service.dto.*;
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
        request.setFullName("testUser");
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
        request.setFullName("testUser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        when(authService.registerUser(any(RegisterRequest.class))).thenThrow(new RuntimeException("Registration failed"));

        ResponseEntity<AuthController.ApiResponse> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Registration failed", response.getBody().getMessage());
    }

    @Test
    void testLoginSuccess() {
        // Create a login request with valid credentials
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setOtp("123456");

        String mockResponse = "Login successful";

        // Mock the authService method to return success response
        when(authService.loginUser(any(LoginRequest.class))).thenReturn(mockResponse);

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        // Assert the success response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody().getMessage());
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // Create a login request with invalid credentials
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");
        request.setOtp("123456");

        // Mock the authService method to throw InvalidCredentialsException
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new OtpException.InvalidCredentialsException("Invalid credentials! Incorrect password."));

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        // Assert the failure response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials! Incorrect password.", response.getBody().getMessage());
    }

    @Test
    void testLoginFailureAccountLocked() {
        // Create a login request with valid credentials
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setOtp("123456");

        // Mock the authService method to throw AccountLockedException
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new OtpException.AccountLockedException("Account locked! Try again later."));

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        // Assert the failure response
        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertEquals("Account locked! Try again later.", response.getBody().getMessage());
    }

    @Test
    void testLoginFailureInvalidOtp() {
        // Create a login request with valid credentials but invalid OTP
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setOtp("wrongOtp");

        // Mock the authService method to throw InvalidOtpException
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new OtpException.InvalidOtpException("Invalid OTP!"));

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.login(request);

        // Assert the failure response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid OTP!", response.getBody().getMessage());
    }


    @Test
    void testGenerateOtpSuccess() {
        String email = "test@example.com";
        String mockResponse = "OTP generated successfully";

        when(authService.generateOtp(email)).thenReturn(mockResponse);

        ResponseEntity<AuthController.ApiResponse> response = authController.generateOtp(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OTP generated successfully", response.getBody().getMessage());
    }

    @Test
    void testGenerateOtpFailure() {
        String email = "test@example.com";
        when(authService.generateOtp(email)).thenThrow(new RuntimeException("Failed to generate OTP"));

        ResponseEntity<AuthController.ApiResponse> response = authController.generateOtp(email);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to generate OTP", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordSuccess() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");
        String mockResponse = "Password reset successfully";

        // Mock the successful response from the service layer
        when(authService.resetPassword(anyString(), any(ResetPasswordRequest.class))).thenReturn(mockResponse);

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword("test@example.com", request);

        // Assert that the response status is 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordFailure() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        // Mock a RuntimeException thrown by the service for a failed password reset
        when(authService.resetPassword(anyString(), any(ResetPasswordRequest.class))).thenThrow(new RuntimeException("Failed to reset password"));

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword("test@example.com", request);

        // Assert that the response status is 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to reset password", response.getBody().getMessage());
    }

    @Test
    void testResetPasswordOldPasswordIncorrect() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setNewPassword("newPassword123");

        // Mock the UnprocessableEntityException thrown by the service for incorrect old password
        when(authService.resetPassword(anyString(), any(ResetPasswordRequest.class)))
                .thenThrow(new OtpException.UnprocessableEntityException("Old password is incorrect!"));

        // Call the controller method
        ResponseEntity<AuthController.ApiResponse> response = authController.resetPassword("test@example.com", request);

        // Assert that the response status is 422 Unprocessable Entity for incorrect old password
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Old password is incorrect!", response.getBody().getMessage());
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

        when(otpService.validateOtp(anyString(), anyString())).thenReturn(true);

        ResponseEntity<AuthController.ApiResponse> response = authController.validateOtp(otpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OTP validated successfully", response.getBody().getMessage());
    }

    @Test
    void testValidateOtpFailure() {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail("test@example.com");
        otpRequest.setOtp("123456");

        when(otpService.validateOtp(anyString(), anyString())).thenThrow(new RuntimeException("OTP validation failed"));

        ResponseEntity<AuthController.ApiResponse> response = authController.validateOtp(otpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("OTP validation failed", response.getBody().getMessage());
    }
}
