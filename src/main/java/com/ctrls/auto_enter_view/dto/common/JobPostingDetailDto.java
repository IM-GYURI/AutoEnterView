package com.ctrls.auto_enter_view.dto.common;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class JobPostingDetailDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class Response {

    private String jobPostingKey;
    private String title;
    private String jobCategory;
    private Integer career;
    private String workLocation;
    private String education;
    private String employmentType;
    private Long salary;
    private String workTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String jobPostingContent;

    private List<String> techStack;
    private List<String> step;


    public static Response from(JobPostingEntity entity, List<String> techStack,
        List<String> step) {

      return Response.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .title(entity.getTitle())
          .jobCategory(entity.getJobCategory())
          .career(entity.getCareer())
          .workLocation(entity.getWorkLocation())
          .education(entity.getEducation())
          .employmentType(entity.getEmploymentType())
          .salary(entity.getSalary())
          .workTime(entity.getWorkTime())
          .startDate(entity.getStartDate())
          .endDate(entity.getEndDate())
          .jobPostingContent(entity.getJobPostingContent())
          .techStack(techStack)
          .step(step)
          .build();
    }
  }
}
