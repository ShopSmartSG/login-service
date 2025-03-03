package sg.edu.nus.iss.login_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.repository.OtpRepository;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @InjectMocks
    private OtpService otpService;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailService emailService;

    private Otp otp;

    @BeforeEach
    void setUp() {
        otp = new Otp("test@example.com", "123456", LocalDateTime.now().plusMinutes(3));
    }

    @Test
    void testGenerateAndStoreOtp_NewOtp() {
        when(otpRepository.findByEmail("test@example.com")).thenReturn(null);

        when(otpRepository.save(any(Otp.class))).thenReturn(null);
        when(emailService.sendOtpEmail(eq("test@example.com"), anyString())).thenReturn(null);

        String response = otpService.generateAndStoreOtp("test@example.com");

        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendOtpEmail(eq("test@example.com"), anyString());

        assertEquals("OTP sent successfully to test@example.com", response);
    }

    @Test
    void testGenerateAndStoreOtp_BlockingCase() {
        Otp otp = mock(Otp.class);
        when(otp.isBlocked()).thenReturn(true);
        when(otp.getBlockedUntil()).thenReturn(LocalDateTime.now().plusMinutes(3));
        when(otpRepository.findByEmail("test@example.com")).thenReturn(otp);

        OtpException exception = assertThrows(OtpException.class, () -> {
            otpService.generateAndStoreOtp("test@example.com");
        });

        assertEquals("You are blocked from generating OTP. Try again after " +
                        otp.getBlockedUntil().minusMinutes(LocalDateTime.now().getMinute()).getMinute() + " minutes.",
                exception.getMessage());
    }

    @Test
    void testGenerateAndStoreOtp_ExistingOtp() {
        // Existing OTP is not blocked or expired
        when(otpRepository.findByEmail("test@example.com")).thenReturn(otp);
        when(otpRepository.save(any(Otp.class))).thenReturn(null);
        when(emailService.sendOtpEmail(eq("test@example.com"), anyString())).thenReturn(null);

        String response = otpService.generateAndStoreOtp("test@example.com");

        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendOtpEmail(eq("test@example.com"), anyString());

        assertEquals("OTP sent successfully to test@example.com", response);
    }

    @Test
    void testGenerateAndStoreOtp_ExistingOtp_WithBlockedState() {
        // Mock an OTP that is blocked
        otp.setBlocked(true);
        otp.setBlockedUntil(LocalDateTime.now().plusMinutes(5));
        when(otpRepository.findByEmail("test@example.com")).thenReturn(otp);

        OtpException exception = assertThrows(OtpException.class, () -> {
            otpService.generateAndStoreOtp("test@example.com");
        });

        assertEquals("You are blocked from generating OTP. Try again after " +
                        otp.getBlockedUntil().minusMinutes(LocalDateTime.now().getMinute()).getMinute() + " minutes.",
                exception.getMessage());
    }

    @Test
    void testValidateOtp_Success() {
        otp.setBlocked(false);
        otp.setExpirationTime(LocalDateTime.now().plusMinutes(3));
        when(otpRepository.findByEmail("test@example.com")).thenReturn(otp);

        boolean isValid = otpService.validateOtp("test@example.com", "123456");

        assertTrue(isValid);
        verify(otpRepository, times(1)).delete(otp);
    }

    @Test
    void testValidateOtp_Failure_Expired() {
        otp.setExpirationTime(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmail("test@example.com")).thenReturn(otp);

        OtpException exception = assertThrows(OtpException.class, () -> {
            otpService.validateOtp("test@example.com", "123456");
        });

        assertEquals("OTP has expired.", exception.getMessage());
    }

    @Test
    void testValidateOtp_Failure_InvalidOtp() {
        otp.setBlocked(false);
        otp.setExpirationTime(LocalDateTime.now().plusMinutes(3));
        when(otpRepository.findByEmail("test@example.com")).thenReturn(otp);

        OtpException exception = assertThrows(OtpException.class, () -> {
            otpService.validateOtp("test@example.com", "wrongotp");
        });

        assertEquals("Invalid OTP. Attempt 1 of 3.", exception.getMessage());
    }

    @Test
    void testValidateOtp_Failure_NoOtpFound() {
        when(otpRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        OtpException exception = assertThrows(OtpException.class, () -> {
            otpService.validateOtp("nonexistent@example.com", "123456");
        });

        assertEquals("No OTP found for this email.", exception.getMessage());
    }
}
