package me.cocoblue.passkeysample.dto.passkey;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PassKeyDeleteReq(@NotEmpty @NotNull String uuid) {

}
