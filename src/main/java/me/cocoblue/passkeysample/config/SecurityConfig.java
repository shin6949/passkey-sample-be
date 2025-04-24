package me.cocoblue.passkeysample.config;

import me.cocoblue.passkeysample.domain.auth.PasskeyRecordsRepository;
import me.cocoblue.passkeysample.domain.auth.PasskeyUserRepository;
import me.cocoblue.passkeysample.domain.user.UserRepository;
import me.cocoblue.passkeysample.security.JwtTokenFilter;
import me.cocoblue.passkeysample.security.PassKeyCustomUserCredentialRepository;
import me.cocoblue.passkeysample.security.PassKeyPublicKeyCredentialUserEntityRepository;
import me.cocoblue.passkeysample.service.auth.CustomUserDetailsService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Log4j2
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtTokenFilter jwtTokenFilter;
  private final CustomUserDetailsService userDetailsService;

  @Value("${app.base-url}")
  private String baseUrl;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain sessionEnabledFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/auth/login/passkey") // 세션을 사용할 URL 패턴
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 또는 ALWAYS
        );

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain statelessFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/webauthn/**", "/api/user/**", "/api/auth/**", "/api/agent/job/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(restAuthenticationEntryPoint()) // 401 처리
            .accessDeniedHandler(restAccessDeniedHandler()) // 403 처리
        )
        .userDetailsService(userDetailsService)
        .addFilterBefore(this.jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .webAuthn((webAuthn) -> webAuthn
            .rpName("Passkey Sample")
            .rpId("localhost")
            .allowedOrigins(Set.of("http://localhost:3000", baseUrl))
            .disableDefaultRegistrationPage(true)
        )
        .formLogin(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration corsConfig = new CorsConfiguration();

    corsConfig.addAllowedOrigin("http://localhost:3000");
    corsConfig.setAllowCredentials(true);
    corsConfig.addAllowedHeader("*");
    corsConfig.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }

  @Bean
  UserCredentialRepository userCredentialRepository(
      PasskeyRecordsRepository passkeyRecordsRepository) {
    return new PassKeyCustomUserCredentialRepository(passkeyRecordsRepository);
  }

  @Bean
  PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(
      PasskeyUserRepository passkeyUserRepository,
      UserRepository userRepository) {
    return new PassKeyPublicKeyCredentialUserEntityRepository(passkeyUserRepository, userRepository);
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  private AuthenticationEntryPoint restAuthenticationEntryPoint() {
    return (request, response, authException) -> {
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
    };
  }

  private AccessDeniedHandler restAccessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpStatus.FORBIDDEN.value());
    };
  }
}
