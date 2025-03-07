package sg.edu.nus.iss.login_service.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.bson.UuidRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MongoSingleton {
    private static final Logger log = LoggerFactory.getLogger(MongoSingleton.class);
    private static final Map<String, MongoClient> mongoClients = new HashMap<>();

    @Value("${mongo.srv}")
    private String mongoSrv;

    @Value("${mongo.users.db}")
    private String userDb;

    @Value("${mongo.users.username}")
    private String userDbUsername;

    @Value("${mongo.users.password}")
    private String userDbPassword;

    @Value("${mongo.otp.db}")
    private String otpDb;

    @Value("${mongo.otp.username}")
    private String otpDbUsername;

    @Value("${mongo.otp.password}")
    private String otpDbPassword;

    private static String staticMongoSrv;
    private static String staticUserDb;
    private static String staticUserDbUsername;
    private static String staticUserDbPassword;
    private static String staticOtpDb;
    private static String staticOtpDbUsername;
    private static String staticOtpDbPassword;

    @PostConstruct
    private void init() {
        staticMongoSrv = this.mongoSrv;
        staticUserDb = this.userDb;
        staticUserDbUsername = this.userDbUsername;
        staticUserDbPassword = this.userDbPassword;
        staticOtpDb = this.otpDb;
        staticOtpDbUsername = this.otpDbUsername;
        staticOtpDbPassword = this.otpDbPassword;
    }

    private MongoSingleton() {
        // Private constructor to prevent instantiation
    }

    public static MongoClient getMongoClient(String dbName) {
        log.debug("Getting MongoClient for DB: {}", dbName);
        if (!mongoClients.containsKey(dbName)) {
            synchronized (MongoSingleton.class) {
                if (!mongoClients.containsKey(dbName)) {
                    mongoClients.put(dbName, createMongoClient(dbName));
                }
            }
        }
        return mongoClients.get(dbName);
    }

    private static MongoClient createMongoClient(String dbName) {
        log.info("Creating MongoClient for DB: {}", dbName);
        log.info("MongoDB SRV: {}", staticMongoSrv);
        log.info("MongoDB Username: {}", dbName.equals(staticUserDb) ? staticUserDbUsername : staticOtpDbUsername);
        log.info("MongoDB Password: {}", dbName.equals(staticUserDb) ? staticUserDbPassword : staticOtpDbPassword);
        ConnectionString connectionString = new ConnectionString(staticMongoSrv);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();

        return MongoClients.create(settings);
    }

    @PreDestroy
    public void closeAllClients() {
        log.info("Closing all MongoClients.");
        for (MongoClient client : mongoClients.values()) {
            client.close();
        }
        mongoClients.clear();
    }
}
