package me.cocoblue.passkeysample.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginReq(
    @Email String email,
    @NotEmpty String password
) {

}
