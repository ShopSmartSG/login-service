package sg.edu.nus.iss.login_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sg.edu.nus.iss.login_service.entity.ProfileType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "Profile type is required")
    private ProfileType profileType; // Added profileType field
}
