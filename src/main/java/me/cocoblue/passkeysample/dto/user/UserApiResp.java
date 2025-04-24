package me.cocoblue.passkeysample.dto.user;

import me.cocoblue.passkeysample.domain.user.UserEntity;

public record UserApiResp(String id, String name, boolean useGravatar, String profileUrl, String role) {

  public UserApiResp(UserEntity user) {
    this(
        user.getId(),
        user.getName(),
        user.isUseGravatar(),
        user.getProfileUrl(),
        user.getRole().name()
    );
  }
}
