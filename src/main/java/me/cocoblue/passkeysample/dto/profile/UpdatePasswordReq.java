package me.cocoblue.passkeysample.dto.profile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdatePasswordReq(@NotEmpty @NotNull String newPassword,
                                @NotEmpty @NotNull String newPasswordConfirm) {

}
