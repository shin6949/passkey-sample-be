package me.cocoblue.passkeysample.controller;

import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.dto.profile.CheckCurrentPasswordMatchReq;
import me.cocoblue.passkeysample.dto.profile.CheckCurrentPasswordMatchResp;
import me.cocoblue.passkeysample.dto.profile.UpdatePasswordReq;
import me.cocoblue.passkeysample.dto.profile.UpdateProfileReq;
import me.cocoblue.passkeysample.security.JwtTokenProvider;
import me.cocoblue.passkeysample.service.auth.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
  private final JwtTokenProvider jwtTokenProvider;
  private final ProfileService profileService;

  /**
   * 비밀번호를 변경하기 전 현재 비밀번호와 일치하는지 확인하는 메서드
   *
   * @param accessToken JWT 토큰
   * @param request     현재 비밀번호 확인 요청
   * @return 현재 비밀번호와 일치하는지 여부
   */
  @PostMapping("/password")
  public ResponseEntity<ApiResponse<CheckCurrentPasswordMatchResp>> checkMatchWithCurrentPassword(
      @RequestHeader("Authorization") String accessToken,
      @Valid @RequestBody CheckCurrentPasswordMatchReq request,
      HttpServletResponse response) {
    final String userId = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));
    final ResponseEntity<ApiResponse<CheckCurrentPasswordMatchResp>> apiResponse = this.profileService.checkMatchWithCurrentPassword(request.inputPassword(), userId);

    if(apiResponse.getBody() != null && apiResponse.getBody().data() != null && apiResponse.getBody().data().isMatch()) {
      log.debug("Saving passwordChangeAuthorizationToken to HTTP Only Cookie.");
      ResponseCookie passwordChangeAuthorizationToken = ResponseCookie.from("passwordChangeAuthorizationToken", apiResponse.getBody().data()
              .AuthorizationToken())
          .httpOnly(true)
          .path("/")
          .maxAge(Duration.ofSeconds(60000L))
          .sameSite("Strict")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, passwordChangeAuthorizationToken.toString());
    }

    return apiResponse;
  }

  /**
   * 비밀번호 변경 요청
   *
   * @param accessToken JWT 토큰
   * @param passwordChangeAuthorizationToken 비밀번호 변경 인가 코드
   * @param request    변경할 비밀번호
   * @return 비밀번호 변경 결과
   */
  @PutMapping("/password")
  public ResponseEntity<ApiResponse<?>> updatePassword(
      @RequestHeader("Authorization") String accessToken,
      @CookieValue(name = "passwordChangeAuthorizationToken", required = false) String passwordChangeAuthorizationToken,
      @Valid @RequestBody UpdatePasswordReq request,
      HttpServletResponse response) {
    final String userId = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));

    // 인가 코드가 없는 경우, 비밀번호 변경 요청을 거부
    if(passwordChangeAuthorizationToken == null) {
      return ResponseEntity
          .status(403)
          .body(ApiResponse.builder()
              .result(ApiResultCode.INVALID_TOKEN)
              .build()
          );
    }

    // 인가 코드는 1회용 코드이므로, 한 번 사용하면 삭제
    ResponseCookie newRefreshTokenCookie = ResponseCookie.from("passwordChangeAuthorizationToken", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0L)
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

    return this.profileService.updatePassword(request, userId, passwordChangeAuthorizationToken);
  }

  /**
   * Password 변경 인가 코드를 폐기하는 메서드
   * Step 2에서 1로 Back 버튼을 누른 경우 작동함.
   */
  @GetMapping("/password/revoke_token")
  public ResponseEntity<?> revokePasswordChangeAuthorizationToken(HttpServletResponse response) {
    log.info("Revoke Password Change Authorization Token Request Received.");
    final ResponseCookie newRefreshTokenCookie = ResponseCookie.from("passwordChangeAuthorizationToken", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0L)
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

    return ResponseEntity.ok().build();
  }

  /**
   * Profile 수정을 위해서 로그인한 사용자의 Profile 정보를 가져오는 메서드
   *
   * @param accessToken JWT 토큰
   * @return Profile 정보
   */
  @GetMapping({"", "/"})
  public ResponseEntity<ApiResponse<?>> fetchProfile(@RequestHeader("Authorization") String accessToken) {
    final String userId = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));

    return this.profileService.fetchProfile(userId);
  }

  /**
   * Profile 수정 요청
   *
   * @param accessToken JWT 토큰
   * @param file Profile 이미지
   * @param request Profile 수정 요청
   * @return Profile 수정 결과
   */
  @PutMapping({"", "/"})
  public ResponseEntity<ApiResponse<?>> updateProfile(
      @RequestHeader("Authorization") String accessToken,
      @RequestPart(value = "profileImage", required = false) MultipartFile file,
      @Valid @RequestPart("profileData") UpdateProfileReq request) {
    final String userId = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));
    log.info("Update Profile Request Received From userId: {}", userId);

    if(request.isProfileImageChanged() && (file == null || file.isEmpty())) {
      log.info("The flag 'isProfileImageChanged' is ture. but The new profile image is not provided.");
    }

    return this.profileService.updateProfile(userId, request, file);
  }
}
