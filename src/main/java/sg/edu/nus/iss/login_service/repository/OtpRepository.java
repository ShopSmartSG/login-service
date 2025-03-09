package sg.edu.nus.iss.login_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import sg.edu.nus.iss.login_service.entity.Otp;
import sg.edu.nus.iss.login_service.entity.ProfileType;

@Repository
public interface OtpRepository extends MongoRepository<Otp, String> {
    Otp findByEmailAndProfileType(String email, ProfileType profileType);
}