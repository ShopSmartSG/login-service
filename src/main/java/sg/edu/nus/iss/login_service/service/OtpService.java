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

    private void sendOtpEmailBasedOnProfileType(String email, String otpCode, ProfileType profileType) {
        logger.debug("Sending OTP email for user with profileType {}", profileType);
        if (profileType == ProfileType.CUSTOMER) {
            logger.debug("Sending OTP email for customer");
            mailSender.send(emailService.sendOtpEmailForCustomer(email, otpCode));
        } else if (profileType == ProfileType.MERCHANT) {
            logger.debug("Sending OTP email for merchant");
            mailSender.send(emailService.sendOtpEmailForMerchant(email, otpCode));
        } else if (profileType == ProfileType.DELIVERY) {
            logger.debug("Sending OTP email for delivery partner");
            mailSender.send(emailService.sendOtpEmailForDeliveryPartner(email, otpCode));
        }
    }

    public String generateAndStoreOtp(String email, ProfileType profileType) {
        logger.info("Starting service to generate OTP for {} for profileType {}", email, profileType);
        Otp existingOtp = otpRepository.findByEmailAndProfileType(email, profileType);
        logger.debug("existing OTP record from database is {}", existingOtp);
        if (existingOtp != null) {
            logger.info("Existing OTP found for user already with profileType {}", profileType);
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
            logger.debug("Updating existing OTP record with new OTP code and expiration time as {}", existingOtp);
            otpRepository.save(existingOtp);
            logger.debug("OTP record updated successfully for {} and triggering email now", email);
            sendOtpEmailBasedOnProfileType(email, existingOtp.getCode(), profileType);
        } else {
            logger.info("Starting Generating new OTP for {} and profileType {}", email, profileType);
            Otp newOtp = new Otp(email, generateOtp(), LocalDateTime.now().plusMinutes(3));
            newOtp.setProfileType(profileType);  // Make sure profileType is set here
            otpRepository.save(newOtp);

            // Send customized email based on profileType
            logger.info("Sending OTP email for user with profileType {}", profileType);
            sendOtpEmailBasedOnProfileType(email, newOtp.getCode(), profileType);
        }

        return "OTP sent successfully to " + email;
    }

    public boolean validateOtp(String email, String inputOtp, ProfileType profileType) {
        logger.info("Starting to validating OTP for {} for profileType {}", email, profileType);
        Otp storedOtp = otpRepository.findByEmailAndProfileType(email, profileType);
        logger.debug("Fetched otp record from database for {}", storedOtp);

        if (storedOtp == null) {
            logger.debug("No OTP found for {}", email);
            throw new OtpException("No OTP found for this email.", HttpStatus.NOT_FOUND);
        }
        if (!storedOtp.getProfileType().equals(profileType)) {
            logger.debug("OTP record does not match the profile type for {}, provided {} and in otp record {}", email, profileType, storedOtp.getProfileType());
            throw new OtpException("OTP does not match the profile type.", HttpStatus.BAD_REQUEST);
        }
        if (storedOtp.isExpired()) {
            logger.debug("OTP has expired for {}", email);
            throw new OtpException("OTP has expired.", HttpStatus.GONE);
        }
        if (storedOtp.isBlocked()) {
            logger.debug("User is blocked from validating OTP for {}", email);
            throw new OtpException("You are blocked from validating OTP. Try after 15 minutes.", HttpStatus.FORBIDDEN);
        }

        logger.debug("Comparing input OTP with stored OTP for {}", email);

        if (storedOtp.getCode().equals(inputOtp)) {
            logger.info("OTP validated successfully for {}, hence deleting existing otp record", email);
            otpRepository.delete(storedOtp);
            logger.debug("OTP record deleted successfully for {}", email);
            return true;
        } else {
            logger.info("Invalid OTP provided for {}", email);
            storedOtp.incrementAttempts();
            otpRepository.save(storedOtp);
            logger.error("Invalid OTP. Attempt " + storedOtp.getFailedAttempts() + " of 3.");
            throw new OtpException("Invalid OTP. Attempt " + storedOtp.getFailedAttempts() + " of 3.", HttpStatus.UNAUTHORIZED);
        }
    }
}
