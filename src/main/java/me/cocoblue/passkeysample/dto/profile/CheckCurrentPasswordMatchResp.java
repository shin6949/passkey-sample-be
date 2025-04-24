package me.cocoblue.passkeysample.dto.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

/**
 * 입력한 비밀번호가 현재 비밀번호와 일치하는지 확인하고 결과를 반환하는 DTO
 *
 * @param isMatch 현재 비밀번호와 일치하는지 여부
 * @param AuthorizationToken 비밀번호 변경 허가 코드 (FE에는 전달하지 않음.)
 */
@Builder
public record CheckCurrentPasswordMatchResp(
    boolean isMatch,
    @JsonIgnore String AuthorizationToken
) {

}
