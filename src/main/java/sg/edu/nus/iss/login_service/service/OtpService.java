package sg.edu.nus.iss.login_service.service;

import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class OtpService {
    private static final Logger logger = Logger.getLogger(OtpService.class.getName());

    @Autowired
    private OtpRepository otpRepository;

    public Otp generateOtp(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        Otp otp = new Otp(email, code, expirationTime);
        logger.info("Generated OTP for email: " + email);
        return otpRepository.save(otp);
    }

    public boolean validateOtp(String email, String code) {
        Optional<Otp> otpOptional = otpRepository.findByEmail(email);
        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            if (!otp.isExpired() && otp.getCode().equals(code) && !otp.isCurrentlyBlocked()) {
                otpRepository.delete(otp);
                logger.info("OTP validated successfully for: " + email);
                return true;
            }
        }
        logger.warning("Invalid OTP attempt for: " + email);
        return false;
    }
}
