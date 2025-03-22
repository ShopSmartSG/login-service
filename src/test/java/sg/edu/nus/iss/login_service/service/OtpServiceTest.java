package sg.edu.nus.iss.login_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.repository.OtpRepository;
import sg.edu.nus.iss.login_service.util.LogMaskingUtil;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    @Mock
    private LogMaskingUtil logMaskingUtil;

    private final String testEmail = "test@example.com";
    private final ProfileType profileType = ProfileType.CUSTOMER;
    private Otp validOtp;

    @BeforeEach
    void setUp() {
        validOtp = new Otp(testEmail, "123456", LocalDateTime.now().plusMinutes(3));
        validOtp.setProfileType(profileType);
    }

    @Test
    void testGenerateOtp() {
        String otp = otpService.generateOtp();
        assertEquals(6, otp.length()); // Ensures 6-digit OTP is generated
    }

    @Test
    void testGenerateAndStoreOtp_NewOtp() {
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(null);
        when(emailService.sendOtpEmailForCustomer(anyString(), anyString())).thenReturn(mock(SimpleMailMessage.class));

        String response = otpService.generateAndStoreOtp(testEmail, profileType);

        assertEquals("OTP sent successfully to " + testEmail, response);
        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendOtpEmailForCustomer(anyString(), anyString());
    }

    @Test
    void testGenerateAndStoreOtp_ExistingOtp() {
        validOtp.setBlocked(false);
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);
        when(emailService.sendOtpEmailForCustomer(anyString(), anyString())).thenReturn(mock(SimpleMailMessage.class));

        String response = otpService.generateAndStoreOtp(testEmail, profileType);

        assertEquals("OTP sent successfully to " + testEmail, response);
        assertNotNull(validOtp.getExpirationTime());
        verify(otpRepository, times(1)).save(validOtp);
        verify(emailService, times(1)).sendOtpEmailForCustomer(anyString(), anyString());
    }

    @Test
    void testGenerateAndStoreOtp_BlockedUser() {
        validOtp.setBlocked(true);
        validOtp.setBlockedUntil(LocalDateTime.now().plusMinutes(10));
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.generateAndStoreOtp(testEmail, profileType));

        assertEquals("You're blocked from generating OTP", exception.getMessage().substring(0, 34)); // Partial match to handle time variation
    }

    @Test
    void testValidateOtp_Successful() {
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        boolean result = otpService.validateOtp(testEmail, "123456", profileType);

        assertTrue(result);
        verify(otpRepository, times(1)).delete(validOtp);
    }

    @Test
    void testValidateOtp_OtpNotFound() {
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(null);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.validateOtp(testEmail, "123456", profileType));

        assertEquals("No OTP found for this email.", exception.getMessage());
    }

    @Test
    void testValidateOtp_ProfileTypeMismatch() {
        validOtp.setProfileType(ProfileType.MERCHANT);
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.validateOtp(testEmail, "123456", profileType));

        assertEquals("OTP does not match the profile type.", exception.getMessage());
    }

    @Test
    void testValidateOtp_ExpiredOtp() {
        validOtp.setExpirationTime(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.validateOtp(testEmail, "123456", profileType));

        assertEquals("OTP has expired.", exception.getMessage());
    }

    @Test
    void testValidateOtp_BlockedOtp() {
        validOtp.setBlocked(true);
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.validateOtp(testEmail, "123456", profileType));

        assertEquals("You are blocked from validating OTP. Try after 15 minutes.", exception.getMessage());
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        validOtp.setCode("654321");
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.validateOtp(testEmail, "123456", profileType));

        assertEquals("Invalid OTP. Attempt 1 of 3.", exception.getMessage());
        verify(otpRepository, times(1)).save(validOtp);
    }

    @Test
    void testValidateOtp_MultipleFailedAttempts() {
        validOtp.setCode("654321");
        validOtp.setFailedAttempts(2);
        when(otpRepository.findByEmailAndProfileType(testEmail, profileType)).thenReturn(validOtp);

        OtpException exception = assertThrows(OtpException.class, () -> otpService.validateOtp(testEmail, "123456", profileType));

        assertEquals("Invalid OTP. Attempt 3 of 3.", exception.getMessage());
        assertEquals(3, validOtp.getFailedAttempts());
        verify(otpRepository, times(1)).save(validOtp);
    }
}
