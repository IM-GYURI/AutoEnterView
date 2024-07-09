package com.ctrls.auto_enter_view.dto.common;

import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MainJobPostingDto {

  @Getter
  @AllArgsConstructor
  @Builder
  public static class Response {

    private List<JobPostingMainInfo> jobPostingsList;
    private int totalPages;
    private long totalElements;
  }

  @Getter
  @AllArgsConstructor
  @Builder
  public static class JobPostingMainInfo {

    private String jobPostingKey;
    private String companyName;
    private String title;
    private List<String> techStack;
    private LocalDate endDate;

    public static JobPostingMainInfo from(JobPostingEntity entity, String companyName,
        List<String> techStack) {

      return JobPostingMainInfo.builder()
          .jobPostingKey(entity.getJobPostingKey())
          .companyName(companyName)
          .title(entity.getTitle())
          .techStack(techStack)
          .endDate(entity.getEndDate())
          .build();
    }
  }
}