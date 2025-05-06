package me.cocoblue.passkeysample.service.auth;

import me.cocoblue.passkeysample.domain.user.UserEntity;
import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.dto.profile.CheckCurrentPasswordMatchResp;
import me.cocoblue.passkeysample.dto.profile.FetchProfileResp;
import me.cocoblue.passkeysample.dto.profile.UpdatePasswordReq;
import me.cocoblue.passkeysample.dto.profile.UpdateProfileReq;
import me.cocoblue.passkeysample.security.JwtTokenProvider;
import me.cocoblue.passkeysample.security.TempTokenActionKey;
import me.cocoblue.passkeysample.service.common.S3Service;
import me.cocoblue.passkeysample.service.user.UserService;
import me.cocoblue.passkeysample.util.ImageUtil;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 수정 관련 서비스 클래스
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author @shin6949
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ProfileService {
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final S3Service s3Service;
  private final UserService userService;

  /**
   * 현재 비밀번호와 입력한 비밀번호가 일치하는지 확인하고 결과를 반환하는 메서드
   *
   * @param inputPassword 입력한 비밀번호
   * @param currentUserId 현재 사용자의 ID
   * @return 비밀번호 일치 여부 결과
   */
  public ResponseEntity<ApiResponse<CheckCurrentPasswordMatchResp>> checkMatchWithCurrentPassword(final String inputPassword, final String currentUserId) {
    log.debug("checkMatchWithCurrentPassword requested with userId: {}", currentUserId);
    final UserEntity userEntity = userService.findUserEntityById(currentUserId);
    if(userEntity == null) {
      log.info("UserEntity is not found. requested userId: {}", currentUserId);
      throw new UsernameNotFoundException("UserEntity is not found. requested userId: " + currentUserId);
    }

    final String encodedCurrentFromUserEntity = userEntity.getPassword();
    boolean isMatch = passwordEncoder.matches(
        inputPassword,
        encodedCurrentFromUserEntity
    );
    log.debug("Password match result: {}", isMatch);

    // Password가 일치하는 경우, Password를 변경할 수 있는 인가 코드를 발급하여 HTTP Only Cookie에 저장.
    // 10분의 유효시간을 가지며, 이후에는 인가 코드가 무효이다.
    String authorizationToken = null;
    if(isMatch) {
      authorizationToken = jwtTokenProvider.generateTempAuthorizationToken(
          userEntity.getId(), 600000L,
          TempTokenActionKey.UPDATE_PASSWORD
      );
    }

    final CheckCurrentPasswordMatchResp responseData = CheckCurrentPasswordMatchResp.builder()
        .isMatch(isMatch)
        .AuthorizationToken(authorizationToken)
        .build();

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.<CheckCurrentPasswordMatchResp>builder()
            .result(ApiResultCode.SUCCESS)
            .data(responseData)
            .build()
        );
  }

  /**
   * 비밀번호를 변경하는 메서드
   *
   * @param request 비밀번호 변경 요청
   * @param currentUserId 현재 사용자의 ID
   * @param passwordChangeAuthorizationToken 비밀번호 변경 인가 코드
   * @return 비밀번호 변경 결과
   */
  @Transactional
  public ResponseEntity<ApiResponse<?>> updatePassword(final UpdatePasswordReq request, final String currentUserId, final String passwordChangeAuthorizationToken) {
    // 인가 코드 유효성 확인 (인가 코드는 refresh token 과 동일한 키를 사용하여 생성)
    final boolean isValidAuthorizationToken = jwtTokenProvider.validateToken(passwordChangeAuthorizationToken, true);
    if(!isValidAuthorizationToken) {
      log.info("Invalid passwordChangeAuthorizationToken. requested userId: {}", currentUserId);
      return ResponseEntity
          .status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.builder()
              .result(ApiResultCode.INVALID_TOKEN)
              .build()
          );
    } else {
      log.info("The Password Change Authorization Token is valid. requested userId: {}", currentUserId);
    }

    // 비밀번호, 비밀번호 확인이 일치하는지 확인
    if(!request.newPassword().equals(request.newPasswordConfirm())) {
      log.info("Password and PasswordConfirm are not matched. requested userId: {}", currentUserId);
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.builder()
              .result(ApiResultCode.PASSWORD_MISMATCH)
              .build()
          );
    } else {
      log.info("Password and PasswordConfirm are matched. requested userId: {}", currentUserId);
    }

    final String userIdFromAuthorizationToken = jwtTokenProvider.getUserIdFromTempAuthorizationToken(
        passwordChangeAuthorizationToken, TempTokenActionKey.UPDATE_PASSWORD
    );
    final UserEntity userEntity = userService.findUserEntityById(currentUserId);
    if(userEntity == null) {
      log.info("UserEntity is not found. requested userId: {}", currentUserId);
      throw new UsernameNotFoundException("UserEntity is not found. requested userId: " + currentUserId);
    }

    // 인가 코드의 userId와 Access Token의 userId가 일치하는지 확인
    if(!userIdFromAuthorizationToken.equals(currentUserId)) {
      log.info("Mismatched userId between AuthorizationToken and AccessToken. requested userId: {}", currentUserId);
      return ResponseEntity
          .status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.builder()
              .result(ApiResultCode.INVALID_TOKEN)
              .build()
          );
    }

    final String encodedToUpdatePassword = passwordEncoder.encode(request.newPassword());
    userEntity.setPassword(encodedToUpdatePassword);
    userService.save(userEntity);
    log.info("Password updated successfully. requested userId: {}", currentUserId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.builder()
            .result(ApiResultCode.SUCCESS)
            .build()
        );
  }

  /**
   * 사용자의 프로필 정보를 가져오는 메서드
   *
   * @param currentUserId 현재 사용자의 ID
   * @return 사용자의 프로필 정보
   */
  @Cacheable(value = "profile_users", key = "#currentUserId", unless = "#result == null")
  public ResponseEntity<ApiResponse<?>> fetchProfile(final String currentUserId) {
    final UserEntity userEntity = userService.findUserEntityById(currentUserId);
    if (userEntity == null) {
      log.info("UserEntity is not found. requested userId: {}", currentUserId);
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.builder()
              .result(ApiResultCode.USER_NOT_FOUND)
              .build()
          );
    }

    if(userEntity.getProfileUrl() != null) {
      userEntity.setFullProfileUrl(userService.configureProfileURL(userEntity.getProfileUrl(), userEntity.getEmail()));
    }

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.builder()
            .result(ApiResultCode.SUCCESS)
            .data(new FetchProfileResp(userEntity))
            .build()
        );
  }

  /**
   * 사용자의 프로필 정보를 수정하는 메서드
   *
   * @param currentUserId 현재 사용자의 ID (Access Token에서 추출)
   * @param request 수정할 프로필 정보
   * @param newProfileImage 새로운 프로필 이미지
   * @return 프로필 수정 결과
   */
  @Caching(evict = {
      // 프로필 정보 캐시 삭제
      @CacheEvict(value = "profile_users", key = "#currentUserId"),
      // Header 쪽 캐시 삭제
      @CacheEvict(value = "users_api_resp", key = "#currentUserId")
  })
  @Transactional
  public ResponseEntity<ApiResponse<?>> updateProfile(final String currentUserId, final UpdateProfileReq request,
      final MultipartFile newProfileImage) {
    log.info("Profile update requested userId: {}", currentUserId);
    final UserEntity userEntity = userService.findUserEntityById(currentUserId);
    log.debug("userService.findUserEntityById(currentUserId): {}", userEntity);
    log.debug("profileUrl Of Entity: {}", userEntity.getProfileUrl());

    if(request.isProfileImageChanged() && !newProfileImage.isEmpty()) {
      log.info("Profile Image is changed. try to upload to storage. requested userId: {}", currentUserId);
      try {
        final String uploadedURL = uploadNewProfileImage(userEntity, newProfileImage);
        // 업로드가 정상적으로 완료되면, 기존 프로필 이미지를 삭제
        if(userEntity.getProfileUrl() != null) {
          deleteOldProfileImage(userEntity.getProfileUrl());
          deleteOldProfileImage(userEntity.getProfileUrl() + "_original");
        }

        userEntity.setProfileUrl(uploadedURL);
        log.debug("Profile Image uploaded successfully. profile url set as: {}", uploadedURL);
      } catch (IOException e) {
        log.info("Failed to upload new profile image.");
        e.fillInStackTrace();
      }
    } else if (request.isProfileImageChanged() && newProfileImage.isEmpty()) {
      log.info("Profile image is changed but newProfileImage is empty. So, don't update profile image.");
    }

    if(request.isProfileImageChanged() && newProfileImage.isEmpty()) {
      log.info("Profile delete request received.");
      // 프로필 이미지 삭제 요청이 들어온 경우, 기존 프로필 이미지를 삭제
      if(userEntity.getProfileUrl() != null) {
        deleteOldProfileImage(userEntity.getProfileUrl());
        deleteOldProfileImage(userEntity.getProfileUrl() + "_original");
      }

      userEntity.setProfileUrl(null);
    }

    // Gravatar 사용 여부가 true인 경우, 프로필 이미지 주소를 변경
    if(request.useGravatar()) {
      log.info("Gravatar is enabled. So, configure Gravatar URL.");
      // 기존 프로필 이미지를 삭제
      if(userEntity.getProfileUrl() != null) {
        log.info("Delete old profile image.");
        deleteOldProfileImage(userEntity.getProfileUrl());
        deleteOldProfileImage(userEntity.getProfileUrl() + "_original");
      }

      final String gravatarURL = userService.configureGravatarURL(userEntity.getEmail());
      log.info("The Gravatar URL is configured as: {}", gravatarURL);
      userEntity.setProfileUrl(gravatarURL);
    }

    // 이메일이 변경되었는데, Gravatar 사용 여부가 true인 경우, 프로필 이미지 주소를 변경
    if(!userEntity.getEmail().equals(request.email()) && request.useGravatar()) {
      userEntity.setProfileUrl(userService.configureGravatarURL(request.email()));
    }

    userEntity.setEmail(request.email());
    userEntity.setName(request.name());
    userEntity.setUseGravatar(request.useGravatar());

    userService.save(userEntity);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.builder()
            .result(ApiResultCode.SUCCESS)
            .build()
        );
  }

  /**
   * 새로운 프로필 이미지를 업로드하는 메서드
   * Original 이미지와 Thumbnail 이미지를 생성하여 저장한다.
   *
   * @param userEntity 변경할 유저의 엔티티
   * @param newProfileImage 새로운 프로필 이미지
   * @return 새로운 프로필 이미지의 URL (prefix 제외)
   */
  @Transactional
  String uploadNewProfileImage(final UserEntity userEntity, final MultipartFile newProfileImage)
      throws IOException {
    final String randomUUID = UUID.randomUUID().toString();

    // Original 이미지를 비동기로 먼저 업로드 (확장자를 제거하고 업로드하여 S3의 versioning 이 가능하도록 함)
    final String originalFileName = String.format("profile_%s_%s_original", userEntity.getId(),
        randomUUID
    );
    final CompletableFuture<Void> originalUpload = s3Service
        .uploadOriginalFileAsync(newProfileImage, "/profile", originalFileName);

    // 예외 처리 추가
    originalUpload.exceptionally(ex -> {
      log.error("Original image upload failed: {}", ex.getMessage());
      return null;
    });
    log.info("Profile image uploaded successfully as filename: {}", originalFileName);

    // 비동기로 처리되는 동안 Thumbnail 이미지를 생성 및 업로드
    byte[] thumbnailWebP = null;
    try {
      // Original -> Thumbnail WEBP
      thumbnailWebP = ImageUtil.processImage(newProfileImage, 512, 512);
    } catch (IOException ioException) {
      log.info("Failed to process image file.");
      return "";
    }

    // Thumbnail 이미지 업로드 (확장자를 제거하고 업로드하여 S3의 versioning 이 가능하도록 함)
    final String thumbnailFileName = String.format("profile_%s_%s", userEntity.getId(), randomUUID);
    final String thumbnailUploadURL = s3Service.uploadProfileResizedFile(thumbnailWebP, "/profile", thumbnailFileName);
    log.info("Uploaded to S3 as full key: {}", thumbnailFileName);
    log.info("thumbnailUploadURL: {}", thumbnailUploadURL);
    log.info("Profile image uploaded successfully");
    return thumbnailUploadURL;
  }

  void deleteOldProfileImage(final String fullKey) {
    log.info("Delete old profile image. fullKey: {}", fullKey);
    final CompletableFuture<Void> deleteOldProfileImage = s3Service.deleteFile(fullKey);
    deleteOldProfileImage.exceptionally(ex -> {
      log.error("Failed to delete old profile image: {}", ex.getMessage());
      return null;
    });
  }
}
