package sg.edu.nus.iss.login_service.util;

import org.springframework.stereotype.Component;

@Component
public class LogMaskingUtil {

    public String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        return username.charAt(0) +
                "****" +
                (username.length() > 1 ? username.charAt(username.length() - 1) : "") +
                domain;
    }

    // Add other masking methods as needed
}
