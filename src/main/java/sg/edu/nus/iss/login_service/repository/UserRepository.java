package sg.edu.nus.iss.login_service.repository;

import org.springframework.stereotype.Repository;
import sg.edu.nus.iss.login_service.entity.User;
import sg.edu.nus.iss.login_service.entity.ProfileType;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>  {
    Optional<User> findByEmailAndProfileType(String email, ProfileType profileType);
}
