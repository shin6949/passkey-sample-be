package me.cocoblue.passkeysample.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

public record AuthResp(
    String accessToken,
    @JsonIgnore String refreshToken
) {
  @Builder
  public AuthResp {} // 컴팩트 생성자
}
