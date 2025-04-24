package me.cocoblue.passkeysample.domain.auth;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface PasskeyUserRepository extends CrudRepository<PasskeyUserEntity, String> {

    Optional<PasskeyUserEntity> findByUserId(UserEntity userId);

    Optional<PasskeyUserEntity> findByPassKeyUserId(String passKeyUserId);
    Optional<PasskeyUserEntity> findByUserId_Id(String userId);
    Optional<PasskeyUserEntity> findByUserId_Email(String email);
}
