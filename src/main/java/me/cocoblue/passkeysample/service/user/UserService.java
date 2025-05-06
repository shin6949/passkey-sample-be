package me.cocoblue.passkeysample.service.user;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import me.cocoblue.passkeysample.domain.user.UserRepository;
import me.cocoblue.passkeysample.domain.user.UserRole;
import me.cocoblue.passkeysample.dto.user.SignUpReq;
import me.cocoblue.passkeysample.dto.user.UserApiResp;
import me.cocoblue.passkeysample.exception.user.DuplicateEmailException;
import me.cocoblue.passkeysample.service.common.S3Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final S3Service s3Service;

  @Cacheable(value = "email_check_result", key = "#email", unless = "#result == true")
  public boolean isDuplicateEmail(final String email) {
    return userRepository.existsByEmail(email);
  }

  @Cacheable(value = "users_api_resp", key = "#id", unless = "#result == true")
  public UserApiResp findByIdToApiResp(final String id) {
    final UserEntity userEntity = userRepository.findById(id).orElse(null);

    assert userEntity != null;
    userEntity.setFullProfileUrl(configureProfileURL(userEntity.getProfileUrl(), userEntity.getEmail()));

    return new UserApiResp(userEntity);
  }

  @Cacheable(value = "users", key = "#id", unless = "#result == null")
  public UserEntity findUserEntityById(final String id) {
    log.debug("Finding user by id {}", id);
    
    final Optional<UserEntity> user = userRepository.findById(id);
    log.debug("The user exists: {}", user.isPresent());
    if (user.isPresent()) {
      log.debug("User found: {}", user.get());
    } else {
      log.debug("User not found");
    }

    return user.orElse(null);
  }

  @CacheEvict(value = "email_check_result", key = "#user.email")
  public UserEntity save(UserEntity user) {
    return userRepository.save(user);
  }

  @Transactional
  @Caching(
      put = {
          @CachePut(value = "email_check_result", key = "#request.email()"),
          @CachePut(value = "users", key = "#result.getId()")
      }
  )
  public UserEntity signUp(final SignUpReq request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new DuplicateEmailException();
    }

    if(!request.password().equals(request.passwordConfirm())) {
      throw new IllegalArgumentException("Password and password confirm do not match");
    }

    final String encodedPassword = passwordEncoder.encode(request.password().trim());
    final UserEntity newUser = request.toEntity(encodedPassword, UserRole.USER);
    newUser.setName(request.name().trim());
    newUser.setEmail(request.email().trim());
    if(request.useGravatar()) {
      newUser.setProfileUrl(configureGravatarURL(request.email()));
    }

    userRepository.save(newUser);

    return newUser;
  }

  public String configureProfileURL(final UserEntity userEntity) {
    return configureProfileURL(userEntity.getProfileUrl(), userEntity.getEmail());
  }

  public String configureProfileURL(final String currentProfileURL, final String email) {
    if(currentProfileURL == null) {
      return null;
    }

    // Gravatar URL이 현재 Profile Image URL인 경우, 그대로 반환
    if(currentProfileURL.equals(configureGravatarURL(email))) {
      return currentProfileURL;
    }

    return s3Service.configureS3URL(currentProfileURL);
  }

  public String configureGravatarURL(final String email) {
    return "https://gravatar.com/avatar/" + encodeEmailByMD5(email) + "?size=500";
  }

  /**
   * Gravatar URL을 만들기 위해 이메일 주소를 MD5로 인코딩하는 함수
   *
   * @param emailAddress 이메일 주소
   * @return MD5로 인코딩된 이메일 주소
   */
  private String encodeEmailByMD5(final String emailAddress) {
    try {
      // 1. Gravatar 표준 요구사항: 트림 + 소문자 변환
      String normalizedEmail = emailAddress.trim().toLowerCase();

      // 2. MD5 인스턴스 생성
      MessageDigest md = MessageDigest.getInstance("MD5");

      // 3. 바이트 배열 변환 및 해싱
      byte[] hashBytes = md.digest(normalizedEmail.getBytes());

      // 4. 16진수 문자열 변환
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        hexString.append(String.format("%02x", b));
      }

      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available", e);
    }
  }
}
