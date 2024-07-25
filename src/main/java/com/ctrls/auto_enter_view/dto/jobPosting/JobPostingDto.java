package com.ctrls.auto_enter_view.dto.jobPosting;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class JobPostingDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @NotNull(message = "직종은 필수 입력값 입니다.")
    private JobCategory jobCategory;

    private int career;

    @NotEmpty(message = "기술 스택은 필수 입력 항목입니다.")
    private List<TechStack> techStack;

    @NotEmpty(message = "채용 단계는 필수 입력 항목입니다.")
    private List<String> jobPostingStep;

    @NotBlank(message = "근무 위치는 필수 입력 항목입니다.")
    private String workLocation;

    @NotNull(message = "학력은 필수 입력값 입니다.")
    private Education education;

    @NotBlank(message = "고용 형태는 필수 항목입니다.")
    private String employmentType;

    @NotNull(message = "급여는 필수 입력 항목입니다.")
    private Long salary;

    @NotBlank(message = "근무 시간은 필수 입력 항목입니다.")
    private String workTime;

    @NotNull(message = "채용 공고 시작일은 필수 입력 항목입니다.")
    private LocalDate startDate;

    @NotNull(message = "채용 공고 마감일은 필수 입력 항목입니다.")
    private LocalDate endDate;

    private String jobPostingContent;

    @Min(value = 1, message = "구인수는 1명 이상이어야 합니다.")
    private int passingNumber;

    public static JobPostingEntity toEntity(String key, String companyKey, Request request) {

      return JobPostingEntity.builder()
          .companyKey(companyKey)
          .jobPostingKey(key)
          .title(request.getTitle())
          .jobCategory(request.getJobCategory())
          .career(request.getCareer())
          .workLocation(request.getWorkLocation())
          .education(request.getEducation())
          .employmentType(request.getEmploymentType())
          .salary(request.getSalary())
          .startDate(request.getStartDate())
          .endDate(request.getEndDate())
          .workTime(request.getWorkTime())
          .jobPostingContent(request.getJobPostingContent())
          .passingNumber(request.getPassingNumber())
          .build();
    }

    public static JobPostingTechStackEntity toTechStackEntity(JobPostingEntity entity,
        TechStack techName) {

      return JobPostingTechStackEntity.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .techName(techName)
          .build();
    }

    public static JobPostingTechStackEntity toTechStackEntity(String jobPostingKey,
        TechStack techName) {

      return JobPostingTechStackEntity.builder()
          .jobPostingKey(jobPostingKey)
          .techName(techName)
          .build();
    }

    public static JobPostingStepEntity toStepEntity(JobPostingEntity entity, String stepName) {

      return JobPostingStepEntity.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .step(stepName)
          .build();
    }
  }

  @Getter
  @AllArgsConstructor
  public static class Response {

    private String jobPostingKey;
    private String jobPostingImageUrl;
  }
}