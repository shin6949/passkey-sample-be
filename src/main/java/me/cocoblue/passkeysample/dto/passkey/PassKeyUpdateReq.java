package me.cocoblue.passkeysample.dto.passkey;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PassKeyUpdateReq(
    @NotEmpty @NotNull String uuid,
    @NotEmpty @NotNull String name
) {
}