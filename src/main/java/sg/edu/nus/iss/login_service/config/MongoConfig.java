package sg.edu.nus.iss.login_service.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "sg.edu.nus.iss.login_service.repository"
)
public class MongoConfig {
    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${mongo.srv}")
    private String mongoSrv;

    @Value("${mongo.db}")
    private String mongoDb;

    @Bean
    public MongoDatabaseFactory mongoFactory() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoSrv + mongoDb))
                .build();

        return new SimpleMongoClientDatabaseFactory(MongoClients.create(settings), mongoDb);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoOrderDbFactory) {
        return new MongoTemplate(mongoOrderDbFactory);
    }

}
