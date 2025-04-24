package me.cocoblue.passkeysample.controller;

import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.dto.passkey.PassKeyListResp;
import me.cocoblue.passkeysample.dto.passkey.PassKeyUpdateReq;
import me.cocoblue.passkeysample.security.JwtTokenProvider;
import me.cocoblue.passkeysample.service.auth.PassKeyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/passkey")
@RequiredArgsConstructor
public class PassKeyController {
  private final JwtTokenProvider jwtTokenProvider;
  private final PassKeyService passKeyService;

  /**
   * 로그인 된 유저에 등록된 패스키 목록 조회
   *
   * @param accessToken 로그인 된 유저의 액세스 토큰
   * @return 패스키 목록
   */
  @GetMapping("")
  public ResponseEntity<ApiResponse<List<PassKeyListResp>>> getPassKey(@RequestHeader("Authorization") String accessToken) {
    final String userId = this.jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));
    final List<PassKeyListResp> passKeyList = this.passKeyService.getPassKeyList(userId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(
            ApiResponse.<List<PassKeyListResp>>builder()
                .result(ApiResultCode.SUCCESS)
                .data(passKeyList)
                .build()
        );
  }

  /**
   * 특정 패스키에 대한 이름 수정 API
   *
   * @param accessToken 로그인 된 유저의 액세스 토큰
   * @param request 패스키 수정 요청
   * @return 성공 여부
   */
  @PutMapping("")
  public ResponseEntity<ApiResponse<Object>> updatePassKey(@RequestHeader("Authorization") String accessToken,
      @Valid @RequestBody final PassKeyUpdateReq request) {
    final String requesterUserId = jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));

    return this.passKeyService.updatePassKeyLabel(requesterUserId, request);
  }

  /**
   * 특정 패스키에 대한 삭제 API
   *
   * @param accessToken 로그인 된 유저의 액세스 토큰
   * @param uuid 삭제할 패스키의 UUID
   * @return 성공 여부
   */
  @DeleteMapping("/{uuid}")
  public ResponseEntity<ApiResponse<Object>> deletePassKey(@RequestHeader("Authorization") String accessToken,
                                                           @PathVariable("uuid") String uuid) {
    final String requesterUserId = jwtTokenProvider.getUserIdFromAccessToken(accessToken.substring(7));

    return this.passKeyService.deletePassKey(requesterUserId, uuid);
  }
}
