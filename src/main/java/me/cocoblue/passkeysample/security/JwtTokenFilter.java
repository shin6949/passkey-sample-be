package me.cocoblue.passkeysample.security;

import me.cocoblue.passkeysample.service.auth.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    final String accessToken = getTokenFromRequest(request);

    boolean isJwtTokenValid = false;
    try {
      isJwtTokenValid = jwtTokenProvider.validateToken(accessToken, false);

    } catch (Exception e) {
      String requestURI = request.getRequestURI();

      if (requestURI.startsWith("/webauthn/options")
              || requestURI.startsWith("/api/user/")
              || requestURI.startsWith("/api/auth/")) {
        filterChain.doFilter(request, response);
        return;
      }
    }

    if (accessToken != null && isJwtTokenValid) {
      UsernamePasswordAuthenticationToken authentication = getAuthenticationFromToken(accessToken);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private UsernamePasswordAuthenticationToken getAuthenticationFromToken(String token) {
    String userId = jwtTokenProvider.getUserIdFromAccessToken(token);
    log.debug("userId from JWT: {}", userId);
    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }
}
