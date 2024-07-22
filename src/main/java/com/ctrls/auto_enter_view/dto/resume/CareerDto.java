package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import com.ctrls.auto_enter_view.enums.JobCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CareerDto {

  @AllArgsConstructor
  @Builder
  @Getter
  public static class Request {

    @NotBlank(message = "회사 이름은 필수 입력값 입니다.")
    private String companyName;

    @NotNull(message = "직종은 필수 입력값 입니다.")
    private JobCategory jobCategory;

    @NotNull(message = "시작일은 필수 입력값 입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수 입력값 입니다.")
    private LocalDate endDate;

    public ResumeCareerEntity toEntity(String resumeKey) {

      return ResumeCareerEntity.builder()
          .resumeKey(resumeKey)
          .companyName(companyName)
          .jobCategory(jobCategory)
          .startDate(startDate)
          .endDate(endDate)
          .calculatedCareer((int) ChronoUnit.YEARS.between(startDate, endDate))
          .build();
    }
  }

  @AllArgsConstructor
  @Builder
  @Getter
  public static class Response {

    private String companyName;
    private String jobCategory;
    private LocalDate startDate;
    private LocalDate endDate;
    private int calculatedCareer;
  }
}