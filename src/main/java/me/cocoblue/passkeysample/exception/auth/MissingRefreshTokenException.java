package me.cocoblue.passkeysample.exception.auth;

public class MissingRefreshTokenException extends RuntimeException {

  public MissingRefreshTokenException() {
    super("리프레시 토큰이 존재하지 않습니다");
  }
}
