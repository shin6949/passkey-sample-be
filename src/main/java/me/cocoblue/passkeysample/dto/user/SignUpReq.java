package me.cocoblue.passkeysample.dto.user;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import me.cocoblue.passkeysample.domain.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpReq(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String passwordConfirm,
    @NotBlank String name,
    boolean useGravatar
) {
  public UserEntity toEntity(String encodedPassword, UserRole role) {
    return UserEntity.builder()
        .email(email())
        .password(encodedPassword)
        .name(name())
        .role(role)
        .profileUrl(null)
        .useGravatar(useGravatar)
        .enabled(true)
        .build();
  }
}
