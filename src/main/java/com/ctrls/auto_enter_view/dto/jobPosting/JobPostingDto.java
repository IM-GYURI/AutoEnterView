package com.ctrls.auto_enter_view.dto.jobPosting;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

public class JobPostingDto {

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    private String jobCategory;

    private Integer career;

    @NotEmpty(message = "기술 스택은 필수 입력 항목입니다.")
    private List<String> techStack;

    @NotEmpty(message = "채용 단계는 필수 입력 항목입니다.")
    private List<String> jobPostingStep;

    @NotBlank(message = "근무 위치는 필수 입력 항목입니다.")
    private String workLocation;

    private String education;

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

    public static JobPostingEntity toEntity(String companyKEy, Request request) {

      return JobPostingEntity.builder()
          .companyKey(companyKEy)
          .jobPostingKey(KeyGenerator.generateKey())
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
          .build();
    }

    public static JobPostingTechStackEntity toTechStackEntity(JobPostingEntity entity,
        String techName) {

      return JobPostingTechStackEntity.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .techName(techName)
          .build();
    }

    public static JobPostingTechStackEntity toTechStackEntity(String jobPostingKey,
        String techName) {

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

    public static JobPostingStepEntity toStepEntity(String jobPostingKey, String stepName) {

      return JobPostingStepEntity.builder()
          .jobPostingKey(jobPostingKey)
          .step(stepName)
          .build();
    }
  }
}