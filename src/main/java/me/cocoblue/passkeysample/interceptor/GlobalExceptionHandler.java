package me.cocoblue.passkeysample.interceptor;

import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.exception.BadRequestException;
import me.cocoblue.passkeysample.exception.auth.InvalidTokenException;
import me.cocoblue.passkeysample.exception.auth.MissingRefreshTokenException;
import me.cocoblue.passkeysample.exception.user.DuplicateEmailException;
import jakarta.transaction.RollbackException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 이메일 없음 (UsernameNotFoundException)
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(
            ApiResponse.builder()
              .result(ApiResultCode.USER_NOT_FOUND)
            .build()
        );
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handlePasswordMismatch(IllegalArgumentException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.builder()
              .result(ApiResultCode.PASSWORD_MISMATCH)
            .build()
        );
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ApiResponse<Object>> handleDuplicateEmail(DuplicateEmailException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.builder()
              .result(ApiResultCode.DUPLICATE_EMAIL)
            .build()
        );
  }

  @ExceptionHandler(MissingRefreshTokenException.class)
  public ResponseEntity<ApiResponse<Object>> handleMissingRefreshTokenException(DuplicateEmailException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.builder()
                .result(ApiResultCode.TOKEN_NOT_FOUND)
                .build()
        );
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiResponse<Object>> handleInvalidTokenException(InvalidTokenException ex) {
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(
            ApiResponse.builder()
                .result(ApiResultCode.INVALID_TOKEN)
                .build()
        );
  }

  @ExceptionHandler(RollbackException.class)
  public ResponseEntity<ApiResponse<Object>> handleRollbackException(RollbackException ex) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiResponse.builder()
                .result(ApiResultCode.INTERNAL_SERVER_ERROR)
                .build()
        );
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadRequestException(RollbackException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.builder()
                .result(ApiResultCode.BAD_REQUEST)
                .build()
        );
  }

}