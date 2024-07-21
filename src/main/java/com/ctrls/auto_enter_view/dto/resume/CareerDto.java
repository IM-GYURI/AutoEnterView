package com.ctrls.auto_enter_view.dto.resume;

import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import com.ctrls.auto_enter_view.enums.JobCategory;
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

    private String companyName;
    private JobCategory jobCategory;
    private LocalDate startDate;
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