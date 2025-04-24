package me.cocoblue.passkeysample.service.auth;

import me.cocoblue.passkeysample.domain.auth.RevokedRefreshTokenEntity;
import me.cocoblue.passkeysample.domain.auth.RevokedRefreshTokenRepository;
import me.cocoblue.passkeysample.domain.user.UserEntity;
import me.cocoblue.passkeysample.domain.user.UserRepository;
import me.cocoblue.passkeysample.dto.auth.AuthResp;
import me.cocoblue.passkeysample.dto.auth.LoginReq;
import me.cocoblue.passkeysample.dto.auth.UptimeUserDetails;
import me.cocoblue.passkeysample.exception.auth.InvalidTokenException;
import me.cocoblue.passkeysample.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final RevokedRefreshTokenRepository revokedRefreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  /** 로그인 */
  @Transactional
  public AuthResp login(final LoginReq requestDto) {
    // CHECK USERNAME AND PASSWORD
    final UserEntity userEntity = this.userRepository.findByEmail(requestDto.email()).orElseThrow(
        () -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. email = " + requestDto.email()));
    if (!passwordEncoder.matches(requestDto.password(), userEntity.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다. email = " + requestDto.email());
    }

    // GENERATE ACCESS_TOKEN AND REFRESH_TOKEN
    final String accessToken = this.jwtTokenProvider.generateAccessToken(
        new UsernamePasswordAuthenticationToken(new UptimeUserDetails(userEntity), userEntity.getPassword()));
    final String refreshToken = this.jwtTokenProvider.generateRefreshToken(
        new UsernamePasswordAuthenticationToken(new UptimeUserDetails(userEntity), userEntity.getPassword()));

    return AuthResp.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Transactional
  public AuthResp loginByPassKey(final String email) {
    // CHECK USERNAME AND PASSWORD
    final UserEntity userEntity = this.userRepository.findByEmail(email).orElseThrow(
        () -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. email = " + email));

    // GENERATE ACCESS_TOKEN AND REFRESH_TOKEN
    final String accessToken = this.jwtTokenProvider.generateAccessToken(
        new UsernamePasswordAuthenticationToken(new UptimeUserDetails(userEntity), userEntity.getPassword()));
    final String refreshToken = this.jwtTokenProvider.generateRefreshToken(
        new UsernamePasswordAuthenticationToken(new UptimeUserDetails(userEntity), userEntity.getPassword()));

    return AuthResp.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  /** Token 갱신 */
  @Transactional
  public AuthResp refreshToken(final String providedRefreshToken) {
    log.debug("refreshToken request for refreshToken: {}", providedRefreshToken);
    // 유효한 REFRESH_TOKEN 인지 확인
    if (!this.jwtTokenProvider.validateToken(providedRefreshToken, true)) {
      log.debug("This is not a valid refresh token");
      throw new InvalidTokenException();
    }

    // Revoked 된 토큰인지 확인
    if(revokedRefreshTokenRepository.findByToken(providedRefreshToken) != null) {
      throw new InvalidTokenException();
    }

    // REFRESH_TOKEN 으로부터 USER_ID 를 추출
    final String userId = this.jwtTokenProvider.getUserIdFromRefreshToken(providedRefreshToken);
    final UserEntity userEntity = this.userRepository.findById(userId).orElseThrow(
        () -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));

    // ACCESS_TOKEN, REFRESH_TOKEN 재발급
    final String newAccessToken = this.jwtTokenProvider.generateAccessToken(
        new UsernamePasswordAuthenticationToken(
            new UptimeUserDetails(userEntity), userEntity.getPassword())
    );

    final String newRefreshToken = this.jwtTokenProvider.generateRefreshToken(
        new UsernamePasswordAuthenticationToken(
            new UptimeUserDetails(userEntity), userEntity.getPassword())
    );

    return AuthResp.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  /**
   * 로그아웃 과정 중 REFRESH_TOKEN 을 REVOKE 하는 메서드
   *
   * @param token REVOKE 할 REFRESH_TOKEN
   */
  @CachePut(value = "revoked_tokens", key = "#token")
  @Transactional
  public RevokedRefreshTokenEntity revokeRefreshToken(final String token) {
    // 유효한 REFRESH_TOKEN 인지 확인
    if (!this.jwtTokenProvider.validateToken(token, true)) {
      throw new InvalidTokenException();
    }

    // Token 만료 시간 추출
    final Date tokenExpiredAtDate = this.jwtTokenProvider.getExpirationFromToken(token, true);
    final LocalDateTime tokenExpiredAt = tokenExpiredAtDate.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();

    // Token REVOKE
    final RevokedRefreshTokenEntity revokedRefreshTokenEntity = RevokedRefreshTokenEntity.builder()
        .token(token)
        .originalExpiredAt(tokenExpiredAt)
        .build();

    this.revokedRefreshTokenRepository.save(revokedRefreshTokenEntity);
    log.info("Token Revoked Successfully.");
    return revokedRefreshTokenEntity;
  }
}