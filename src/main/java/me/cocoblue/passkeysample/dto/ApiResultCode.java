package me.cocoblue.passkeysample.dto;

public enum ApiResultCode {
  SUCCESS,
  BAD_REQUEST,
  // 회원 가입
  DUPLICATE_EMAIL,
  PASSWORD_MISMATCH,
  // 인증 관련 오류
  INVALID_CREDENTIALS,
  // 토큰의 만료, 토큰의 무효화 등을 포함함.
  INVALID_TOKEN,
  TOKEN_NOT_FOUND,
  USER_NOT_FOUND,
  DISABLED_USER,
  // Passkey 관련 오류
  PASSKEY_NOT_FOUND,
  // 기타 에러
  INTERNAL_SERVER_ERROR,
}
