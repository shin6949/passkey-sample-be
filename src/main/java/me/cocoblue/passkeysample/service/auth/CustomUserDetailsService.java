package me.cocoblue.passkeysample.service.auth;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import me.cocoblue.passkeysample.domain.user.UserRepository;
import me.cocoblue.passkeysample.dto.auth.UptimeUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;	// 별도로 생성해야 함

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByEmail(email).orElseThrow(
        () -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. email = " + email));
    return new UptimeUserDetails(user);	// 위에서 생성한 UptimeUserDetails Class
  }

  public UserDetails loadUserById(String userId) throws IllegalArgumentException {
    log.debug("CustomUserDetailsService.loadUserById Called. Id: {}", userId);
    UserEntity user = userRepository.findById(userId).orElseThrow(
        () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. user_id = " + userId));
    return new UptimeUserDetails(user);
  }
}