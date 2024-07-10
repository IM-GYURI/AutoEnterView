package com.ctrls.auto_enter_view.dto.common;

import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SignInDto {

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotBlank
    @Pattern(regexp = "^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$", message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;
  }

  @Getter
  @AllArgsConstructor
  @Builder
  public static class Response {

    private String key;
    private String name;
    private String email;
    private UserRole role;
  }

  public static Response fromCompany(CompanyEntity company) {

    return Response.builder()
        .key(company.getCompanyKey())
        .name(company.getCompanyName())
        .email(company.getEmail())
        .role(company.getRole())
        .build();
  }

  public static Response fromCandidate(CandidateEntity candidate) {

    return Response.builder()
        .key(candidate.getCandidateKey())
        .name(candidate.getName())
        .email(candidate.getEmail())
        .role(candidate.getRole())
        .build();
  }
}