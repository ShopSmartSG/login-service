package sg.edu.nus.iss.login_service.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sg.edu.nus.iss.login_service.dto.*;
import sg.edu.nus.iss.login_service.service.AuthService;
import sg.edu.nus.iss.login_service.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");

        doAnswer(invocation -> null).when(authService).registerUser(any(RegisterRequest.class));


        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        when(authService.loginUser(any(LoginRequest.class))).thenReturn("true");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful!"));
    }

    @Test
    void testLoginUser_Failure() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");
        when(authService.loginUser(any(LoginRequest.class))).thenReturn("false");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials!"));
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com", "newpassword123");
        doAnswer(invocation -> null).when(otpService).generateOtp(anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email."));
    }

    @Test
    void testValidateOtp_Success() throws Exception {
        OtpRequest request = new OtpRequest("john@example.com", "123456");
        when(otpService.validateOtp(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP validated successfully!"));
    }

    @Test
    void testValidateOtp_Failure() throws Exception {
        OtpRequest request = new OtpRequest("john@example.com", "654321");
        when(otpService.validateOtp(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired OTP."));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com", "newpassword123");
        when(authService.resetPassword(anyString(), anyString())).thenReturn("Password updated successfully!");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully."));
    }

    @Test
    void testResetPassword_Failure() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com", "weakpass");
        when(authService.resetPassword(anyString(), anyString())).thenReturn("Password is too weak");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password is too weak"));
    }
}
