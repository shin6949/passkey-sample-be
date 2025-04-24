package me.cocoblue.passkeysample.dto.profile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CheckCurrentPasswordMatchReq(@NotEmpty @NotNull String inputPassword) {

}
