package me.cocoblue.passkeysample.dto.profile;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record FetchProfileResp(
  String email,
  String name,
  String profileImage,
  boolean useGravatar,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public FetchProfileResp(UserEntity userEntity) {
    this(
      userEntity.getEmail(),
      userEntity.getName(),
      userEntity.getFullProfileUrl(),
      userEntity.isUseGravatar(),
      userEntity.getCreatedAt(),
      userEntity.getUpdatedAt()
    );
  }
}
