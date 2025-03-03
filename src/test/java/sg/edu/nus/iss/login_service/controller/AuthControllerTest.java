package sg.edu.nus.iss.login_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sg.edu.nus.iss.login_service.dto.LoginRequest;
import sg.edu.nus.iss.login_service.dto.RegisterRequest;
import sg.edu.nus.iss.login_service.dto.ResetPasswordRequest;
import sg.edu.nus.iss.login_service.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("Ritu Thori", "abc@gmail.com" , "password123" );

        // Mocking the register service call
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenReturn("User registered successfully!");

        // Performing the test and expecting a 200 OK status with a correct response message
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(content().json("{\"statusCode\":200,\"message\":\"User registered successfully!\"}"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testRegister_Failure() throws Exception {
        RegisterRequest request = new RegisterRequest("Ritu Thori", "test@example.com", "password123");

        // Mocking the exception thrown during registration as RuntimeException
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Invalid registration details"));

        // Performing the test and expecting a 400 Bad Request status with the error message
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Expect 400 Bad Request
                .andExpect(content().json("{\"statusCode\":400,\"message\":\"Invalid registration details\"}"));

        verify(authService, times(1)).registerUser(any(RegisterRequest.class));
    }


    @Test
    void testLogin_Failure() throws Exception {
        // Setting up the login request with sample data
        LoginRequest request = new LoginRequest("test@example.com", "password123", "123456");

        // Mocking authService.loginUser to throw a RuntimeException for invalid credentials
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials!"));

        // Performing the mock request and expecting 401 Unauthorized status
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // Expect 401 Unauthorized
                .andExpect(jsonPath("$.statusCode").value(401))  // Check if statusCode is 401
                .andExpect(jsonPath("$.message").value("Invalid credentials!"));  // Check if message is correct

        verify(authService, times(1)).loginUser(any(LoginRequest.class));
    }



    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123" , "123456");

        // Mocking the successful login response
        when(authService.loginUser(any(LoginRequest.class)))
                .thenReturn("Login successful!");

        // Performing the mock request and expecting a 200 OK status with a success message
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(content().json("{\"statusCode\":200,\"message\":\"Login successful!\"}"));

        verify(authService, times(1)).loginUser(any(LoginRequest.class));
    }

    @Test
    void testGenerateOtp_Success() throws Exception {
        String email = "test@example.com";

        // Mocking OTP generation success
        when(authService.generateOtp(anyString()))
                .thenReturn("OTP sent successfully!");

        // Performing the mock request and expecting 200 OK status with success message
        mockMvc.perform(post("/auth/generate-otp")
                        .param("email", email))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(content().json("{\"statusCode\":200,\"message\":\"OTP sent successfully!\"}"));

        verify(authService, times(1)).generateOtp(anyString());
    }

    @Test
    void testGenerateOtp_Failure() throws Exception {
        String email = "test@example.com";

        // Mocking OTP generation failure with RuntimeException
        when(authService.generateOtp(anyString()))
                .thenThrow(new RuntimeException("Invalid email address"));

        // Performing the mock request and expecting 400 Bad Request status with error message
        mockMvc.perform(post("/auth/generate-otp")
                        .param("email", email))
                .andExpect(status().isBadRequest()) // Expect 400 Bad Request
                .andExpect(content().json("{\"statusCode\":400,\"message\":\"Invalid email address\"}"));

        verify(authService, times(1)).generateOtp(anyString());
    }


    @Test
    void testResetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("123456" , "newpassword123");
        String email = "test@example.com";

        // Mocking the password reset success
        when(authService.resetPassword(anyString(), any(ResetPasswordRequest.class)))
                .thenReturn("Password reset successfully!");

        // Performing the mock request and expecting 200 OK status
        mockMvc.perform(post("/auth/reset-password")
                        .param("email", email)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(content().json("{\"statusCode\":200,\"message\":\"Password reset successfully!\"}"));

        verify(authService, times(1)).resetPassword(anyString(), any(ResetPasswordRequest.class));
    }

    @Test
    void testResetPassword_Failure() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("123456", "newpassword123");
        String email = "test@example.com";

        // Mocking password reset failure with RuntimeException
        when(authService.resetPassword(anyString(), any(ResetPasswordRequest.class)))
                .thenThrow(new RuntimeException("Invalid reset request"));

        // Performing the mock request and expecting 400 Bad Request status with error message
        mockMvc.perform(post("/auth/reset-password")
                        .param("email", email)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Expect 400 Bad Request
                .andExpect(content().json("{\"statusCode\":400,\"message\":\"Invalid reset request\"}"));

        verify(authService, times(1)).resetPassword(anyString(), any(ResetPasswordRequest.class));
    }

}
