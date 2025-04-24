package me.cocoblue.passkeysample.controller;

import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.dto.user.EmailCheckResp;
import me.cocoblue.passkeysample.dto.user.SignUpReq;
import me.cocoblue.passkeysample.dto.user.UserApiResp;
import me.cocoblue.passkeysample.security.JwtTokenProvider;
import me.cocoblue.passkeysample.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserApiResp>> fetchLoginUser(@RequestHeader("Authorization") String accessToken) {
    final String id = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));
    final UserApiResp userResponseDto = this.userService.findByIdToApiResp(id);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.<UserApiResp>builder()
            .result(ApiResultCode.SUCCESS)
            .data(userResponseDto)
            .build()
        );
  }

  @GetMapping("/")
  public ResponseEntity<ApiResponse<UserApiResp>> fetchUser(@RequestHeader("Authorization") String accessToken,
      @RequestParam String id) {
    final UserApiResp userResponseDto = this.userService.findByIdToApiResp(id);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.<UserApiResp>builder()
            .result(ApiResultCode.SUCCESS)
            .data(userResponseDto)
            .build()
        );
  }

  @PutMapping("/")
  public ResponseEntity<ApiResponse<UserApiResp>> updateUser(@RequestHeader("Authorization") String accessToken) {
    final String id = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));
    final UserApiResp userResponseDto = this.userService.findByIdToApiResp(id);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.<UserApiResp>builder()
            .result(ApiResultCode.SUCCESS)
            .data(userResponseDto)
            .build()
        );
  }

  @GetMapping("/check-email")
  public ResponseEntity<ApiResponse<EmailCheckResp>> checkEmailDuplicate(@RequestParam String email) {
    boolean exists = userService.isDuplicateEmail(email);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.<EmailCheckResp>builder()
            .result(ApiResultCode.SUCCESS)
            .data(new EmailCheckResp(exists))
            .build()
        );
  }

  @PostMapping("/sign-up")
  public ResponseEntity<?> signUpUser(@Valid @RequestBody SignUpReq signUpReq) {
    log.debug("signUpReq = {}", signUpReq);
    userService.signUp(signUpReq);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
