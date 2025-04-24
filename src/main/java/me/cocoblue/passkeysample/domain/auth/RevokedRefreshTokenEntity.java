package me.cocoblue.passkeysample.domain.auth;

import me.cocoblue.passkeysample.domain.common.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "revoked_refresh_token")
public class RevokedRefreshTokenEntity extends BaseTime {
  @Builder
  public RevokedRefreshTokenEntity(String token, LocalDateTime originalExpiredAt) {
    this.token = token;
    this.originalExpiredAt = originalExpiredAt;
  }

  @Id
  @Column(name = "token", nullable = false, length = 500)
  private String token;

  @Column(name = "original_expired_at", nullable = false)
  private LocalDateTime originalExpiredAt;
}
