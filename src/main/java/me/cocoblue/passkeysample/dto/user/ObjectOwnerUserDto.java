package me.cocoblue.passkeysample.dto.user;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import lombok.Builder;

@Builder
public record ObjectOwnerUserDto (
    String id,
    String name,
    String profileImageUrl
) {
  public ObjectOwnerUserDto(UserEntity userEntity) {
    this(userEntity.getId(), userEntity.getName(), userEntity.getFullProfileUrl());
  }
}
