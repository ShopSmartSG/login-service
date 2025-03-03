package sg.edu.nus.iss.login_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.login_service.dto.ForgotPasswordRequest;
import sg.edu.nus.iss.login_service.dto.LoginRequest;
import sg.edu.nus.iss.login_service.dto.RegisterRequest;
import sg.edu.nus.iss.login_service.dto.ResetPasswordRequest;
import sg.edu.nus.iss.login_service.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Custom Response format with status code and message
    public static class ApiResponse {
        private int statusCode;
        private String message;

        public ApiResponse(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            String response = authService.registerUser(request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            String response = authService.loginUser(request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (Exception e) {
            // Return 401 Unauthorized for login failure with status code and message
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponse> generateOtp(@RequestParam String email) {
        try {
            String response = authService.generateOtp(email);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String email, @RequestBody ResetPasswordRequest request) {
        try {
            String response = authService.resetPassword(email, request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String response = authService.forgotPassword(request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
}
