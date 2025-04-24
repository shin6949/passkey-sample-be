package me.cocoblue.passkeysample.dto;

import jakarta.annotation.Nullable;
import lombok.Builder;

public record ApiResponse<T> (
    ApiResultCode result,
    @Nullable T data){

  @Builder
  public ApiResponse(ApiResultCode result, T data) {
    this.result = result;
    this.data = data;
  }
}
