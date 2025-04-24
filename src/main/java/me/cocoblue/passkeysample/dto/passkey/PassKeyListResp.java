package me.cocoblue.passkeysample.dto.passkey;

import me.cocoblue.passkeysample.domain.auth.PasskeyRecordsEntity;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record PassKeyListResp(
    String uuid,
    String label,
    LocalDateTime createdAt,
    LocalDateTime lastUsedAt
) {

  public PassKeyListResp(PasskeyRecordsEntity entity) {
    this(
        entity.getUuid(),
        entity.getLabel(),
        // Epoch 밀리초 -> LocalDateTime 변환
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.getCreated()),
            ZoneId.of("Asia/Seoul")
        ),
        // Epoch 밀리초 -> LocalDateTime 변환
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.getLastUsed()),
            ZoneId.of("Asia/Seoul")
        )
    );
  }
}
