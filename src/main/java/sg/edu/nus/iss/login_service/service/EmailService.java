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

    public SimpleMailMessage sendOtpEmailForCustomer(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            logger.info("Sending OTP for Customer to email");
            message.setTo(toEmail);
            message.setSubject("Your Customer Dashboard OTP");
            message.setText("Your OTP for accessing the Customer Dashboard is: " + otp);
            mailSender.send(message);
            logger.info("OTP email sent to {}", toEmail);
            return message;
        } catch (Exception e) {
            logger.error("Error sending OTP for Customer to email");
            throw new RuntimeException("Error sending OTP to email. " + e);
        }
    }

    public SimpleMailMessage sendOtpEmailForMerchant(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            logger.info("Sending OTP for Merchant to email");
            message.setTo(toEmail);
            message.setSubject("Your Merchant Dashboard OTP");
            message.setText("Your OTP for accessing the Merchant Dashboard is: " + otp);
            mailSender.send(message);
            logger.info("OTP email sent to {}", toEmail);
            return message;
        } catch (Exception e) {
            logger.error("Error sending OTP for Merchant to email");
            throw new RuntimeException("Error sending OTP to email. " + e);
        }
    }

    public SimpleMailMessage sendOtpEmailForDeliveryPartner(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            logger.info("Sending OTP for Delivery Partner to email");
            message.setTo(toEmail);
            message.setSubject("Your Delivery Partner Dashboard OTP");
            message.setText("Your OTP for accessing the Delivery Partner Dashboard is: " + otp);
            mailSender.send(message);
            logger.info("OTP email sent to {}", toEmail);
            return message;
        } catch (Exception e) {
            logger.error("Error sending OTP for Delivery Partnerr to email");
            throw new RuntimeException("Error sending OTP to email. " + e);
        }
    }
}
