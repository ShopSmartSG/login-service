package sg.edu.nus.iss.login_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotEmpty(message = "Otp is required be empty")
    private String otp;

    @NotEmpty(message = "New password cannot be empty")
    private String newPassword;
}
