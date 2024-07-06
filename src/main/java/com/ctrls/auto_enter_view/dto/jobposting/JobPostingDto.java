package com.ctrls.auto_enter_view.dto.jobposting;


import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.util.KeyGenerator;
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

    private String title;

    private String jobCategory;

    private Integer career;

    private List<String> techStack;

    private List<String> jobPostingStep;

    private String workLocation;

    private String education;

    private String employmentType;

    private Long salary;

    private String workTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateTime;

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
          .startDateTime(request.getStartDateTime())
          .endDateTime(request.getEndDateTime())
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

    public static JobPostingStepEntity toStepEntity(JobPostingEntity entity, String stepName) {
      return JobPostingStepEntity.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .step(stepName)
          .build();
    }

  }


}
