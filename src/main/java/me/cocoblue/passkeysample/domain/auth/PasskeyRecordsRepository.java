package me.cocoblue.passkeysample.domain.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface PasskeyRecordsRepository extends CrudRepository<PasskeyRecordsEntity, String> {

    List<PasskeyRecordsEntity> findByUserId(String userId);

    Optional<PasskeyRecordsEntity> findByCredentialId(String credentialId);
    Optional<PasskeyRecordsEntity> findByUuid(String uuid);
}
