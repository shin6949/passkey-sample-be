package me.cocoblue.passkeysample.domain.common;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ApplicationSettingEntity를 다루는 Repository
 *
 * @author @shin6949
 * @version 1.0.0
 * @see ApplicationSettingEntity
 * @since 1.0.0
 */
@Repository
public interface ApplicationSettingRepository extends
    JpaRepository<ApplicationSettingEntity, String> {

  Optional<ApplicationSettingEntity> findApplicationSettingEntityBySettingKeyEquals(
      final String settingKey);
}
