package com.ctrls.auto_enter_view.dto.common;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
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
    private String companyKey;
    private String title;
    private JobCategory jobCategory;
    private Integer career;
    private String workLocation;
    private Education education;
    private String employmentType;
    private Long salary;
    private String workTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String jobPostingContent;

    private List<TechStack> techStack;
    private List<String> step;
    private String image;

    public static Response from(JobPostingEntity entity, List<TechStack> techStack,
        List<String> step, String imageUrl) {

      return Response.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .companyKey(entity.getCompanyKey())
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
          .image(imageUrl)
          .build();
    }
  }
}