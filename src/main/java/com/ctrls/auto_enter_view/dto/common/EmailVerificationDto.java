package com.ctrls.auto_enter_view.dto.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationDto {

  @NotBlank(message = "이메일은 필수로 입력되어야 합니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  private String email;

  @NotBlank(message = "인증 코드는 필수로 입력되어야 합니다.")
  private String verificationCode;
}