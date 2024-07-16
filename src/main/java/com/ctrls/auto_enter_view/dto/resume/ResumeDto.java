package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.enums.TechStack;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class ResumeDto {

  @AllArgsConstructor
  @Builder
  @Getter
  public static class Request {

    @NotBlank(message = "제목은 필수 입력값 입니다.")
    private String title;

    @NotBlank(message = "희망직종은 필수 입력값 입니다.")
    private String jobWant;

    @NotBlank(message = "이름은 필수 입력값 입니다.")
    private String name;

    @NotBlank(message = "성별은 필수 입력값 입니다.")
    private String gender;

    @NotNull(message = "생년월일은 필수 입력값 입니다.")
    private LocalDate birthDate;

    @NotBlank(message = "이메일은 필수 입력값 입니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수 입력값 입니다.")
    private String phoneNumber;

    @NotBlank(message = "주소는 필수 입력값 입니다.")
    private String address;

    @NotBlank(message = "학력은 필수 입력값 입니다.")
    private String scholarship;

    @NotBlank(message = "학교명은 필수 입력값 입니다.")
    private String schoolName;

    private List<TechStack> techStack;

    private List<ExperienceDto> experience;
    private List<CareerDto> career;
    private List<CertificateDto> certificates;
    private String portfolio;
    private ImageDto image;

    public ResumeEntity toEntity(String resumeKey, String candidateKey) {

      return ResumeEntity.builder()
          .resumeKey(resumeKey)
          .candidateKey(candidateKey)
          .title(title)
          .jobWant(jobWant)
          .name(name)
          .gender(gender)
          .birthDate(birthDate)
          .email(email)
          .phoneNumber(phoneNumber)
          .address(address)
          .scholarship(scholarship)
          .schoolName(schoolName)
          .portfolio(portfolio)
          .build();
    }
  }

  @AllArgsConstructor
  @Builder
  @Getter
  public static class Response {

    private String resumeKey;
  }
}