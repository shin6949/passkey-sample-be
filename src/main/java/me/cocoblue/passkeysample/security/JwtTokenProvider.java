package me.cocoblue.passkeysample.security;

import me.cocoblue.passkeysample.domain.auth.RevokedRefreshTokenRepository;
import me.cocoblue.passkeysample.dto.auth.UptimeUserDetails;
import me.cocoblue.passkeysample.exception.auth.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${app.jwt.access-private-key-location}")
  private String accessPrivateKeyPath;
  @Value("${app.jwt.refresh-private-key-location}")
  private String refreshPrivateKeyPath;
  @Value("${app.jwt.expiration.access}")
  private Long jwtAccessTokenExpirationTime;
  @Value("${app.jwt.expiration.refresh}")
  private Long jwtRefreshTokenExpirationTime;

  private final RevokedRefreshTokenRepository revokedRefreshTokenRepository;

  private PrivateKey accessPrivateKey;
  private PublicKey accessPublicKey;
  private PrivateKey refreshPrivateKey;
  private PublicKey refreshPublicKey;

  @PostConstruct
  protected void init() throws Exception {
    accessPrivateKey = PemFileLoader.loadPrivateKey(accessPrivateKeyPath);
    accessPublicKey = PemFileLoader.extractPublicKeyFromPrivateKey(accessPrivateKeyPath);
    refreshPrivateKey = PemFileLoader.loadPrivateKey(refreshPrivateKeyPath);
    refreshPublicKey = PemFileLoader.extractPublicKeyFromPrivateKey(refreshPrivateKeyPath);
  }

  public String generateAccessToken(final Authentication authentication) {
    final UptimeUserDetails uptimeUserDetails = (UptimeUserDetails) authentication.getPrincipal();
    final Date expiryDate = new Date(new Date().getTime() + jwtAccessTokenExpirationTime);
    return Jwts.builder()
        .subject(uptimeUserDetails.getId())
        .issuedAt(new Date())
        .expiration(expiryDate)
        .signWith(accessPrivateKey, SIG.RS256)
        .compact();
  }

  public String generateRefreshToken(final Authentication authentication) {
    final UptimeUserDetails uptimeUserDetails = (UptimeUserDetails) authentication.getPrincipal();
    final Date expiryDate = new Date(new Date().getTime() + jwtRefreshTokenExpirationTime);
    return Jwts.builder()
        .subject(uptimeUserDetails.getId())
        .issuedAt(new Date())
        .expiration(expiryDate)
        .signWith(refreshPrivateKey, SIG.RS256)
        .compact();
  }

  /**
   * 비밀번호 변경, Object 삭제 등 민감한 작업을 수행하기 전에 임시로 발급하는 토큰
   *
   * @param userId        사용자 ID
   * @param expirationTime 만료 시간
   * @param actionKey     작업 유형
   * @return 임시 인증 토큰
   */
  public String generateTempAuthorizationToken(final String userId, final long expirationTime,
      final TempTokenActionKey actionKey) {
    final Date expiryDate = new Date(new Date().getTime() + expirationTime);
    return Jwts.builder()
        .subject(userId)
        .claim("action", actionKey)
        .issuedAt(new Date())
        .expiration(expiryDate)
        .signWith(refreshPrivateKey, SIG.RS256)
        .compact();
  }

  /**
   * 임시 인증 토큰에서 사용자 ID를 추출하는 메서드
   *
   * @param token 임시 인증 토큰
   * @param expectedAction 작업 유형
   * @return 사용자 ID
   */
  public String getUserIdFromTempAuthorizationToken(final String token, final TempTokenActionKey expectedAction) {
    // claims 를 불러옴.
    final Claims claims = Jwts.parser()
        .verifyWith(refreshPublicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    // Action key를 불러옴.
    final TempTokenActionKey actualAction = TempTokenActionKey.valueOf(
        claims.get("action", String.class)
    );

    // Action key가 일치하지 않으면 예외 발생.
    if (actualAction != expectedAction) {
      throw new InvalidTokenException();
    }

    return claims.getSubject();
  }

  public Date getExpirationFromToken(final String token, final boolean isRefreshToken) {
    return Jwts.parser()
        .verifyWith(isRefreshToken ? refreshPublicKey : accessPublicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getExpiration();
  }

  public String getUserIdFromAccessToken(final String token) {
    return Jwts.parser()
        .verifyWith(accessPublicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public String getUserIdFromRefreshToken(final String token) {
    return Jwts.parser()
        .verifyWith(refreshPublicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public boolean validateToken(final String token, final boolean isRefreshToken) {
    try {
      final JwtParser parser = Jwts.parser()
          .verifyWith(isRefreshToken ? refreshPublicKey : accessPublicKey)
          .build();

      parser.parseSignedClaims(token);

      if(isRefreshToken) {
        if(revokedRefreshTokenRepository.findByToken(token) != null) {
          log.info("Tried to use revoked refresh token.");
          throw new InvalidTokenException();
        }
      }
      return true;
    } catch (final ExpiredJwtException ex) {
      log.info("A request using Expired JWT token.");
      throw new InvalidTokenException();
    } catch (final UnsupportedJwtException ex) {
      log.error("[JWT] Unsupported token: {}", ex.getMessage());
      throw new InvalidTokenException();
    } catch (final MalformedJwtException ex) {
      log.error("[JWT] Malformed token: {}", ex.getMessage());
      throw new InvalidTokenException();
    } catch (final SignatureException ex) {
      log.error("[JWT] Signature mismatch: {}", ex.getMessage());
      throw new InvalidTokenException();
    } catch (final IllegalArgumentException ex) {
      log.error("[JWT] Empty claims: {}", ex.getMessage());
      throw new InvalidTokenException();
    }
  }
}