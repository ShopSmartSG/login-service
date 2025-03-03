package sg.edu.nus.iss.login_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SimpleMailMessage simpleMailMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendOtpEmail_Success() {
        String toEmail = "test@example.com";
        String otp = "123456";

        // Mocking the behavior of mailSender.send() to ensure it doesn't send emails
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Call the method
        SimpleMailMessage message = emailService.sendOtpEmail(toEmail, otp);

        // Verify that mailSender.send() was called once
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        // Validate that the message was constructed correctly
        assertEquals(toEmail, message.getTo()[0]);
        assertEquals("Your OTP Code", message.getSubject());
        assertEquals("Your OTP is: " + otp, message.getText());
    }

    @Test
    void testSendOtpEmail_Failure() {
        String toEmail = "test@example.com";
        String otp = "123456";

        // Simulate an exception when sending the email
        doThrow(new RuntimeException("Email sending failed")).when(mailSender).send(any(SimpleMailMessage.class));

        // Call the method and verify that it throws the expected exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendOtpEmail(toEmail, otp);
        });

        assertEquals("Error sending OTP to email: " + toEmail, exception.getMessage());
    }
}
