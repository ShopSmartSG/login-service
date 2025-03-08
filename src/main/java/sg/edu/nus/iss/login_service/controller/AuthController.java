package sg.edu.nus.iss.login_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.login_service.dto.*;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.service.AuthService;
import sg.edu.nus.iss.login_service.service.OtpService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    private final OtpService otpService;

    @Autowired
    public AuthController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

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

    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponse> generateOtp(@RequestParam String email, @RequestParam ProfileType profileType) {
        try {
            logger.debug("Generating OTP for email: {} with profileType: {}", email, profileType);
            String response = authService.generateOtp(email, profileType);
            logger.debug("OTP generated successfully for email: {} with response: {}", email, response);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (Exception e) {
            logger.error("Error generating OTP for email: {} with profileType: {} with exception : ", email, profileType, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            String response = authService.loginUser(request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (OtpException.InvalidCredentialsException e) {
            // Return 401 Unauthorized for invalid credentials (incorrect password/OTP)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        } catch (OtpException.AccountLockedException e) {
            // Return 423 Locked for account locked due to failed attempts
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new ApiResponse(HttpStatus.LOCKED.value(), e.getMessage()));
        } catch (Exception e) {
            // Return 400 Bad Request for any other errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String response = authService.forgotPassword(request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (OtpException.EmailNotFoundException e) {
            // Return 404 Not Found if the email is not registered
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (OtpException.InvalidOtpException e) {
            // Return 422 Unprocessable Entity if OTP is invalid
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ApiResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage()));
        } catch (Exception e) {
            // Return 400 Bad Request for other issues
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<ApiResponse> validateOtp(@RequestBody OtpRequest otpRequest) {
        try {
            logger.debug("Validating OTP for email: {} with profileType: {} and otp: {}",
                    otpRequest.getEmail(), otpRequest.getProfileType(), otpRequest.getOtp());
            boolean isValid = otpService.validateOtp(otpRequest.getEmail(), otpRequest.getOtp(), otpRequest.getProfileType());
            if (isValid) {
                logger.debug("OTP validated successfully for email: {}", otpRequest.getEmail());
                return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "OTP validated successfully"));
            } else {
                logger.debug("Invalid OTP provided for email: {}", otpRequest.getEmail());
                // In case the service method doesn't throw an exception but returns false
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(new ApiResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Invalid OTP"));
            }
        } catch (OtpException.InvalidOtpException e) {
            logger.error("Invalid OTP provided for email: {} with exception: ", otpRequest.getEmail(), e);
            // Return 422 Unprocessable Entity if OTP is invalid or expired
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ApiResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Error validating OTP for email: {} with exception: ", otpRequest.getEmail(), e);
            // Return 400 Bad Request for any other errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String response = authService.resetPassword(request);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), response));
        } catch (OtpException.OldPasswordIncorrectException e) {
            // Return 422 Unprocessable Entity if the old password is incorrect
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ApiResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage()));
        } catch (OtpException.EmailNotFoundException e) {
            // Return 404 Not Found if the email is not registered
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            // Return 400 Bad Request for other errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

}
