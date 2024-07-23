package com.ctrls.auto_enter_view.dto.company;

import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SignUpDto {

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    @NotBlank(message = "이메일은 필수 입력값 입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "인증번호는 필수 입력값 입니다.")
    private String verificationCode;

    @NotBlank(message = "비밀번호는 필수 입력값 입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호 형식이 올바르지 않습니다.")
    private String password;

    @NotBlank(message = "회사 이름은 필수 입력값 입니다.")
    private String companyName;

    @NotBlank(message = "전화번호는 필수 입력값 입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String companyNumber;

    public CompanyEntity toEntity(String companyKey, String password) {

      return CompanyEntity.builder()
          .companyKey(companyKey)
          .email(email)
          .password(password)
          .companyName(companyName)
          .companyNumber(companyNumber)
          .role(UserRole.ROLE_COMPANY)
          .build();
    }
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Response {

    private String companyKey;
    private String email;
    private String name;
  }
}