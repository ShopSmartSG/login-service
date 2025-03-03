package sg.edu.nus.iss.login_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public SimpleMailMessage sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            logger.info("Sending OTP to email: " + toEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);
            mailSender.send(message);
            logger.info("OTP email sent to {}", toEmail);
            return message;
        } catch (Exception e){
            logger.error("Error sending OTP to email: " + toEmail);
            throw new RuntimeException("Error sending OTP to email: " + toEmail, e);
        }
    }
}
