package me.cocoblue.passkeysample.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileReq(
  @NotEmpty @NotNull @Email String email,
  @NotEmpty @NotNull String name,
  @NotNull Boolean useGravatar,
  @NotNull Boolean isProfileImageChanged,
  @NotNull Boolean isEmailChanged,
  @NotNull Boolean isEmailChecked
) {

}
