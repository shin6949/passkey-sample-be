package me.cocoblue.passkeysample.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * ApplicationSetting 테이블의 key, default 값 모음.
 *
 * @author @shin6949
 * @version 1.0.0
 * @since 1.0.0
 */
@RequiredArgsConstructor
@ToString
@Getter
public enum ApplicationSettingKey {
  S3_ENABLED("S3_ENABLED", "false");

  private final String keyName;
  private final String defaultValue;
}
