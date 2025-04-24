package me.cocoblue.passkeysample.service.auth;

import me.cocoblue.passkeysample.domain.auth.PasskeyRecordsEntity;
import me.cocoblue.passkeysample.domain.auth.PasskeyRecordsRepository;
import me.cocoblue.passkeysample.domain.auth.PasskeyUserEntity;
import me.cocoblue.passkeysample.domain.auth.PasskeyUserRepository;
import me.cocoblue.passkeysample.dto.ApiResponse;
import me.cocoblue.passkeysample.dto.ApiResultCode;
import me.cocoblue.passkeysample.dto.passkey.PassKeyListResp;
import me.cocoblue.passkeysample.dto.passkey.PassKeyUpdateReq;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * PassKey 관련 서비스 클래스<br>
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author @shin6949
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PassKeyService {
    private final PasskeyRecordsRepository passkeyRecordsRepository;
    private final PasskeyUserRepository passkeyUserRepository;

    public List<PassKeyListResp> getPassKeyList(final String userId) {
        log.debug("getPassKeyList: userId={}", userId);

        final Optional<PasskeyUserEntity> passkeyUserEntity = passkeyUserRepository.findByUserId_Id(
            userId);
        // PassKey를 생성하지 않은 유저인 경우, 빈 리스트를 return
        if (passkeyUserEntity.isEmpty()) {
            log.info("PasskeyUserEntity is not found. userId: {}", userId);
            return List.of();
        }
        final String passKeyUserId = passkeyUserEntity.get().getPassKeyUserId();

        final List<PassKeyListResp> credentialRecords = this.passkeyRecordsRepository.findByUserId(
                passKeyUserId)
            .stream()
            .map(PassKeyListResp::new)
            .collect(Collectors.toList());

        log.debug("getPassKeyList: credentialRecords size: {}", credentialRecords.size());
        return credentialRecords;
    }

    @Transactional
    public ResponseEntity<ApiResponse<Object>> updatePassKeyLabel(final String requesterUserId,
        final PassKeyUpdateReq request) {
        final Optional<PasskeyUserEntity> passkeyUserEntityOptional = passkeyUserRepository.findByUserId_Id(
            requesterUserId);
        final Optional<PasskeyRecordsEntity> passkeyRecordsEntityOptional = passkeyRecordsRepository.findByUuid(
            request.uuid());

        final ResponseEntity<ApiResponse<Object>> errorResponse = validateRequest(passkeyUserEntityOptional, passkeyRecordsEntityOptional);
        if (errorResponse != null) {
            log.info("The updatePassKeyLabel is invalid. requesterUserId: {}, request: {}", requesterUserId, request);
            return errorResponse;
        }

        // 라벨 업데이트
        passkeyRecordsEntityOptional.get().setLabel(request.name());
        passkeyRecordsRepository.save(passkeyRecordsEntityOptional.get());

        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }

    @Transactional
    public ResponseEntity<ApiResponse<Object>> deletePassKey(final String requesterUserId,
        final String toDeleteUuid) {
        final Optional<PasskeyUserEntity> passkeyUserEntityOptional = passkeyUserRepository.findByUserId_Id(
            requesterUserId);
        final Optional<PasskeyRecordsEntity> passkeyRecordsEntityOptional = passkeyRecordsRepository.findByUuid(
                toDeleteUuid);

        final ResponseEntity<ApiResponse<Object>> errorResponse = validateRequest(passkeyUserEntityOptional, passkeyRecordsEntityOptional);
        if (errorResponse != null) {
            log.info("The deletePassKey is invalid. requesterUserId: {}, uuid: {}", requesterUserId, toDeleteUuid);
            return errorResponse;
        }

        // Passkey 삭제
        passkeyRecordsRepository.delete(passkeyRecordsEntityOptional.get());

        return ResponseEntity
            .status(HttpStatus.OK)
            .build();
    }

    /**
     * 요청이 유효한지 확인하는 메소드<br>
     * 1. 실제로 있는 유저, 패스키인지 확인<br>
     * 2. Access Token의 유저와 패스키의 유저가 일치하는지 확인<br>
     *
     * @param userEntity 요청한 유저의 Entity
     * @param recordEntity 요청한 패스키의 Entity
     * @return 유효하지 않은 경우, 에러 응답
     */
    private ResponseEntity<ApiResponse<Object>> validateRequest(
        Optional<PasskeyUserEntity> userEntity,
        Optional<PasskeyRecordsEntity> recordEntity
    ) {
        // 요청한 객체가 실제로 있는지 확인
        if (userEntity.isEmpty() || recordEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                    .result(ApiResultCode.PASSKEY_NOT_FOUND)
                    .build());
        }

        // 요청한 유저와 패스키의 유저가 일치하는지 확인
        if (!recordEntity.get().getUserId().equals(userEntity.get().getPassKeyUserId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                    .result(ApiResultCode.INVALID_CREDENTIALS)
                    .build());
        }

        return null;
    }
}