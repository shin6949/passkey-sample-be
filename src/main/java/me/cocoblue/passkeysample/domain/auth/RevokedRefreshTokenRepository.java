package me.cocoblue.passkeysample.domain.auth;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedRefreshTokenRepository extends JpaRepository<RevokedRefreshTokenEntity, String> {
  @Cacheable(value = "revoked_tokens", key = "#token", unless = "#result == null")
  RevokedRefreshTokenEntity findByToken(String token);
}
