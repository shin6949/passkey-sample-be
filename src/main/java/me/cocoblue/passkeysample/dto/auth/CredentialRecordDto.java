package me.cocoblue.passkeysample.dto.auth;

import lombok.Builder;
import lombok.With;

@Builder
public record CredentialRecordDto(
    String id,
    @With String label,  // label만 수정 가능
    String created,
    String lastUsed,
    Long signatureCount
) {}
