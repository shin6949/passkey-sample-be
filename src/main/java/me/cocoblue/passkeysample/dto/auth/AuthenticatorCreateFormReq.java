package me.cocoblue.passkeysample.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record AuthenticatorCreateFormReq(
		@NotNull @Valid String clientDataJSON,
		@NotNull @Valid String attestationObject,
		Set<String> transports,
		@NotNull String clientExtensions
) {}
