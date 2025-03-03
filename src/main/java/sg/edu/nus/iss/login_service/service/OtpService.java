package sg.edu.nus.iss.login_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.repository.OtpRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private EmailService emailService;

    @Autowired
    public OtpService(OtpRepository otpRepository, JavaMailSender mailSender, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
        this.emailService = emailService;

    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(999999));
    }

    public String generateAndStoreOtp(String email) {
        Otp existingOtp = otpRepository.findByEmail(email);

        if (existingOtp != null) {
            if (existingOtp.isBlocked()) {
                throw new OtpException("You are blocked from generating OTP. Try again after " +
                        existingOtp.getBlockedUntil().minusMinutes(LocalDateTime.now().getMinute()).getMinute() + " minutes.",
                        HttpStatus.FORBIDDEN);
            }

            existingOtp.setCode(generateOtp());
            logger.info("OTP expiration time: " + existingOtp.getExpirationTime() + ", Current time: " + LocalDateTime.now());
            existingOtp.setExpirationTime(LocalDateTime.now().plusMinutes(3));
            existingOtp.setFailedAttempts(0);
            existingOtp.setBlocked(false);
            existingOtp.setBlockedUntil(null);
            otpRepository.save(existingOtp);
            mailSender.send(emailService.sendOtpEmail(email, existingOtp.getCode()));
        } else {
            Otp newOtp = new Otp(email, generateOtp(), LocalDateTime.now().plusMinutes(3));
            otpRepository.save(newOtp);
            emailService.sendOtpEmail(email, newOtp.getCode());
        }

        return "OTP sent successfully to " + email;
    }


    public boolean validateOtp(String email, String inputOtp) {
        Otp storedOtp = otpRepository.findByEmail(email);

        if (storedOtp == null) throw new OtpException("No OTP found for this email.", HttpStatus.NOT_FOUND);
        if (storedOtp.isExpired()) throw new OtpException("OTP has expired.", HttpStatus.GONE);
        if (storedOtp.isBlocked()) throw new OtpException("You are blocked from validating OTP. Try after 15 minutes.", HttpStatus.FORBIDDEN);

        if (storedOtp.getCode().equals(inputOtp)) {
            otpRepository.delete(storedOtp);
            return true; // ✅ Correct return type
        } else {
            storedOtp.incrementAttempts();
            otpRepository.save(storedOtp);
            logger.error("Invalid OTP. Attempt " + storedOtp.getFailedAttempts() + " of 3.");
            throw new OtpException("Invalid OTP. Attempt " + storedOtp.getFailedAttempts() + " of 3.", HttpStatus.UNAUTHORIZED);
        }
    }

}
