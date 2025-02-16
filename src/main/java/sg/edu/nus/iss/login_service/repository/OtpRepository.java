package sg.edu.nus.iss.login_service.repository;

import sg.edu.nus.iss.login_service.entity.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OtpRepository extends MongoRepository<Otp, String> {
    Optional<Otp> findByEmail(String email);
}
