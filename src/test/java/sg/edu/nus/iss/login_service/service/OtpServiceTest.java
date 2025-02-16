package sg.edu.nus.iss.login_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.repository.OtpRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateOtp_Success() {
        String email = "test@example.com";
        Otp mockOtp = new Otp(email, "123456", LocalDateTime.now().plusMinutes(5));

        when(otpRepository.save(any(Otp.class))).thenReturn(mockOtp);

        Otp generatedOtp = otpService.generateOtp(email);

        assertNotNull(generatedOtp);
        assertEquals(email, generatedOtp.getEmail());
        assertEquals(6, generatedOtp.getCode().length());
        verify(otpRepository, times(1)).save(any(Otp.class));
    }

    @Test
    void testValidateOtp_Success() {
        String email = "test@example.com";
        String validOtp = "123456";
        Otp mockOtp = new Otp(email, validOtp, LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(mockOtp));

        boolean isValid = otpService.validateOtp(email, validOtp);

        assertTrue(isValid);
        verify(otpRepository, times(1)).delete(mockOtp);
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        String email = "test@example.com";
        String validOtp = "123456";
        String invalidOtp = "654321";
        Otp mockOtp = new Otp(email, validOtp, LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(mockOtp));

        boolean isValid = otpService.validateOtp(email, invalidOtp);

        assertFalse(isValid);
        verify(otpRepository, never()).delete(any(Otp.class));
    }

    @Test
    void testValidateOtp_ExpiredOtp() {
        String email = "test@example.com";
        String validOtp = "123456";
        Otp expiredOtp = new Otp(email, validOtp, LocalDateTime.now().minusMinutes(1)); // OTP expired

        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(expiredOtp));

        boolean isValid = otpService.validateOtp(email, validOtp);

        assertFalse(isValid);
        verify(otpRepository, never()).delete(any(Otp.class));
    }

    @Test
    void testValidateOtp_NoOtpFound() {
        String email = "test@example.com";

        when(otpRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean isValid = otpService.validateOtp(email, "123456");

        assertFalse(isValid);
        verify(otpRepository, never()).delete(any(Otp.class));
    }
}
