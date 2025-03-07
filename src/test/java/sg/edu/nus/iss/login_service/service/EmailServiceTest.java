package sg.edu.nus.iss.login_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private final String testEmail = "test@example.com";
    private final String testOtp = "123456";

    @Test
    void testSendOtpEmailForCustomer_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        SimpleMailMessage message = emailService.sendOtpEmailForCustomer(testEmail, testOtp);

        assertEquals("Your Customer Dashboard OTP", message.getSubject());
        assertEquals("Your OTP for accessing the Customer Dashboard is: " + testOtp, message.getText());
        assertArrayEquals(new String[]{testEmail}, message.getTo());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmailForMerchant_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        SimpleMailMessage message = emailService.sendOtpEmailForMerchant(testEmail, testOtp);

        assertEquals("Your Merchant Dashboard OTP", message.getSubject());
        assertEquals("Your OTP for accessing the Merchant Dashboard is: " + testOtp, message.getText());
        assertArrayEquals(new String[]{testEmail}, message.getTo());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmailForDeliveryPartner_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        SimpleMailMessage message = emailService.sendOtpEmailForDeliveryPartner(testEmail, testOtp);

        assertEquals("Your Delivery Partner Dashboard OTP", message.getSubject());
        assertEquals("Your OTP for accessing the Delivery Partner Dashboard is: " + testOtp, message.getText());
        assertArrayEquals(new String[]{testEmail}, message.getTo());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmailForCustomer_Failure() {
        doThrow(new MailException("SMTP error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendOtpEmailForCustomer(testEmail, testOtp));

        assertTrue(exception.getMessage().contains("Error sending OTP to email"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmailForMerchant_Failure() {
        doThrow(new MailException("SMTP error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendOtpEmailForMerchant(testEmail, testOtp));

        assertTrue(exception.getMessage().contains("Error sending OTP to email"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmailForDeliveryPartner_Failure() {
        doThrow(new MailException("SMTP error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendOtpEmailForDeliveryPartner(testEmail, testOtp));

        assertTrue(exception.getMessage().contains("Error sending OTP to email"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
