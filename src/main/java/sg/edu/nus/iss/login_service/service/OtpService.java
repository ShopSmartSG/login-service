package sg.edu.nus.iss.login_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import sg.edu.nus.iss.login_service.exception.OtpException;
import sg.edu.nus.iss.login_service.repository.OtpRepository;
import sg.edu.nus.iss.login_service.util.LogMaskingUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private EmailService emailService;
    private LogMaskingUtil logMaskingUtil;

    @Autowired
    public OtpService(OtpRepository otpRepository, JavaMailSender mailSender, EmailService emailService, LogMaskingUtil logMaskingUtil) {
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
        this.emailService = emailService;
        this.logMaskingUtil = logMaskingUtil;

    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendOtpEmailBasedOnProfileType(String email, String otpCode, ProfileType profileType) {
        if (profileType == ProfileType.CUSTOMER) {
            mailSender.send(emailService.sendOtpEmailForCustomer(email, otpCode));
        } else if (profileType == ProfileType.MERCHANT) {
            mailSender.send(emailService.sendOtpEmailForMerchant(email, otpCode));
        } else if (profileType == ProfileType.DELIVERY) {
            mailSender.send(emailService.sendOtpEmailForDeliveryPartner(email, otpCode));
        }
    }

    public String generateAndStoreOtp(String email, ProfileType profileType) {
        Otp existingOtp = otpRepository.findByEmailAndProfileType(email, profileType);

        if (existingOtp != null) {
            if (existingOtp.isBlocked()) {
                throw new OtpException("You're blocked from generating OTP. Try again after " +
                        existingOtp.getBlockedUntil().minusMinutes(LocalDateTime.now().getMinute()).getMinute() + " minutes.",
                        HttpStatus.FORBIDDEN);
            }

            existingOtp.setCode(generateOtp());
            logger.info("OTP expiration time: " + existingOtp.getExpirationTime() + ", Current time: " + LocalDateTime.now());
            existingOtp.setExpirationTime(LocalDateTime.now().plusMinutes(3));
            existingOtp.setFailedAttempts(0);
            existingOtp.setBlocked(false);
            existingOtp.setBlockedUntil(null);
            existingOtp.setProfileType(profileType);
            otpRepository.save(existingOtp);
            sendOtpEmailBasedOnProfileType(email, existingOtp.getCode(), profileType);
        } else {
            Otp newOtp = new Otp(email, generateOtp(), LocalDateTime.now().plusMinutes(3));
            newOtp.setProfileType(profileType);  // Make sure profileType is set here
            otpRepository.save(newOtp);

            // Send customized email based on profileType
            sendOtpEmailBasedOnProfileType(email, newOtp.getCode(), profileType);
        }
        logger.info("OTP sent successfully to {}", logMaskingUtil.maskEmail(email));
        return "OTP sent successfully to " + email;
    }

    public boolean validateOtp(String email, String inputOtp, ProfileType profileType) {
        Otp storedOtp = otpRepository.findByEmailAndProfileType(email, profileType);

        if (storedOtp == null) throw new OtpException("No OTP found for this email.", HttpStatus.NOT_FOUND);
        if (!storedOtp.getProfileType().equals(profileType)) throw new OtpException("OTP does not match the profile type.", HttpStatus.BAD_REQUEST);
        if (storedOtp.isExpired()) throw new OtpException("OTP has expired.", HttpStatus.GONE);
        if (storedOtp.isBlocked()) throw new OtpException("You are blocked from validating OTP. Try after 15 minutes.", HttpStatus.FORBIDDEN);

        if (storedOtp.getCode().equals(inputOtp)) {
            otpRepository.delete(storedOtp);
            return true;
        } else {
            storedOtp.incrementAttempts();
            otpRepository.save(storedOtp);
            logger.error("Invalid OTP. Attempt " + storedOtp.getFailedAttempts() + " of 3.");
            throw new OtpException("Invalid OTP. Attempt " + storedOtp.getFailedAttempts() + " of 3.", HttpStatus.UNAUTHORIZED);
        }
    }
}
