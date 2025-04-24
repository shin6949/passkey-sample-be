package me.cocoblue.passkeysample.controller;

import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.dto.auth.AuthResp;
import me.cocoblue.passkeysample.dto.auth.LoginReq;
import me.cocoblue.passkeysample.exception.auth.MissingRefreshTokenException;
import me.cocoblue.passkeysample.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @Value("${app.jwt.expiration.refresh}")
  private Long jwtRefreshTokenExpirationTime;

  /** 로그인 API */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResp>> login(@Valid @RequestBody final LoginReq requestDto,
      HttpServletResponse response) {
    log.debug("login request for email: {}", requestDto.email());
    final AuthResp responseDto = this.authService.login(requestDto);

    // Refresh Token은 Cookie에 저장하여 관리
    final ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", responseDto.refreshToken())
        .httpOnly(true)
//        .secure(true)
//        .path("/api/auth/refresh")
        .path("/")
        .maxAge(Duration.ofSeconds(jwtRefreshTokenExpirationTime))
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(
            ApiResponse.<AuthResp>builder()
                .result(ApiResultCode.SUCCESS)
                .data(responseDto)
                .build()
        );
  }

  /**
   * 로그아웃 API
   *
   * @param refreshToken 리프레시 토큰
   * @param response HTTP 응답 객체
   * @return 로그아웃 성공 여부
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Object>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {
    log.info("Logout request received.");

    // Refresh Token 무효화
    log.info("Revoking Refresh Token");
    authService.revokeRefreshToken(refreshToken);

    // Refresh Token 삭제
    final ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
//        .secure(true)
//        .path("/api/auth/refresh")
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(
            ApiResponse.builder()
                .result(ApiResultCode.SUCCESS)
                .build()
        );
  }

  @PostMapping("/login/passkey")
  public ResponseEntity<ApiResponse<AuthResp>> generateTokenByPasskey(
     SecurityContext securityContext,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    log.debug("login by passkey request received.");
    log.debug("Security Context: {}", securityContext.getAuthentication().toString());
    String email = securityContext.getAuthentication().getName();
    final AuthResp responseDto = this.authService.loginByPassKey(email);

    // Access Token 을 발급하면, 세션 인증은 필요하지 않음.
    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    logoutHandler.logout(request, response, securityContext.getAuthentication());

    // 새로운 리프레시 토큰 쿠키 설정
    ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", responseDto.refreshToken())
        .httpOnly(true)
        .path("/")
        .maxAge(Duration.ofSeconds(jwtRefreshTokenExpirationTime))
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(
            ApiResponse.<AuthResp>builder()
                .result(ApiResultCode.SUCCESS)
                .data(responseDto)
                .build()
        );
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthResp>> refreshToken(
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response
  ) {
    log.debug("Refresh token request received.");
    log.debug("Refresh token: {}", refreshToken);

    if (refreshToken == null || refreshToken.isBlank()) {
      throw new MissingRefreshTokenException();
    }

    final AuthResp newTokenDto = authService.refreshToken(refreshToken);

    // 새로운 리프레시 토큰 쿠키 설정
    ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", newTokenDto.refreshToken())
        .httpOnly(true)
        .path("/")
        .maxAge(Duration.ofSeconds(jwtRefreshTokenExpirationTime))
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(
            ApiResponse.<AuthResp>builder()
                .result(ApiResultCode.SUCCESS)
                .data(newTokenDto)
                .build()
        );
  }
}