package me.cocoblue.passkeysample.domain.user;

import java.util.Optional;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = "users")
public interface UserRepository extends JpaRepository<UserEntity, String> {
  boolean existsByEmail(String email);
  Optional<UserEntity> findByEmail(String email);
}
