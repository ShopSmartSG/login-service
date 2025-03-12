package sg.edu.nus.iss.login_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConfigLogger implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLogger.class);

    @Value("${mongo.srv}")
    private String mongoSrv;

    @Value("${mongo.users.db}")
    private String mongoUsersDb;

    @Value("${mongo.users.username}")
    private String mongoUsersUsername;

    // Avoid logging sensitive data such as passwords in production!
    @Value("${mongo.users.password}")
    private String mongoUsersPassword;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}")
    private String fromEmailPassword;


    @Override
    public void run(String... args) {
        logger.info("MongoDB SRV: {}", mongoSrv);
        logger.info("MongoDB Users DB: {}", mongoUsersDb);
        logger.info("MongoDB Users Username: {}", mongoUsersUsername);
        // If necessary, log a masked version of the password for debugging
        logger.info("MongoDB Users Password: {}", maskPassword(mongoUsersPassword));
        logger.info("Email From: {}", fromEmail);
        // If necessary, log a masked version of the password for debugging
        logger.info("Email Password: {}", maskPassword(fromEmailPassword));
    }

    private String maskPassword(String password) {
        if (password != null && password.length() > 2) {
            return password.charAt(0) + "***" + password.charAt(password.length() - 1);
        }
        return "N/A";
    }
}
