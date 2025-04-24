package me.cocoblue.passkeysample.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class WebAuthnSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtProvider;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    log.debug("WebAuthn Authentication Success");

    // 1. JWT 토큰 생성
    String accessToken = jwtProvider.generateAccessToken(authentication);
    String refreshToken = jwtProvider.generateRefreshToken(authentication);

    // 2. 응답 헤더 설정
    response.setHeader("Authorization", "Bearer " + accessToken);

    // 3. Refresh Token을 HTTP Only 쿠키로 설정
    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(Duration.ofDays(30))
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    // 4. 기존 응답 유지 및 추가 정보 포함
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("redirectUrl", "/");
    responseBody.put("authenticated", true);
    responseBody.put("user", authentication.getPrincipal()); // 사용자 정보 추가

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), responseBody);
  }
}
